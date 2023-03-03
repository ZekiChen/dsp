package com.tecdo.job.controller;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.tecdo.common.util.Params;
import com.tecdo.job.constant.EventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Component
public class MessageQueue {

    @Autowired
    private MessageObserver observer;

    private static final long WARN_COST = 2;

    static class MessageItem {
        EventType eventType;
        Params param;
    }

    // messageQueue is used for producer-consumer mode
    private final LinkedBlockingQueue<MessageItem> messageQueue = new LinkedBlockingQueue<>();

    // store the maps which are not in used
    private final ConcurrentLinkedQueue<MessageItem> itemPool = new ConcurrentLinkedQueue<>();

    /**
     * put message
     */
    public void putMessage(EventType eventType) {
        putMessage(eventType, null);
    }

    /**
     * put message
     */
    public void putMessage(EventType eventType, Params param) {
        MessageItem item = retrieveItem();
        item.eventType = eventType;
        item.param = param;
        try {
            messageQueue.put(item);
        } catch (Exception e) {
            log.error("Failed when put msg!", e);
        }
    }

    /**
     * Notify consumer to handle the message
     */
    public void eventLoop() {
        Transaction transaction = null;
        while (true) {
            try {
                MessageItem item = messageQueue.take();
                transaction = Cat.newTransaction("event", item.eventType.name());
                long startTime = System.currentTimeMillis();
                observer.handle(item.eventType, item.param);
                long cost = System.currentTimeMillis() - startTime;
                if (cost > WARN_COST) {
                    log.warn("handle event: {}, cost long time: {} ms", item.eventType, cost);
                }
                item.param = null;
                itemPool.offer(item);
                transaction.setStatus(Transaction.SUCCESS);
            } catch (Exception e) {
                log.error("Failed in event loop ! cause:", e);
                transaction.setStatus(e);
            } finally {
                transaction.complete();
            }
        }
    }

    /**
     * Retrieve an item
     */
    private MessageItem retrieveItem() {
        MessageItem item = itemPool.poll();
        if (null == item) {
            item = new MessageItem();
        }
        return item;
    }
}
