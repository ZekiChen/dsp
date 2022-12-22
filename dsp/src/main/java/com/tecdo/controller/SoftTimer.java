package com.tecdo.controller;

import com.tecdo.common.Params;
import com.tecdo.constant.EventType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public class SoftTimer {

  private Logger log = LoggerFactory.getLogger(getClass());

  private AtomicLong itemId = new AtomicLong(0);

  private static SoftTimer instance;

  private SoftTimer() {
    startCheckTimer();
  }

  public static SoftTimer getInstance() {
    if (instance == null) {
      synchronized (SoftTimer.class) {
        if (instance == null) {
          instance = new SoftTimer();
        }
      }
    }
    return instance;
  }

  class TimerItem {

    long id;
    Params params;
    long startTime;
    long delay;
    EventType event;
  }

  // active items
  private ConcurrentHashMap<Long, TimerItem> itemRegistered = new ConcurrentHashMap<>();
  // item pool for reuse
  private ConcurrentLinkedQueue<TimerItem> itemPool = new ConcurrentLinkedQueue<>();

  private Timer checkTimer;
  private long checkInterval = 10;

  protected void startCheckTimer() {
    checkTimer = new Timer();

    // The timer can not be scheduled after it's cancelled, so there, the repeat mode is using
    this.checkTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        check();
      }
    }, this.checkInterval, this.checkInterval);
  }

  private void check() {
    long currentTime;
    Iterator<TimerItem> itr;

    currentTime = System.currentTimeMillis();
    itr = this.itemRegistered.values().iterator();

    while (itr.hasNext()) {
      TimerItem item = itr.next();
      if ((item != null) && (item.delay <= (currentTime - item.startTime))) {
        MessageQueue.getInstance().putMessage(item.event, item.params);
        // push into pool for re-use
        itr.remove();
        this.itemPool.add(item);
      }
    }
  }

  public long startTimer(EventType event, Params params, long delay) {
    long id = itemId.incrementAndGet();
    TimerItem timerItem = this.itemPool.poll();
    if (timerItem == null) {
      timerItem = new TimerItem();
    }

    timerItem.id = id;
    timerItem.params = params;
    timerItem.startTime = System.currentTimeMillis();
    timerItem.delay = delay;
    timerItem.event = event;

    this.itemRegistered.put(id, timerItem);
    if (log.isDebugEnabled()) {
      log.debug("start timer. event is {}", event);
    }
    return id;
  }

  public void cancel(long id) {
    this.itemRegistered.remove(id);
  }

}
