package com.tecdo.util;

import com.tecdo.entity.Ad;
import com.tecdo.enums.biz.AdTypeEnum;

/**
 * 创意物料 工具
 * <p>
 * Created by Zeki on 2023/1/31
 */
public class CreativeUtil {

    public static Integer getCreativeId(Ad ad) {
        switch (AdTypeEnum.of(ad.getType())) {
            case BANNER:
            case NATIVE:
                return ad.getImage();
            case VIDEO:
                return ad.getVideo();
        }
        return null;
    }
}
