package com.tecdo.config.http;

import com.ejlchina.okhttps.Config;
import com.ejlchina.okhttps.HTTP;

import java.util.concurrent.Executors;

import cn.hutool.extra.spring.SpringUtil;
import okhttp3.Dispatcher;

public class OkHttpsConfig implements Config {

  private static String coreSize = SpringUtil.getProperty("pac.ok-https.core-size");
  private static String maxRequests = SpringUtil.getProperty("pac.ok-https.max-requests");
  private static String maxRequestsPerHost =
    SpringUtil.getProperty("pac.ok-https.max-requests-per-host");

  @Override
  public void with(HTTP.Builder builder) {
    builder.config(okConfig -> {
      Dispatcher dispatcher =
        new Dispatcher(Executors.newFixedThreadPool(Integer.parseInt(coreSize)));
      dispatcher.setMaxRequests(Integer.parseInt(maxRequests));
      dispatcher.setMaxRequestsPerHost(Integer.parseInt(maxRequestsPerHost));
      okConfig.dispatcher(dispatcher);
    });
  }
}
