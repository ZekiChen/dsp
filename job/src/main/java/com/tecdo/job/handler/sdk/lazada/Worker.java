package com.tecdo.job.handler.sdk.lazada;

import com.google.common.base.MoreObjects;
import com.google.common.net.HttpHeaders;
import com.google.common.util.concurrent.RateLimiter;
import com.tecdo.job.domain.entity.DeviceRecall;
import com.xxl.job.core.context.XxlJobHelper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import cn.hutool.core.util.IdUtil;

public class Worker {

  private final int QUEUE_LIMIT = 40000;
  private final int NEED_SLEEP_QUEUE_COUNT = 20000;
  private final int SLEEP_TIME_INCR = 1000;
  private final int checkBatchSize = 1000;
  private int sleepTime = 1000;
  private int count = 0;

  private Long requestCount;
  private final RateLimiter rateLimiter;
  private final List<String> affSub;

  public Worker(int rateLimit, int affSubCount) {
    requestCount = 0L;
    rateLimiter = RateLimiter.create(rateLimit);
    affSub = IntStream.range(0, affSubCount)
                      .mapToObj(i -> UUID.randomUUID().toString().substring(0, 9))
                      .collect(Collectors.toList());
  }

  public void handle(int affSubCount, long totalCount, List<DeviceRecall> list, String url)
    throws InterruptedException, UnsupportedEncodingException {
    for (DeviceRecall device : list) {
      control();
      request(affSubCount, totalCount, device, url);
    }
  }

  private void control() throws InterruptedException {
    if (count >= checkBatchSize) {
      int queuedCount = HttpUtils.queuedCallsCount();
      if (queuedCount > QUEUE_LIMIT) {
        sleepTime += SLEEP_TIME_INCR;
      } else {
        sleepTime = Math.max(SLEEP_TIME_INCR, sleepTime / 2);
      }
      XxlJobHelper.log("queuedCount:{}", queuedCount);
      count = 0;
      if (queuedCount >= NEED_SLEEP_QUEUE_COUNT) {
        XxlJobHelper.log("sleepTime:{}", sleepTime);
        Thread.sleep(sleepTime);
      }
    }
  }


  private void request(int affSubCount, long totalCount, DeviceRecall device, String url)
    throws UnsupportedEncodingException {

    if (requestCount >= totalCount) {
      return;
    }
    rateLimiter.acquire();

    Map<String, String> header = new HashMap<>();
    header.put(HttpHeaders.X_FORWARDED_FOR, device.getIp());
    header.put(HttpHeaders.USER_AGENT, device.getUa());
    String lang = MoreObjects.firstNonNull(device.getLang(), "en").toLowerCase(Locale.ROOT);
    header.put(HttpHeaders.ACCEPT_LANGUAGE, lang);


    String clickId = IdUtil.fastSimpleUUID() + System.currentTimeMillis();
    url = url.replace("{device_id}", device.getDeviceId())
             .replace("{click_id}", clickId)
             .replace("{aff_sub5}", affSub.get((int) (requestCount % affSubCount)))
             .replace("{make}", encode(device.getDeviceMake()))
             .replace("{model}", encode(device.getDeviceModel()))
             .replace("{ip}", encode(device.getIp()))
             .replace("{ua}", encode(device.getUa()))
             .replace("{osv}", encode(device.getOsv()))
             .replace("{country}", encode(device.getCountry()))
             .replace("{os}", encode(device.getOs()))
             .replace("{version}", encode(device.getVersion()))
             .replace("{data_source}", encode(device.getDataSource()))
             .replace("{lang}", encode(lang));

    HttpUtils.asyncRequest(url, header, device, clickId);
    requestCount++;
    count++;
  }

  private String encode(String s) throws UnsupportedEncodingException {
    return URLEncoder.encode(MoreObjects.firstNonNull(s, ""), "UTF-8");
  }

}
