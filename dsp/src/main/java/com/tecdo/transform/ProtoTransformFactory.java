package com.tecdo.transform;


import cn.hutool.extra.spring.SpringUtil;

import static cn.hutool.core.util.StrUtil.COMMA;

public class ProtoTransformFactory {

  public static final String O25_N11 = "o_2.5_n_1.1";
  public static final String INMOBI = "inmobi";
  public static final String FORCE = "force";
  public static final String YANDEX = "yandex";

  public static IProtoTransform getProtoTransform(String api) {
    switch (api) {
      case O25_N11:
        return SpringUtil.getBean(O25N11Transform.class);
      case FORCE:
        return SpringUtil.getBean(ForceBannerTransform.class);
      case INMOBI:
        return SpringUtil.getBean(InMobiTransform.class);
      case INMOBI + COMMA + FORCE:
        return SpringUtil.getBean(InMobiForceTransform.class);
      case YANDEX:
        return SpringUtil.getBean(YandexTransform.class);
      case YANDEX + COMMA + FORCE:
        return SpringUtil.getBean(YandexForceTransform.class);
      default:
        return null;
    }
  }

}
