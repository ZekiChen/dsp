package com.tecdo.core.launch.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadFactoryHelper {
  public static ThreadFactory create(String name, boolean daemon) {
    return create(name, Thread.MIN_PRIORITY, daemon);
  }

  public static ThreadFactory create(String name, int priority, boolean daemon) {
    return new ThreadFactory() {
      private final AtomicInteger mCount = new AtomicInteger(1);

      @Override
      public Thread newThread(Runnable runnable) {
        Thread result = new Thread(runnable, name + "#" + mCount.getAndIncrement());
        result.setPriority(priority);
        result.setDaemon(daemon);
        return result;
      }
    };
  }
}
