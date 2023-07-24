package com.tecdo.core.launch.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool {

  private ExecutorService executorService;

  public ThreadPool(String coreSize) {
    this.executorService = new ThreadPoolExecutor(Integer.parseInt(coreSize),
            Integer.parseInt(coreSize),
            0L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            ThreadFactoryHelper.create("async-worker", Thread.NORM_PRIORITY, false));
  }

  public ExecutorService getExecutor() {
    return executorService;
  }

  public void execute(Runnable runnable) {
    getExecutor().execute(runnable);
  }

  /**
   * this will not throw exception,until you call {@link Future#get()}
   */
  public Future<?> submit(Runnable runnable) {
    return getExecutor().submit(runnable);
  }

  public <T> Future<T> submit(Callable<T> callable) {
    return getExecutor().submit(callable);
  }
}
