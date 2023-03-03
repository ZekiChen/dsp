package com.tecdo.common.thread;

import java.util.concurrent.*;

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

  public void execute(Runnable runnable) {
    executorService.execute(runnable);
  }

  /**
   * this will not throw exception,until you call {@link Future#get()}
   */
  public Future<?> submit(Runnable runnable) {
    return executorService.submit(runnable);
  }

}
