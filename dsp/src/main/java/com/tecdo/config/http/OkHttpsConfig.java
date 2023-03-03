//package com.tecdo.config.http;
//
//import cn.zhxu.okhttps.Config;
//import cn.zhxu.okhttps.HTTP;
//import net.dreamlu.mica.auto.annotation.AutoService;
//import okhttp3.Dispatcher;
//
//@AutoService(OkHttpsConfig.class)
//public class OkHttpsConfig implements Config {
//
//    @Override
//    public void with(HTTP.Builder builder) {
//        builder.config(okConfig -> {
//            Dispatcher dispatcher = new Dispatcher();
//            dispatcher.setMaxRequests(1);
//            dispatcher.setMaxRequestsPerHost(1);
//            okConfig.dispatcher(dispatcher);
//        });
//    }
//
//}