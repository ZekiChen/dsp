package com.tecdo.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.tecdo.adm.api.delivery.entity.Ad;
import com.tecdo.adm.api.delivery.enums.AdTypeEnum;
import com.tecdo.domain.biz.BidCreative;
import com.tecdo.domain.openrtb.request.*;
import com.tecdo.domain.openrtb.request.n.NativeRequestAsset;
import com.tecdo.enums.openrtb.ImageAssetTypeEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 创意物料 工具
 * <p>
 * Created by Zeki on 2023/1/31
 */
@Slf4j
public class CreativeHelper {

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

    public static BidCreative getAdFormat(Imp imp) {
        BidCreative bidCreative = new BidCreative();
        Banner banner = imp.getBanner();
        Video video = imp.getVideo();
        Audio audio = imp.getAudio();
        Native native1 = imp.getNative1();
        StringBuilder wSb = new StringBuilder();
        StringBuilder hSb = new StringBuilder();
        if (banner != null) {
            bidCreative.setType(AdTypeEnum.BANNER.getType());
            bidCreative.setPos(banner.getPos());
            if (banner.getW() != null && banner.getH() != null) {
                bidCreative.setWidth(banner.getW().toString());
                bidCreative.setHeight(banner.getH().toString());
            } else {
                if (CollUtil.isNotEmpty(banner.getFormat())) {
                    for (Format format : banner.getFormat()) {
                        if (format.getW() != null && format.getH() != null) {
                            wSb.append(format.getW()).append(StrUtil.COMMA);
                            hSb.append(format.getH()).append(StrUtil.COMMA);
                            break;
                        }
                    }
                    if (wSb.length() > 0) {
                        bidCreative.setWidth(wSb.delete(wSb.length() - 1, wSb.length()).toString().split(StrUtil.COMMA)[0]);
                        bidCreative.setHeight(hSb.delete(hSb.length() - 1, hSb.length()).toString().split(StrUtil.COMMA)[0]);
                    }
                }
            }
        } else if (video != null) {
            bidCreative.setType(AdTypeEnum.VIDEO.getType());
            bidCreative.setPos(video.getPos());
            if (video.getW() != null && video.getH() != null) {
                bidCreative.setWidth(video.getW().toString());
                bidCreative.setHeight(video.getH().toString());
            }
        } else if (audio != null) {
            bidCreative.setType(AdTypeEnum.AUDIO.getType());
        } else if (native1 != null) {
            bidCreative.setType(AdTypeEnum.NATIVE.getType());
            if (native1.getNativeRequest() != null && CollUtil.isNotEmpty(native1.getNativeRequest().getAssets())) {
                for (NativeRequestAsset nativeRequestAsset : native1.getNativeRequest().getAssets()) {
                    if (nativeRequestAsset.getImg() == null) {
                        continue;
                    }
                    if (!Objects.equals(nativeRequestAsset.getImg().getType(),
                                        ImageAssetTypeEnum.MAIN.getValue())) {
                        continue;
                    }
                    // 以下就是img,并且是主图像的判断
                    if (nativeRequestAsset.getImg().getW() != null && nativeRequestAsset.getImg().getH() != null) {
                        wSb.append(nativeRequestAsset.getImg().getW()).append(StrUtil.COMMA);
                        hSb.append(nativeRequestAsset.getImg().getH()).append(StrUtil.COMMA);
                    } else if (nativeRequestAsset.getImg().getWmin() != null && nativeRequestAsset.getImg().getHmin() != null) {
                        wSb.append(nativeRequestAsset.getImg().getWmin()).append(StrUtil.COMMA);
                        hSb.append(nativeRequestAsset.getImg().getHmin()).append(StrUtil.COMMA);
                    }
                }
                if (wSb.length() > 0) {
                    bidCreative.setWidth(wSb.delete(wSb.length() - 1, wSb.length()).toString().split(StrUtil.COMMA)[0]);
                    bidCreative.setHeight(hSb.delete(hSb.length() - 1, hSb.length()).toString().split(StrUtil.COMMA)[0]);
                }
            }
        } else if (imp.getImpType() == 1) {
            bidCreative.setType(AdTypeEnum.NATIVE.getType());
        } else {
            log.error("imp type error, imp id: {}", imp.getId());
        }
        return bidCreative;
    }

    public static boolean isAdFormatUnique(Imp imp) {
        int count = 0;
        if (imp.getBanner() != null) {
            count++;
        }
        if (imp.getNative1() != null) {
            count++;
        }
        if (imp.getVideo() != null) {
            count++;
        }
        if (imp.getAudio() != null) {
            count++;
        }
        return count == 1;
    }
}
