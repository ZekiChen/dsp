package com.tecdo.config.http;

import com.ejlchina.okhttps.Config;
import com.ejlchina.okhttps.HTTP;

import java.util.concurrent.Executors;

import cn.hutool.extra.spring.SpringUtil;

import okhttp3.Dispatcher;

public class OkHttpsConfig implements Config {

  private static String coreSize = "256";
  private static String maxRequests = "256";
  private static String maxRequestsPerHost =
    "256";

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
