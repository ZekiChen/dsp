package com.tecdo.filter;

import cn.hutool.core.collection.CollUtil;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.*;
import com.tecdo.domain.openrtb.request.n.NativeRequestAsset;
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

    private static final String EQ_OPERATION = "eq";

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTO adDTO) {
        AdTypeEnum curAdTypeEnum = AdTypeEnum.of(adDTO.getAd().getType());
        if (curAdTypeEnum == null || CollUtil.isEmpty(adDTO.getCreativeMap())) {
            return true;
        }
        switch (curAdTypeEnum) {
            case BANNER:
                Banner banner = imp.getBanner();
                if (banner == null) {
                    return false;
                }
                Creative creative = adDTO.getCreativeMap().get(adDTO.getAd().getIcon());
                if (banner.getW() != null && banner.getH() != null) {
                    if (!ConditionUtil.compare(banner.getW().toString(), EQ_OPERATION, creative.getWidth().toString())
                            || !ConditionUtil.compare(banner.getH().toString(), EQ_OPERATION, creative.getHeight().toString())) {
                        return false;
                    }
                } else {
                    if (CollUtil.isEmpty(banner.getFormat())) {
                        return false;
                    }
                    boolean hitFlag = false;
                    for (Format format : banner.getFormat()) {
                        if (format.getW() != null && format.getH() != null) {
                            if (ConditionUtil.compare(format.getW().toString(), EQ_OPERATION, creative.getWidth().toString())
                                    && ConditionUtil.compare(format.getH().toString(), EQ_OPERATION, creative.getHeight().toString())) {
                                hitFlag = true;
                            }
                        }
                    }
                    return hitFlag;
                }
            case VIDEO:
                Video video = imp.getVideo();
                if (video == null || video.getW() == null || video.getH() == null) {
                    return false;
                }
                creative = adDTO.getCreativeMap().get(adDTO.getAd().getVideo());
                return ConditionUtil.compare(video.getW().toString(), EQ_OPERATION, creative.getWidth().toString())
                        && ConditionUtil.compare(video.getH().toString(), EQ_OPERATION, creative.getHeight().toString());
            case NATIVE:
                Native native1 = imp.getNative1();
                creative = adDTO.getCreativeMap().get(adDTO.getAd().getImage());
                if (native1 == null || native1.getNativeRequest() == null || CollUtil.isEmpty(native1.getNativeRequest().getNativeRequestAssets())) {
                    return false;
                }
                boolean hitFlag = false;
                for (NativeRequestAsset nativeRequestAsset : native1.getNativeRequest().getNativeRequestAssets()) {
                    if (nativeRequestAsset.getImg() == null || nativeRequestAsset.getImg().getW() == null || nativeRequestAsset.getImg().getH() == null) {
                        return false;
                    }
                    if (ConditionUtil.compare(nativeRequestAsset.getImg().getW().toString(), EQ_OPERATION, creative.getWidth().toString())
                            && ConditionUtil.compare(nativeRequestAsset.getImg().getH().toString(), EQ_OPERATION, creative.getHeight().toString())) {
                        hitFlag = true;
                    }
                }
                return hitFlag;
        }
        return true;
    }
}
