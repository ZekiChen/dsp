package com.tecdo.filter;

import cn.hutool.core.collection.CollUtil;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.*;
import com.tecdo.domain.openrtb.request.n.Asset;
import com.tecdo.entity.Creative;
import com.tecdo.enums.biz.AdTypeEnum;
import com.tecdo.filter.util.ConditionUtil;
import org.springframework.stereotype.Component;

/**
 * 物料格式 过滤
 * <p>
 * Created by Zeki on 2023/1/3
 **/
@Component
public class CreativeFormatFilter extends AbstractRecallFilter {

    private static final String INCLUDE_OPERATION = "include";

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTO adDTO) {
        if (CollUtil.isEmpty(adDTO.getCreative())) {
            return true;
        }
        Banner banner = imp.getBanner();
        Video video = imp.getVideo();
        Native native1 = imp.getNative1();
        for (Creative creative : adDTO.getCreative()) {
            switch (AdTypeEnum.of(creative.getType())) {
                case BANNER:
                    if (banner == null) {
                        return false;
                    }
                    if (banner.getW() != null && banner.getH() != null) {
                        if (!ConditionUtil.compare(banner.getW().toString(), INCLUDE_OPERATION, creative.getWidth())
                                || !ConditionUtil.compare(banner.getH().toString(), INCLUDE_OPERATION, creative.getHeight())) {
                            return false;
                        }
                    } else {
                        if (CollUtil.isEmpty(banner.getFormat())) {
                            return false;
                        }
                        boolean hitFlag = false;
                        for (Format format : banner.getFormat()) {
                            if (format.getW() != null && format.getH() != null) {
                                if (ConditionUtil.compare(format.getW().toString(), INCLUDE_OPERATION, creative.getWidth())
                                        && ConditionUtil.compare(format.getH().toString(), INCLUDE_OPERATION, creative.getHeight())) {
                                    hitFlag = true;
                                }
                            }
                        }
                        if (!hitFlag) {
                            return false;
                        }
                    }
                    break;
                case VIDEO:
                    if (video == null || video.getW() == null || video.getH() == null) {
                        return false;
                    }
                    if (!ConditionUtil.compare(video.getW().toString(), INCLUDE_OPERATION, creative.getWidth())
                            || !ConditionUtil.compare(video.getH().toString(), INCLUDE_OPERATION, creative.getHeight())) {
                        return false;
                    }
                    break;
                case NATIVE:
                    if (native1 == null || native1.getNativeRequest() == null || CollUtil.isEmpty(native1.getNativeRequest().getAssets())) {
                        return false;
                    }
                    boolean hitFlag = false;
                    for (Asset asset : native1.getNativeRequest().getAssets()) {
                        if (asset.getImg() == null || asset.getImg().getW() == null || asset.getImg().getH() == null) {
                            return false;
                        }
                        if (ConditionUtil.compare(asset.getImg().getW().toString(), INCLUDE_OPERATION, creative.getWidth())
                                && ConditionUtil.compare(asset.getImg().getH().toString(), INCLUDE_OPERATION, creative.getHeight())) {
                            hitFlag = true;
                        }
                    }
                    if (!hitFlag) {
                        return false;
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Invalid creative type: " + creative.getType());
            }
        }
        return true;
    }
}
