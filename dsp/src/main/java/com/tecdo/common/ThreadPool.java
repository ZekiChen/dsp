package com.tecdo.common;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool {

  private ExecutorService executorService = //
    new ThreadPoolExecutor(16,
                           Integer.MAX_VALUE,
                           60L,
                           TimeUnit.SECONDS,
                           new ArrayBlockingQueue<>(32),
                           ThreadFactoryHelper.create("async-worker", Thread.NORM_PRIORITY, false));

  private ThreadPool() {
  }

  private static ThreadPool threadPool = new ThreadPool();

  public static ThreadPool getInstance() {
    return threadPool;
  }

  public Future<?> execute(Runnable runnable) {
    return executorService.submit(runnable);
  }

}
