package com.tecdo.log;

import com.tecdo.domain.biz.log.NotBidReasonLog;
import com.tecdo.util.JsonHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NotBidReasonLogger {
  private final static Logger logger = LoggerFactory.getLogger(NotBidReasonLogger.class);

  private final static Logger notBidReasonLog = LoggerFactory.getLogger("not_bid_reason_log");

  private final static ScheduledExecutorService service =
    Executors.newSingleThreadScheduledExecutor();

  private final static ConcurrentLinkedQueue<NotBidReasonLog> queue = new ConcurrentLinkedQueue<>();

  private static final int LOG_COUNT_PER_BATCH = 200000;
  private static final int LOG_LIST_SIZE = 1000;
  private static final int WRITE_INTERVAL = 1000;

  public static void log(String bidId, Integer adId, String reason) {
    NotBidReasonLog log = new NotBidReasonLog(bidId, adId, reason);
    queue.offer(log);
  }

  public static void init() {
    service.scheduleAtFixedRate(new WriteTask(), 1000, WRITE_INTERVAL, TimeUnit.MILLISECONDS);
  }

  static class WriteTask implements Runnable {

    @Override
    public void run() {
      int count = 0;
      ArrayList<NotBidReasonLog> list = new ArrayList<>(LOG_LIST_SIZE);
      while (count < LOG_COUNT_PER_BATCH) {
        try {
          NotBidReasonLog item = queue.poll();
          if (item != null) {
            list.add(item);
            count++;
          } else {
            break;
          }
          if (list.size() >= LOG_LIST_SIZE) {
            notBidReasonLog.info(JsonHelper.toJSONString(list));
            list.clear();
          }
        } catch (Exception e) {
          logger.error("Handle not bid log catch an exception.", e);
        }
      }
      if (list.size() > 0) {
        notBidReasonLog.info(JsonHelper.toJSONString(list));
      }
    }
  }

}
