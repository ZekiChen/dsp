package com.tecdo.controller;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.tecdo.common.Params;
import com.tecdo.constant.EventType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageQueue {

  private static final long WARN_COST = 2;
  private static Logger logger = LoggerFactory.getLogger(MessageQueue.class);

  class MessageItem {

    EventType eventType;
    Params param;
  }

  private static MessageQueue ourInstance = new MessageQueue();

  private MessageQueue() {
  }

  public static MessageQueue getInstance() {
    return ourInstance;
  }

  //messageQueue is used for producer-consumer mode
  private LinkedBlockingQueue<MessageItem> messageQueue = new LinkedBlockingQueue<>();

  //store the maps which are not in used
  private ConcurrentLinkedQueue<MessageItem> itemPool = new ConcurrentLinkedQueue<>();

  private MessageObserver observer;

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
      logger.error("Failed when put msg!", e);
    }
  }

  /**
   * Register observer
   */
  public void registerObserver(MessageObserver observer) {
    this.observer = observer;
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
          logger.warn("handle event {} cost long time: {} ms", item.eventType, cost);
        }
        item.param = null;
        itemPool.offer(item);
        transaction.setStatus(Transaction.SUCCESS);
      } catch (Exception e) {
        logger.error("Failed in event loop ! cause:", e);
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
