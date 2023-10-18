package com.tecdo.filter;

import cn.hutool.core.collection.CollUtil;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.entity.Creative;
import com.tecdo.adm.api.delivery.enums.AdTypeEnum;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.*;
import com.tecdo.domain.openrtb.request.n.Img;
import com.tecdo.domain.openrtb.request.n.NativeRequestAsset;
import com.tecdo.enums.biz.VideoMimeEnum;
import com.tecdo.enums.biz.VideoProtocolEnum;
import com.tecdo.enums.openrtb.ImageAssetTypeEnum;
import com.tecdo.transform.ProtoTransformFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

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
                return bannerFilter(imp.getBanner(), adDTO);
            case NATIVE:
                return nativeFilter(imp.getNative1(), adDTO, affiliate);
            case VIDEO:
                return videoFilter(imp.getVideo(), adDTO);
            default:
                return true;
        }
    }

    private static boolean bannerFilter(Banner banner, AdDTO adDTO) {
        if (banner == null) {
            return false;
        }
        Creative creative = adDTO.getCreativeMap().get(adDTO.getAd().getImage());
        if (creative == null) {
            return false;
        }
        if (banner.getW() != null && banner.getH() != null) {
            return (((float) creative.getWidth() / creative.getHeight() ==
                    (float) banner.getW() / banner.getH()));
        } else {
            if (CollUtil.isEmpty(banner.getFormat())) {
                return false;
            }
            boolean hitFlag = false;
            for (Format format : banner.getFormat()) {
                if (format.getW() != null && format.getH() != null) {
                    if ((float) creative.getWidth() / creative.getHeight() ==
                            (float) format.getW() / format.getH()) {
                        hitFlag = true;
                    }
                }
            }
            return hitFlag;
        }
    }

    private boolean nativeFilter(Native native1, AdDTO adDTO, Affiliate affiliate) {
        if (affiliate.getApi().equals(ProtoTransformFactory.VIVO)) {
            return true;
        } else {
            if (native1 == null || native1.getNativeRequest() == null
                    || CollUtil.isEmpty(native1.getNativeRequest().getAssets())) {
                return false;
            }
            for (NativeRequestAsset asset : native1.getNativeRequest().getAssets()) {
                if (asset.getImg() != null && adDTO.getAd().getImage() != null) {
                    Creative creative = getCreativeByImgType(asset, adDTO);
                    if (creative == null) {
                        return false;
                    }
                    if (!checkImgSize(asset.getImg(), creative)) {
                        return false;
                    }
                } else if (asset.getVideo() != null && adDTO.getAd().getVideo() != null) {
                    if (!videoFilter(asset.getVideo(), adDTO)) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    private static boolean videoFilter(Video video, AdDTO adDTO) {
        if (video == null || video.getW() == null || video.getH() == null) {
            return false;
        }
        List<String> mimes = video.getMimes();
        List<Integer> protocols = video.getProtocols();
        Integer videoId = adDTO.getAd().getVideo();
        if (videoId == null
                || adDTO.getCreativeMap().get(videoId) == null
                || CollUtil.isEmpty(mimes)
                || CollUtil.isEmpty(protocols)) {
            return false;
        }
        Creative creative = adDTO.getCreativeMap().get(videoId);
        switch (VideoMimeEnum.of(creative.getSuffix())) {
            case MP4:
                boolean mimeSupport = mimes.stream().anyMatch(s -> VideoMimeEnum.MP4.getMime().equals(s));
                if (!mimeSupport) {
                    return false;
                }
                break;
            case OTHER:
                return false;
        }
        Integer minDuration = video.getMinduration();
        Integer maxDuration = video.getMaxduration();
        Integer duration = creative.getDuration();
        if ((minDuration != null && duration < minDuration)
                || (maxDuration != null && duration > maxDuration)) {
            return false;
        }
        if ((float) creative.getWidth() / creative.getHeight() != (float) video.getW() / video.getH()) {
            return false;
        }
        return protocols.stream().anyMatch(type -> VideoProtocolEnum.of(type) != VideoProtocolEnum.OTHER);
    }


    private Creative getCreativeByImgType(NativeRequestAsset nativeRequestAsset, AdDTO adDTO) {
        return Objects.equals(nativeRequestAsset.getImg().getType(), ImageAssetTypeEnum.MAIN.getValue())
                ? adDTO.getCreativeMap().get(adDTO.getAd().getImage())
                : adDTO.getCreativeMap().get(adDTO.getAd().getIcon());
    }

    private boolean checkImgSize(Img img, Creative creative) {
        return isSizeMatch(img.getWmin(), img.getHmin(), creative.getWidth(), creative.getHeight())
                || isSizeMatch(img.getW(), img.getH(), creative.getWidth(), creative.getHeight());
    }

    private boolean isSizeMatch(Integer reqW, Integer reqH, Integer creW, Integer creH) {
        return reqW != null && reqH != null && reqW > 0 && reqH > 0
                && creW >= reqW && creH >= reqH
                && (float) creW / creH == (float) reqW / reqH;
    }
}
