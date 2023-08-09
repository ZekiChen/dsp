package com.tecdo;

import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.tecdo.entity.CheatingData;
import com.tecdo.entity.CheatingDataSize;
import com.tecdo.mapper.doris.CheatingDataMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

  @Value("${pac.cheating.load.batch-size:2000000}")
  private int batchSize;

  @Value("${pac.cheating.fpp:0.0001}")
  private double fpp;

  @Value("${pac.cheating.base-dir:~/data/}")
  private String baseDir;


  public void run() {
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
            log.info("load cheating data meet error", e);
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

      long A = System.currentTimeMillis();
      for (Map.Entry<String, BloomFilter<CharSequence>> entry : collect.entrySet()) {
        String reason = entry.getKey();
        BloomFilter<CharSequence> filter = entry.getValue();
        String fileName = baseDir + DateUtil.format(new Date(), "yyyyMMdd") + "/" + reason;
        File f = new File(fileName);
        if (!f.getParentFile().exists()) {
          f.getParentFile().mkdirs();
        }
        OutputStream out = new FileOutputStream(f);
        filter.writeTo(out);
      }

      log.info("write to file cost:{} s", (System.currentTimeMillis() - A) / 1000);

      // 清理历史过期文件
      Calendar calendar = Calendar.getInstance();
      for (int i = -2; i > -7; i--) {
        calendar.add(Calendar.DATE, i);
        File directory = new File(baseDir + DateUtil.format(calendar.getTime(), "yyyyMMdd"));
        if (directory.exists() && directory.isDirectory()) {
          File[] files = directory.listFiles();
          for (File file : files) {
            file.delete();
          }
          directory.delete();
        }
      }

    } catch (Exception e) {
      log.info("load cheating data meet error", e);
    }
  }

}
