package com.tecdo.job.handler.sdk.lazada;

import com.tecdo.core.launch.thread.ThreadPool;
import com.tecdo.job.domain.entity.DeviceRecall;
import com.tecdo.job.handler.sdk.lazada.match.RtaHelper;
import com.tecdo.job.handler.sdk.lazada.match.Status;
import com.tecdo.job.mapper.DeviceRecallMapper;
import com.tecdo.job.mapper.DeviceRecallOdsMapper;
import com.tecdo.job.util.JsonHelper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class LazadaMatchJob {

  private final DeviceRecallMapper deviceRecallMapper;
  private final DeviceRecallOdsMapper odsMapper;

  private final ThreadPool threadPool;

  @Value("${pac.sdk.match.query-batch-size:10000}")
  private int BATCH_SIZE;

  @Value("${pac.sdk.match.timeout:5000}")
  private int TIMEOUT;

  @XxlJob("sdk-lazada-match")
  public void handle() throws InterruptedException, UnsupportedEncodingException {

    String param = XxlJobHelper.getJobParam();
    Map<String, Object> config = JsonHelper.parseMap(param, String.class, Object.class);
    if (config == null) {
      XxlJobHelper.handleFail("error config:" + param);
      return;
    }

    String countryCode3 = (String) config.get("countryCode3");
    String countryCode2 = (String) config.get("countryCode2");
    String os = (String) config.get("os");
    String packageName = (String) config.get("packageName");
    String recallType = (String) config.get("recallType");
    int time = ((Number) config.getOrDefault("time", -6)).intValue();

    String advCampaignId = (String) config.get("advCampaignId");
    String advMemId = (String) config.get("advMemId");
    String appKey = (String) config.get("appKey");
    String appSecret = (String) config.get("appSecret");

    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DATE, time);
    long timeInMillis = calendar.getTimeInMillis();
    int recallTag = (int) (timeInMillis / 604800000);

    Long dbOffset = timeInMillis;

    List<DeviceRecall> query;
    do {
      query = deviceRecallMapper.queryForUpdate(countryCode3,
                                                os,
                                                packageName,
                                                recallType,
                                                recallTag,
                                                dbOffset,
                                                BATCH_SIZE);
      dbOffset = query.stream().mapToLong(DeviceRecall::getTimeMillis).max().orElse(Long.MAX_VALUE);

      List<Future<DeviceRecall>> futureList = new ArrayList<>();

      for (DeviceRecall recall : query) {
        Future<DeviceRecall> future = threadPool.submit(() -> {
          int status = RtaHelper.requestRta(countryCode2,
                                            recall.getDeviceId(),
                                            advCampaignId,
                                            advMemId,
                                            appKey,
                                            appSecret);
          // 请求成功，并且前后状态不一致，则需要更新
          if (status != Status.UNKNOWN && status != recall.getStatus()) {
            DeviceRecall res = new DeviceRecall();
            res.setRecallTag(recall.getRecallTag());
            res.setRecallType(recall.getRecallType());
            res.setCountry(recall.getCountry());
            res.setOs(recall.getOs());
            res.setPackageName(recall.getPackageName());
            res.setDeviceId(recall.getDeviceId());
            res.setTimeMillis(System.currentTimeMillis());
            res.setStatus(status);
            return res;
          }
          return null;
        });
        futureList.add(future);
      }

      List<DeviceRecall> needUpdate = new ArrayList<>();
      try {
        for (Future<DeviceRecall> future : futureList) {
          DeviceRecall deviceRecall = future.get(TIMEOUT, TimeUnit.MILLISECONDS);
          if (deviceRecall != null) {
            needUpdate.add(deviceRecall);
          }
        }
      } catch (Exception e) {
        XxlJobHelper.log(e);
      }
      if (needUpdate.size() > 0) {
        XxlJobHelper.log("update size:{}", needUpdate.size());
        XxlJobHelper.log(JsonHelper.toJSONString(needUpdate));
        odsMapper.update(needUpdate);
      } else {
        XxlJobHelper.log("not need update");
      }
    } while (query.size() > 0);

  }


}