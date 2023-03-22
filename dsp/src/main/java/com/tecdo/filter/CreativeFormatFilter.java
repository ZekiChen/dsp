package com.tecdo.filter;

import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.Banner;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Format;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.domain.openrtb.request.Native;
import com.tecdo.domain.openrtb.request.Video;
import com.tecdo.domain.openrtb.request.n.NativeRequestAsset;
import com.tecdo.entity.Affiliate;
import com.tecdo.entity.Creative;
import com.tecdo.enums.biz.AdTypeEnum;
import com.tecdo.enums.openrtb.ImageAssetTypeEnum;
import com.tecdo.filter.util.ConditionHelper;

import org.springframework.stereotype.Component;

import java.util.Objects;

import cn.hutool.core.collection.CollUtil;

/**
 * 物料格式 过滤
 * <p>
 * Created by Zeki on 2023/1/3
 **/
@Component
public class CreativeFormatFilter extends AbstractRecallFilter {

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTO adDTO, Affiliate affiliate) {
        AdTypeEnum curAdTypeEnum = AdTypeEnum.of(adDTO.getAd().getType());
        // 没有物料
        if (curAdTypeEnum == null || CollUtil.isEmpty(adDTO.getCreativeMap())) {
            return false;
        }
        switch (curAdTypeEnum) {
            case BANNER:
                Banner banner = imp.getBanner();
                if (banner == null) {
                    return false;
                }
                Creative creative = adDTO.getCreativeMap().get(adDTO.getAd().getImage());
                if (banner.getW() != null && banner.getH() != null) {
                    return ConditionHelper.compare(banner.getW().toString(),
                                                   Constant.EQ,
                                                   creative.getWidth().toString()) &&
                           ConditionHelper.compare(banner.getH().toString(),
                                                   Constant.EQ,
                                                   creative.getHeight().toString());
                } else {
                    if (CollUtil.isEmpty(banner.getFormat())) {
                        return false;
                    }
                    boolean hitFlag = false;
                    for (Format format : banner.getFormat()) {
                        if (format.getW() != null && format.getH() != null) {
                            if (ConditionHelper.compare(format.getW().toString(), Constant.EQ, creative.getWidth().toString())
                                    && ConditionHelper.compare(format.getH().toString(), Constant.EQ, creative.getHeight().toString())) {
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
                if (adDTO.getAd().getVideo() == null ||
                    adDTO.getCreativeMap().get(adDTO.getAd().getVideo()) == null) {
                    return false;
                }
                creative = adDTO.getCreativeMap().get(adDTO.getAd().getVideo());
                return ConditionHelper.compare(video.getW().toString(), Constant.EQ, creative.getWidth().toString())
                        && ConditionHelper.compare(video.getH().toString(), Constant.EQ, creative.getHeight().toString());
            case NATIVE:
                Native native1 = imp.getNative1();
                if (native1 == null || native1.getNativeRequest() == null || CollUtil.isEmpty(native1.getNativeRequest().getAssets())) {
                    return false;
                }
                boolean hitFlag = false;
                for (NativeRequestAsset nativeRequestAsset : native1.getNativeRequest().getAssets()) {
                    if (nativeRequestAsset.getImg() == null) {
                        continue;
                    }
                    if (Objects.equals(nativeRequestAsset.getImg().getType(),
                                       ImageAssetTypeEnum.MAIN.getValue())) {
                        creative = adDTO.getCreativeMap().get(adDTO.getAd().getImage());
                    } else {
                        creative = adDTO.getCreativeMap().get(adDTO.getAd().getIcon());
                    }
                    if (creative == null){
                        return false;
                    }
                    // 先判断是否存在wmin，hmin，如果存在并且大于0，如果大于并且宽高比例一致则为true，如果不大于，也不返回false，接着判断w和h
                    // 由于native存在icon和image，所以判断时为true不能直接返回
                    // 每一轮image的判断都将hitFlag重置为false，只有所有image都符合时才通过
                    hitFlag = false;
                    Integer wmin = nativeRequestAsset.getImg().getWmin();
                    Integer hmin = nativeRequestAsset.getImg().getHmin();
                    Integer w = nativeRequestAsset.getImg().getW();
                    Integer h = nativeRequestAsset.getImg().getH();
                    if (wmin != null && hmin != null) {
                        if (wmin > 0 && hmin > 0 && creative.getWidth() >= wmin &&
                            creative.getHeight() >= hmin &&
                            (float)creative.getWidth() / creative.getHeight() == (float) wmin / hmin) {
                            hitFlag = true;
                            // 这个图像判断通过，跳到下一个图像
                            continue;
                        }
                    }
                    // 没有wmin，hmin，或者min判断不通过，则进入下面的判断
                    // 判断w和h是否存在，大小符合则为true
                    if (w != null && h != null) {
                        if (w.equals(creative.getWidth()) && h.equals(creative.getHeight())) {
                            hitFlag = true;
                        }
                        // icon 只要大于要求值，并且比例相同就通过
                        if (Objects.equals(nativeRequestAsset.getImg().getType(),
                                           ImageAssetTypeEnum.MAIN.getValue())) {
                            if (creative.getWidth() >= w && creative.getHeight() >= h &&
                                (float) creative.getWidth() / creative.getHeight() ==
                                (float) w / h) {
                                hitFlag = true;
                            }
                        }
                    }
                    // 如果这一轮图像判断，hitFlag 为false，则返回false
                    if (!hitFlag) {
                        return false;
                    }
                }
                return hitFlag;
        }
        return true;
    }
}
