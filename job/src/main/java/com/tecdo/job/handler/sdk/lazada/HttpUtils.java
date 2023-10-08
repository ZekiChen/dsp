package com.tecdo.job.handler.sdk.lazada;

import com.tecdo.core.launch.thread.ThreadFactoryHelper;
import com.tecdo.job.domain.entity.DeviceRecall;
import com.xxl.job.core.context.XxlJobHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpUtils {

  private static final ThreadFactory FACTORY =
    ThreadFactoryHelper.create("async-worker", Thread.NORM_PRIORITY, false);

  private static final BlockingQueue<Runnable> QUEUE = new LinkedBlockingQueue<Runnable>();

  private static final ExecutorService EXECUTOR_SERVICE =
    // 由于用了LinkedBlockingQueue，maximumPoolSize 大于 corePoolSize，也没有作用，实际上是newFixedThreadPool
    new ThreadPoolExecutor(400, 400, 60L, TimeUnit.SECONDS, QUEUE, FACTORY);

  private static final OkHttpClient CLIENT =
    new OkHttpClient.Builder().dispatcher(new Dispatcher(EXECUTOR_SERVICE))
                              .connectionPool(new ConnectionPool(300, 5, TimeUnit.MINUTES))
                              .connectTimeout(5, TimeUnit.SECONDS)
                              .build();

  private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);

  static {
    CLIENT.dispatcher().setMaxRequests(4000);
    CLIENT.dispatcher().setMaxRequestsPerHost(4000);
  }

  public static int queuedCallsCount() {
    return CLIENT.dispatcher().queuedCallsCount();
  }

  private static AtomicInteger count = new AtomicInteger();

  public static void asyncRequest(String url, Map<String, String> header, DeviceRecall device,String clickId) {
    Request.Builder builder = new Request.Builder().url(url);
    try {
      header.forEach(builder::addHeader);
      Request request = builder.build();
      CLIENT.newCall(request).enqueue(new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
          StringWriter stringWriter = new StringWriter();
          e.printStackTrace(new PrintWriter(stringWriter));
          String errorMsg = stringWriter.toString();
          XxlJobHelper.log("HttpUtils get error. url: {}, message: {}", url, errorMsg);
        }

        @Override
        public void onResponse(Call call, Response response) {
          if (response.body() != null) {
            int code = response.code();
            if (code < 400) {
              SdkLogger.log(device, clickId);
            }
            int tmp = count.incrementAndGet();
            if (tmp % 1000 == 0) {
              XxlJobHelper.log("finish total count:{}", tmp);
            }
            response.body().close();
          }
        }
      });
    } catch (Exception e) {
      StringWriter stringWriter = new StringWriter();
      e.printStackTrace(new PrintWriter(stringWriter));
      String errorMsg = stringWriter.toString();
      XxlJobHelper.log("HttpUtils get error. url: {}, message: {}", url, errorMsg);
    }
  }

  public static void request(String url, Map<String, String> header) {
    try {
      Request.Builder builder = new Request.Builder().url(url);
      header.forEach(builder::addHeader);
      Request request = builder.build();
      CLIENT.newCall(request).execute();
    } catch (IOException e) {
      StringWriter stringWriter = new StringWriter();
      e.printStackTrace(new PrintWriter(stringWriter));
      String errorMsg = stringWriter.toString();
      XxlJobHelper.log("HttpUtils get error. url: {}, message: {}", url, errorMsg);
    }
  }

}
