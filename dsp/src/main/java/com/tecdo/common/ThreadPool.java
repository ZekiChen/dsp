package com.tecdo.common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cn.hutool.extra.spring.SpringUtil;

public class ThreadPool {

  private static String coreSize = SpringUtil.getProperty("pac.thread-pool.core-size");

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
