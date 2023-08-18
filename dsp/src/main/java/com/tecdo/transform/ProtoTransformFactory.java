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
        InMobiTransform inMobiTransform = SpringUtil.getBean(InMobiTransform.class);
        inMobiTransform.setForceBannerEnable(false);
        return inMobiTransform;
      case INMOBI + COMMA + FORCE:
        inMobiTransform = SpringUtil.getBean(InMobiTransform.class);
        inMobiTransform.setForceBannerEnable(true);
        return inMobiTransform;
      case YANDEX:
        YandexTransform yandexTransform = SpringUtil.getBean(YandexTransform.class);
        yandexTransform.setForceBannerEnable(false);
        return yandexTransform;
      case YANDEX + COMMA + FORCE:
        yandexTransform = SpringUtil.getBean(YandexTransform.class);
        yandexTransform.setForceBannerEnable(true);
        return yandexTransform;
      default:
        return null;
    }
  }

}
