package com.tecdo.transform;


import cn.hutool.extra.spring.SpringUtil;

public class ProtoTransformFactory {

  private static final String O25_N11 = "o_2.5_n_1.1";

  public static IProtoTransform getProtoTransform(String api) {
    switch (api) {
      case O25_N11:
        return SpringUtil.getBean(O25N11Transform.class);
      default:
        return null;
    }
  }

}
