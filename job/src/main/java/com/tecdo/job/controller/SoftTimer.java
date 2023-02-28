package com.tecdo.job.controller;

import com.tecdo.common.util.Params;
import com.tecdo.job.constant.EventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class SoftTimer {

    private final AtomicLong itemId = new AtomicLong(0);

    @Autowired
    private MessageQueue messageQueue;

    private SoftTimer() {
        startCheckTimer();
    }

    static class TimerItem {
        long id;
        Params params;
        long startTime;
        long delay;
        EventType event;
    }

    // active items
    private final ConcurrentHashMap<Long, TimerItem> itemRegistered = new ConcurrentHashMap<>();
    // item pool for reuse
    private final ConcurrentLinkedQueue<TimerItem> itemPool = new ConcurrentLinkedQueue<>();

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
        log.info("start soft timer");
    }

    private void check() {
        long currentTime;
        Iterator<TimerItem> itr;

        currentTime = System.currentTimeMillis();
        itr = this.itemRegistered.values().iterator();

        while (itr.hasNext()) {
            TimerItem item = itr.next();
            if ((item != null) && (item.delay <= (currentTime - item.startTime))) {
                messageQueue.putMessage(item.event, item.params);
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
