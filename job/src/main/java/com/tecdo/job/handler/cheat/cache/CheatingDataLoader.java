//package com.tecdo.job.handler.cheat.cache;
//
//import com.google.common.base.Charsets;
//import com.google.common.hash.BloomFilter;
//import com.google.common.hash.Funnels;
//import com.tecdo.adm.api.doris.entity.CheatingData;
//import com.tecdo.adm.api.doris.entity.CheatingDataSize;
//import com.tecdo.adm.api.doris.mapper.CheatingDataMapper;
//import com.xxl.job.core.handler.annotation.XxlJob;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.Date;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
//import cn.hutool.core.date.DateUtil;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class CheatingDataLoader {
//
//  private final CheatingDataMapper mapper;
//
//  @Value("${pac.cheating.load.batch-size:100000}")
//  private int batchSize;
//
//  @Value("${pac.cheating.fpp:0.0001}")
//  private double fpp;
//
//
//  @XxlJob("cheating-data-load")
//  public void load() {
//    log.info("start");
//    try {
//      List<CheatingData> cheatingData;
//      Long hashCode = 0L;
//      String now = DateUtil.format(new Date(), "yyyy-MM-dd");
//
//      List<CheatingDataSize> reasonCount = mapper.selectSize(now);
//      Map<String, BloomFilter<CharSequence>> map = //
//        reasonCount.stream()
//                   .collect(Collectors.toMap(e -> e.getReason(),
//                                             e -> BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8),
//                                                                     e.getDataSize(),
//                                                                     fpp)));
//      long totalCost = 0;
//      do {
//        cheatingData = mapper.getCheatingData(hashCode, now, batchSize);
//        hashCode = cheatingData.stream()
//                               .map(CheatingData::getHashCode)
//                               .max(Long::compareTo)
//                               .orElse(Long.MAX_VALUE);
//        long A = System.currentTimeMillis();
//        for (CheatingData item : cheatingData) {
//          BloomFilter<CharSequence> filter = map.get(item.getReason());
//          if (filter != null) {
//            filter.put(item.getCheatKey());
//          }
//        }
//        totalCost = totalCost + (System.currentTimeMillis() - A) / 1000;
//        log.info("hashCode:{}", hashCode);
//        log.info("put into bloom filter cost:{} ms", (System.currentTimeMillis() - A));
//      } while (cheatingData.size() > 0);
//
//      log.info("put into bloom filter total cost: " + totalCost);
//      for (Map.Entry<String, BloomFilter<CharSequence>> e : map.entrySet()) {
//        String k = e.getKey();
//        BloomFilter<CharSequence> filter = e.getValue();
//        for (int i = 0; i < 10000000; i++) {
//          filter.put(UUID.randomUUID().toString().substring(0, 6));
//        }
//      }
//
//      long A = System.currentTimeMillis();
//      for (Map.Entry<String, BloomFilter<CharSequence>> entry : map.entrySet()) {
//        String reason = entry.getKey();
//        BloomFilter<CharSequence> filter = entry.getValue();
//        File f = new File(reason + DateUtil.format(new Date(), "yyyy-MM-dd-HH"));
//        OutputStream out = new FileOutputStream(f);
//        filter.writeTo(out);
//      }
//
//      log.info("write to file cost: " + (System.currentTimeMillis() - A) / 1000);
//
//      long B = System.currentTimeMillis();
//      for (Map.Entry<String, BloomFilter<CharSequence>> entry : map.entrySet()) {
//        String reason = entry.getKey();
//        BloomFilter<CharSequence> filter = entry.getValue();
//        File f = new File(reason + DateUtil.format(new Date(), "yyyy-MM-dd-HH"));
//        InputStream in = new FileInputStream(f);
//        filter = BloomFilter.readFrom(in, Funnels.stringFunnel(Charsets.UTF_8));
//        log.info("{} filter,size:{}", reason, filter.approximateElementCount());
//      }
//
//      log.info("read from file cost: " + (System.currentTimeMillis() - B) / 1000);
//
//
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//  }
//
//}
