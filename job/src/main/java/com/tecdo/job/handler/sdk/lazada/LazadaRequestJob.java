package com.tecdo.job.handler.sdk.lazada;

import com.tecdo.job.domain.entity.DeviceRecall;
import com.tecdo.job.mapper.ClickJobCurRecordMapper;
import com.tecdo.job.mapper.DeviceRecallMapper;
import com.tecdo.job.util.JsonHelper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class LazadaRequestJob {

  private final DeviceRecallMapper deviceRecallMapper;

  private final ClickJobCurRecordMapper curRecordMapper;

  @Value("${pac.sdk.query-batch-size:10000}")
  private int BATCH_SIZE;
  @Value("${pac.sdk.query-max-loop-count:2}")
  private int MAX_LOOP_COUNt;

  private String tableName = "recall_device";

  @XxlJob("sdk-lazada")
  public void handle() throws InterruptedException, UnsupportedEncodingException {

    String param = XxlJobHelper.getJobParam();
    Map<String, Object> config = JsonHelper.parseMap(param, String.class, Object.class);
    if (config == null) {
      XxlJobHelper.handleFail("error config:" + param);
      return;
    }

    String country = (String) config.get("country");
    String os = (String) config.get("os");
    String packageName = (String) config.get("packageName");
    Integer recallType = ((Number) config.get("recallType")).intValue();
    String url = (String) config.get("url");
    int totalCount = ((Number) config.get("totalCount")).intValue();
    int affSubCount = ((Number) config.get("affSubCount")).intValue();
    int rateLimit = ((Number) config.get("rateLimit")).intValue();
    int time = ((Number) config.getOrDefault("time", -2)).intValue();
    long cycleTimeMillis = ((Number) config.getOrDefault("cycleTimeMillis", 604800000)).longValue();
    boolean adjust = Boolean.parseBoolean((String) config.getOrDefault("adjust", "false"));

    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DATE, time);
    long timeInMillis = calendar.getTimeInMillis();
    int recallTag = (int) (timeInMillis / cycleTimeMillis);

    Worker requestWorker = new Worker(rateLimit, affSubCount);


    int count = 0;
    int loopCount = 0;
    Long dbOffset = curRecordMapper.getCur(country, os, packageName, recallType, tableName);

    if (dbOffset == null || dbOffset == 0) {
      curRecordMapper.createCur(country, os, packageName, recallType, tableName, timeInMillis);
      dbOffset = timeInMillis;
    }
    dbOffset = Math.max(dbOffset, timeInMillis);
    String condition = country + "_" + os + "_" + packageName + "_" + recallType;

    List<DeviceRecall> query = deviceRecallMapper.query(country,
                                                        os,
                                                        packageName,
                                                        recallType,
                                                        recallTag,
                                                        dbOffset,
                                                        Math.min(BATCH_SIZE, totalCount - count));

    while (totalCount > count && MAX_LOOP_COUNt > loopCount) {

      count += query.size();
      dbOffset = query.stream().mapToLong(DeviceRecall::getTimeMillis).max().orElse(timeInMillis);

      if (query.size() == 0) {

        loopCount++;
        XxlJobHelper.log("query all the table,loop back,condition:{},loopCount:{}",
                         condition,
                         loopCount);
      } else {
        // request
        requestWorker.handle(affSubCount, totalCount, query, url, adjust);
      }
      try {

        query = deviceRecallMapper.query(country,
                                         os,
                                         packageName,
                                         recallType,
                                         recallTag,
                                         dbOffset,
                                         Math.min(BATCH_SIZE, totalCount - count));
        // update id offset after every query
        curRecordMapper.updateCur(country, os, packageName, recallType, tableName, dbOffset);
      } catch (Exception e) {
        XxlJobHelper.log(e);
      }
    }
  }

}