package com.tecdo.job.handler.cheat.cache;

import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.tecdo.adm.api.doris.entity.CheatingData;
import com.tecdo.adm.api.doris.entity.CheatingDataSize;
import com.tecdo.adm.api.doris.mapper.CheatingDataMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.hutool.core.date.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CheatingDataLoader {

  private final CheatingDataMapper mapper;

  @Value("${pac.cheating.load.batch-size:100000}")
  private int batchSize;

  @Value("${pac.cheating.fpp:0.0001}")
  private double fpp;


  @XxlJob("cheating-data-load")
  public void load() {
    log.info("start");
    try {
      List<CheatingData> cheatingData = new ArrayList<>();

      String now = DateUtil.format(new Date(), "yyyy-MM-dd HH");

      List<CheatingDataSize> reasonCount = mapper.selectSize(now);
      Map<String, BloomFilter<CharSequence>> collect = //
        reasonCount.stream()
                   .collect(Collectors.toMap(CheatingDataSize::getReason,
                                             e -> BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8),
                                                                     e.getDataSize(),
                                                                     fpp)));
      long start = System.currentTimeMillis();
      for (Map.Entry<String, BloomFilter<CharSequence>> entry : collect.entrySet()) {
        String reason = entry.getKey();
        BloomFilter<CharSequence> filter = entry.getValue();
        Long hashCode = 0L;
        do {
          try {
            cheatingData = mapper.getCheatingData(hashCode, now, reason, batchSize);
          } catch (Exception e) {
            StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            String errorMsg = stringWriter.toString();
            XxlJobHelper.log("load cheating data meet error:{}", errorMsg);
          }
          hashCode = cheatingData.stream()
                                 .map(CheatingData::getHashCode)
                                 .max(Long::compareTo)
                                 .orElse(Long.MAX_VALUE);
          for (CheatingData item : cheatingData) {
            filter.put(item.getCheatKey());
          }
        } while (cheatingData.size() > 0);
      }
      log.info("load cheating data from db cost:{} s", (System.currentTimeMillis() - start) / 1000);
      XxlJobHelper.log("load cheating data from db cost:{} s", (System.currentTimeMillis() - start) / 1000);


      long A = System.currentTimeMillis();
      for (Map.Entry<String, BloomFilter<CharSequence>> entry : collect.entrySet()) {
        String reason = entry.getKey();
        BloomFilter<CharSequence> filter = entry.getValue();
        File f = new File(reason + DateUtil.format(new Date(), "yyyyMMddHH"));
        OutputStream out = new FileOutputStream(f);
        filter.writeTo(out);
      }

      log.info("write to file cost:{} s", (System.currentTimeMillis() - A) / 1000);
      XxlJobHelper.log("write to file cost:{} s", (System.currentTimeMillis() - A) / 1000);

      collect.clear();

      Map<String, BloomFilter<CharSequence>> read = new HashMap<>();
      long B = System.currentTimeMillis();
      for (CheatingDataSize item : reasonCount) {
        String reason = item.getReason();
        BloomFilter<CharSequence> filter;
        File f = new File(reason + DateUtil.format(new Date(), "yyyyMMddHH"));
        InputStream in = new FileInputStream(f);
        filter = BloomFilter.readFrom(in, Funnels.stringFunnel(Charsets.UTF_8));
        read.put(reason, filter);
      }
      log.info("read from file cost:{} s", (System.currentTimeMillis() - B) / 1000);
      XxlJobHelper.log("read from file cost:{} s", (System.currentTimeMillis() - B) / 1000);
    } catch (Exception e) {
      StringWriter stringWriter = new StringWriter();
      e.printStackTrace(new PrintWriter(stringWriter));
      String errorMsg = stringWriter.toString();
      XxlJobHelper.log("meet error:{}", errorMsg);
    }
  }

}
