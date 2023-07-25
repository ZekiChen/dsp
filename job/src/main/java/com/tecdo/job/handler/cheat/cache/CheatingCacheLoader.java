package com.tecdo.job.handler.cheat.cache;

import com.tecdo.adm.api.doris.entity.CheatingData;
import com.tecdo.adm.api.doris.entity.CheatingDataSize;
import com.tecdo.adm.api.doris.mapper.CheatingDataMapper;
import com.tecdo.common.constant.CacheConstant;
import com.tecdo.core.launch.thread.ThreadPool;
import com.xxl.job.core.handler.annotation.XxlJob;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CheatingCacheLoader {

  private final CheatingDataMapper mapper;

  private final RedissonClient redissonClient;

  private final ThreadPool threadPool;

  @Value("${pac.cheating.load.batch-size:100000}")
  private int batchSize;

  @Value("${pac.cheating.fpp:0.0001}")
  private double fpp;

  @Value("${pac.cheating.redis.timeout:3000}")
  private int TIMEOUT;

  @Value("${pac.cheating.redis.batch-size:1000}")
  private int redisBatchSize;

  @XxlJob("cheating-load-redis")
  public void load() {
    log.info("start");
    try {
      List<CheatingData> cheatingData;
      Long hashCode = 0L;
      String now = DateUtil.format(new Date(), "yyyy-MM-dd");

      List<CheatingDataSize> reasonCount = mapper.selectSize(now);

      Map<String, RBloomFilter<String>> collect = //
        reasonCount.stream()
                   .collect(Collectors.toMap(CheatingDataSize::getReason,
                                             e -> getBloomFilter(makeKey(e.getReason()),
                                                                 e.getDataSize())));
      long totalCost = 0;
      do {
        cheatingData = mapper.getCheatingData(hashCode, now, batchSize);
        hashCode = cheatingData.stream()
                               .map(CheatingData::getHashCode)
                               .max(Long::compareTo)
                               .orElse(Long.MAX_VALUE);
        log.info("hashCode:{}", hashCode);

        long A = System.currentTimeMillis();
        List<Future<Boolean>> futureList = new ArrayList<>();
        for (CheatingData item : cheatingData) {
          RBloomFilter<String> filter = collect.get(item.getReason());
          if (filter != null) {
            Future<Boolean> future = threadPool.submit(() -> filter.add(item.getCheatKey()));
            futureList.add(future);
            if (futureList.size() >= redisBatchSize) {
              for (Future<Boolean> i : futureList) {
                i.get(TIMEOUT, TimeUnit.MILLISECONDS);
              }
              futureList.clear();
            }
          }
        }

        for (Future<Boolean> future : futureList) {
          future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        }

        totalCost = totalCost + (System.currentTimeMillis() - A) / 1000;
        log.info("put into bloom filter cost:{} ms", (System.currentTimeMillis() - A));
      } while (cheatingData.size() > 0);

      log.info("put into bloom filter total cost: " + totalCost);

      // set expire
      collect.values().forEach(f -> f.expire(2, TimeUnit.HOURS));

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // todo
  //  这里可能存在一个问题，存入的数据量过多，导致布隆过滤器误判率升高。当不重复的数据量大于2E时，需要切分成多个布隆过滤器
  private RBloomFilter<String> getBloomFilter(String key, Long size) {
    RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(key);

    if (bloomFilter.isExists()) {
      return bloomFilter;
    }
    bloomFilter.tryInit(size, fpp);
    return bloomFilter;
  }

  private String makeKey(String reason) {
    Calendar calendar = Calendar.getInstance();
    String time;
    String key;

    time = DateUtil.format(calendar.getTime(), "yyyyMMddHH");
    key = CacheConstant.CF_CACHE.concat(StrUtil.COLON)
                                .concat(reason)
                                .concat(StrUtil.COLON)
                                .concat(time);
    return key;
  }

}
