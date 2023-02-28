package com.tecdo.common.thread;

import java.util.concurrent.*;

public class ThreadPool {

//  private static String coreSize = SpringUtil.getProperty("pac.thread-pool.core-size");
  private static String coreSize = "12";

  private ExecutorService executorService = //
    new ThreadPoolExecutor(Integer.parseInt(coreSize),
                           Integer.parseInt(coreSize),
                           0L,
                           TimeUnit.SECONDS,
                           new LinkedBlockingQueue<>(),
                           ThreadFactoryHelper.create("async-worker", Thread.NORM_PRIORITY, false));

  private ThreadPool() {
  }

  private static ThreadPool threadPool = new ThreadPool();

  public static ThreadPool getInstance() {
    return threadPool;
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
