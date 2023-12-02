package com.tecdo.log;

import com.tecdo.domain.biz.log.NotBidReasonLog;
import com.tecdo.util.JsonHelper;
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotBidReasonLogger {
  private final static Logger logger = LoggerFactory.getLogger(NotBidReasonLogger.class);

  private final static Logger notBidReasonLog = LoggerFactory.getLogger("not_bid_reason_log");

  private final static ScheduledExecutorService service =
      Executors.newSingleThreadScheduledExecutor();

  private final static ConcurrentHashMap<String, LinkedBlockingQueue<NotBidReasonLog>> map =
    new ConcurrentHashMap<>();

  private static final int CLEAR_INTERVAL = 600;
  private static final int NEED_CLEAR_DATA_TIME = 600;

  public static void log(String bidId, Integer adId, String reason) {
    NotBidReasonLog log = new NotBidReasonLog(bidId, adId, reason);
    map.computeIfAbsent(bidId, k -> new LinkedBlockingQueue<>()).offer(log);
  }

  public static void consume(String bidId) {
    LinkedBlockingQueue<NotBidReasonLog> logQueue = map.remove(bidId);
    if (logQueue != null && !logQueue.isEmpty()) {
      notBidReasonLog.info(JsonHelper.toJSONString(logQueue));
      logQueue.clear();
    }
  }

  public static void clear(String bidId) {
    LinkedBlockingQueue<NotBidReasonLog> logQueue = map.remove(bidId);
    if (logQueue != null && !logQueue.isEmpty()) {
      logQueue.clear();
    }
  }

  public static void init() {
    service.scheduleAtFixedRate(new ClearTask(), 60, CLEAR_INTERVAL, TimeUnit.SECONDS);
  }


  static class ClearTask implements Runnable {

    @Override
    public void run() {
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.SECOND, -NEED_CLEAR_DATA_TIME);
      long needClearTime = calendar.getTimeInMillis();
      KeySetView<String, LinkedBlockingQueue<NotBidReasonLog>> keys = map.keySet();
      for (String key : keys) {
        long createStamp = Long.parseLong(key.substring(32, 45));
        if (createStamp < needClearTime) {
          clear(key);
        }
      }
    }
  }

}
