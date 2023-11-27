package com.tecdo.log;

import com.tecdo.domain.biz.log.NotBidReasonLog;
import com.tecdo.util.JsonHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class NotBidReasonLogger {
  private final static Logger logger = LoggerFactory.getLogger(NotBidReasonLogger.class);

  private final static Logger notBidReasonLog = LoggerFactory.getLogger("not_bid_reason_log");

  private final static ConcurrentHashMap<String, LinkedBlockingQueue<NotBidReasonLog>> map =
    new ConcurrentHashMap<>();

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

}
