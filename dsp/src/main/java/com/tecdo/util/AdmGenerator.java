package com.tecdo.util;

import com.tecdo.adm.api.delivery.entity.Creative;
import com.tecdo.constant.FormatKey;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.n.NativeRequest;
import com.tecdo.domain.openrtb.request.n.NativeRequestAsset;
import com.tecdo.domain.openrtb.response.n.Data;
import com.tecdo.domain.openrtb.response.n.Img;
import com.tecdo.domain.openrtb.response.n.Link;
import com.tecdo.domain.openrtb.response.n.NativeResponse;
import com.tecdo.domain.openrtb.response.n.NativeResponseAsset;
import com.tecdo.domain.openrtb.response.n.Title;
import com.tecdo.enums.biz.VideoMimeEnum;
import com.tecdo.enums.openrtb.DataAssetTypeEnum;
import com.tecdo.enums.openrtb.ImageAssetTypeEnum;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import cn.hutool.core.date.DateUtil;

public class AdmGenerator {

    public static String forceBannerAdm(String clickUrl,
                                        String deepLink,
                                        String imgUrl,
                                        List<String> impTrackUrl,
                                        List<String> clickTrackUrl,
                                        String impInfoUrl,
                                        String forceLink,
                                        String forceJudgeUrl,
                                        String collectFeatureUrl,
                                        String collectCodeUrl,
                                        String collectErrorUrl,
                                        double delayTime,
                                        boolean encrypt) {
        String finalClickUrl = StringUtils.firstNonBlank(deepLink, clickUrl);
        StringBuilder impDivListBuilder = new StringBuilder();
        String impDivTemplate = "<img src=\"{impTrack}\" style=\"display:none\"/>";
        for (String s : impTrackUrl) {
            impDivListBuilder.append(impDivTemplate.replace("{impTrack}", s));
        }
        StringBuilder clickTrackBuilder = new StringBuilder();
        for (String s : clickTrackUrl) {
            clickTrackBuilder.append("\"").append(s).append("\"").append(",");
        }
        clickTrackBuilder.delete(clickTrackBuilder.length() - 1, clickTrackBuilder.length());
        String admTemplate;
        if (encrypt) {
            admTemplate = StringConfigUtil.getForceBannerTemplate();
        } else {
            admTemplate = StringConfigUtil.getNotEncryptForceBannerTemplate();
        }
        String adm = admTemplate.replace(FormatKey.CLICK_URL, finalClickUrl)
                .replace(FormatKey.FORCE_URL, forceLink)
                .replace(FormatKey.IMG_URL, imgUrl)
                .replace(FormatKey.IMP_DIV_LIST, impDivListBuilder.toString())
                .replace(FormatKey.CLICK_TRACK_URL_LIST, clickTrackBuilder.toString())
                .replace(FormatKey.IMP_INFO_URL, impInfoUrl)
                .replace(FormatKey.COLLECT_FEATURE_URL, collectFeatureUrl)
                .replace(FormatKey.COLLECT_CODE_URL, collectCodeUrl)
                .replace(FormatKey.COLLECT_ERROR_URL, collectErrorUrl)
                .replace(FormatKey.DELAY_TIME, String.valueOf(delayTime))
                .replace(FormatKey.FORCE_JUDGE_URL, forceJudgeUrl);
        return adm;
    }

    public static String bannerAdm(String clickUrl,
                                   String deepLink,
                                   String imgUrl,
                                   List<String> impTrackUrl,
                                   List<String> clickTrackUrl,
                                   String impInfoUrl,
                                   String collectFeatureUrl,
                                   String collectCodeUrl,
                                   String collectErrorUrl,
                                   String checkUrl,
                                   String checkCountUrl,
                                   boolean needCheck) {
        String finalClickUrl = StringUtils.firstNonBlank(deepLink, clickUrl);
        StringBuilder impDivListBuilder = new StringBuilder();
        String impDivTemplate = "<img src=\"{impTrack}\" style=\"display:none\"/>";
        for (String s : impTrackUrl) {
            impDivListBuilder.append(impDivTemplate.replace("{impTrack}", s));
        }
        StringBuilder clickTrackBuilder = new StringBuilder();
        for (String s : clickTrackUrl) {
            clickTrackBuilder.append("\"").append(s).append("\"").append(",");
        }
        clickTrackBuilder.delete(clickTrackBuilder.length() - 1, clickTrackBuilder.length());
        String admTemplate;
        if (needCheck) {
            admTemplate = StringConfigUtil.getBannerTemplateWithCheck();
        } else {
            admTemplate = StringConfigUtil.getBannerTemplate();
        }
        String adm = admTemplate.replace(FormatKey.CLICK_URL, finalClickUrl)
                .replace(FormatKey.IMG_URL, imgUrl)
                .replace(FormatKey.IMP_DIV_LIST, impDivListBuilder.toString())
                .replace(FormatKey.CLICK_TRACK_URL_LIST, clickTrackBuilder.toString())
                .replace(FormatKey.COLLECT_FEATURE_URL, collectFeatureUrl)
                .replace(FormatKey.COLLECT_CODE_URL, collectCodeUrl)
                .replace(FormatKey.COLLECT_ERROR_URL, collectErrorUrl)
                .replace(FormatKey.PIXALATE_CHECK_URL, checkUrl)
                .replace(FormatKey.PIXALATE_CHECK_COUNT_URL, checkCountUrl)
                .replace(FormatKey.IMP_INFO_URL, impInfoUrl);
        return adm;
    }

    public static NativeResponse nativeAdm(NativeRequest nativeRequest,
                                           AdDTO adDTO,
                                           String clickUrl,
                                           String deepLink,
                                           List<String> impTrackUrl,
                                           List<String> clickTrackUrl) {
        List<NativeRequestAsset> nativeRequestAssets = nativeRequest.getAssets();

        List<NativeResponseAsset> responseAssetList = new ArrayList<>();
        for (NativeRequestAsset asset : nativeRequestAssets) {
            NativeResponseAsset nativeResponseAsset = new NativeResponseAsset();
            nativeResponseAsset.setId(asset.getId());
            nativeResponseAsset.setRequired(asset.getRequired());
            if (asset.getImg() != null) {
                Img img = new Img();
                Integer type = asset.getImg().getType();
                Creative creative;
                if (Objects.equals(type, ImageAssetTypeEnum.ICON.getValue())) {
                    creative = adDTO.getCreativeMap().get(adDTO.getAd().getIcon());
                } else {
                    creative = adDTO.getCreativeMap().get(adDTO.getAd().getImage());
                }
                img.setUrl(creative.getUrl());
                img.setW(creative.getWidth());
                img.setH(creative.getHeight());
                nativeResponseAsset.setImg(img);
            }
            if (asset.getTitle() != null) {
                Title title = new Title();
                Integer len = asset.getTitle().getLen();
                String titleString = adDTO.getAd().getTitle();
                if (len != null && len > 0) {
                    titleString = titleString.substring(0, Math.min(len, titleString.length()));
                }
                title.setText(titleString);
                nativeResponseAsset.setTitle(title);
            }
            if (asset.getData() != null) {
                Integer type = asset.getData().getType();
                Integer len = asset.getData().getLen();
                String value = null;
                switch (DataAssetTypeEnum.of(type)) {
                    case ctatext:
                        value = adDTO.getAd().getCta();
                        break;
                    case DESC:
                        value = adDTO.getAd().getDescription();
                        break;
                    case SPONSORED:
                        value = adDTO.getAd().getName();
                        break;
                    default:
                }
                if (value == null) {
                    continue;
                }
                if (len != null && len > 0) {
                    value = value.substring(0, Math.min(len, value.length()));
                }
                Data data = new Data();
                data.setValue(value);
                nativeResponseAsset.setData(data);
            }
            responseAssetList.add(nativeResponseAsset);
        }
        NativeResponse nativeResponse = new NativeResponse();
        nativeResponse.setVer(nativeRequest.getVer());
        nativeResponse.setAssets(responseAssetList);
        nativeResponse.setImptrackers(impTrackUrl);
        Link link = new Link();
        link.setUrl(StringUtils.firstNonBlank(deepLink, clickUrl));
        link.setFallback(clickUrl);
        link.setClicktrackers(clickTrackUrl);
        nativeResponse.setLink(link);

        return nativeResponse;
    }

    public static String videoAdm(Integer adId, Creative video, Creative image,
                                  String clickUrl, String deepLink,
                                  List<String> impTrackUrl, List<String> clickTrackUrl) {
        String impTemplate = "<Impression><![CDATA[{IMP_TRACK}]]></Impression>";
        String impTracks = impTrackUrl.stream()
                .map(s -> impTemplate.replace("{IMP_TRACK}", s)).collect(Collectors.joining());

        String clickTemplate = "<ClickTracking><![CDATA[{CLICK_TRACK}]]></ClickTracking>";
        String clickTracks = clickTrackUrl.stream()
                .map(s -> clickTemplate.replace("{CLICK_TRACK}", s)).collect(Collectors.joining());

        String companionAdsTemplate = "<CompanionClickTracking><![CDATA[{CLICK_TRACK}]]></CompanionClickTracking>";
        String companionClickTracks = clickTrackUrl.stream()
                .map(s -> companionAdsTemplate.replace("{CLICK_TRACK}", s)).collect(Collectors.joining());

        String companionAdTemplate = "";
        if (image != null) {
            companionAdTemplate = "<Creative><CompanionAds>"
                    + "<Companion width=\"{WIDTH}\" height=\"{HEIGHT}\">"
                    + "<StaticResource><![CDATA[{IMG_URL}]]></StaticResource>"
                    + "<CompanionClickThrough><![CDATA[{LANDING_PAGE}]]></CompanionClickThrough>"
                    + companionClickTracks
                    + "</Companion></CompanionAds></Creative>";
            companionAdTemplate = companionAdTemplate
                    .replace(FormatKey.WIDTH, image.getWidth().toString())
                    .replace(FormatKey.HEIGHT, image.getHeight().toString())
                    .replace(FormatKey.IMG_URL, image.getUrl())
                    .replace(FormatKey.LANDING_PAGE, StringUtils.firstNonBlank(deepLink, clickUrl));
        }

        return StringConfigUtil.getVideoVast4Template()
                .replace(FormatKey.AD_ID, adId.toString())
                .replace(FormatKey.VIDEO_NAME, video.getName() + "." + video.getSuffix())
                .replace(FormatKey.VIDEO_URL, video.getUrl())
                .replace(FormatKey.VIDEO_DURATION, DateUtil.secondToTime(video.getDuration()))
                .replace(FormatKey.LANDING_PAGE, StringUtils.firstNonBlank(deepLink, clickUrl))
                .replace(FormatKey.MIME_TYPE, VideoMimeEnum.of(video.getSuffix()).getMime())
                .replace(FormatKey.WIDTH, video.getWidth().toString())
                .replace(FormatKey.HEIGHT, video.getHeight().toString())
                .replace(FormatKey.CLICK_TRACK_LIST, clickTracks)
                .replace(FormatKey.IMP_TRACK_LIST, impTracks)
                .replace(FormatKey.COMPANION_AD, companionAdTemplate)
                ;
    }

    public static String forceVideoAdm(Integer adId, Creative video, Creative image,
                                       String clickUrl, String deepLink,
                                       List<String> impTrackUrl, List<String> clickTrackUrl,
                                       String forceBannerAdm) {
        String impTemplate = "<Impression><![CDATA[{IMP_TRACK}]]></Impression>";
        String impTracks = impTrackUrl.stream()
                .map(s -> impTemplate.replace("{IMP_TRACK}", s)).collect(Collectors.joining());

        String clickTemplate = "<ClickTracking><![CDATA[{CLICK_TRACK}]]></ClickTracking>";
        String clickTracks = clickTrackUrl.stream()
                .map(s -> clickTemplate.replace("{CLICK_TRACK}", s)).collect(Collectors.joining());

        String companionAdsTemplate = "<CompanionClickTracking><![CDATA[{CLICK_TRACK}]]></CompanionClickTracking>";
        String companionClickTracks = clickTrackUrl.stream()
                .map(s -> companionAdsTemplate.replace("{CLICK_TRACK}", s)).collect(Collectors.joining());

        String companionAdTemplate = "";
        if (image != null) {
            companionAdTemplate = "<Creative><CompanionAds>"
                    + "<Companion width=\"{WIDTH}\" height=\"{HEIGHT}\">"
                    + "<HTMLResource><![CDATA[{FORCE_BANNER_ADM}]]></HTMLResource>"
                    + "<CompanionClickThrough><![CDATA[{LANDING_PAGE}]]></CompanionClickThrough>"
                    + companionClickTracks
                    + "</Companion></CompanionAds></Creative>";
            companionAdTemplate = companionAdTemplate
                    .replace(FormatKey.WIDTH, image.getWidth().toString())
                    .replace(FormatKey.HEIGHT, image.getHeight().toString())
                    .replace(FormatKey.FORCE_BANNER_ADM, forceBannerAdm)
                    .replace(FormatKey.LANDING_PAGE, StringUtils.firstNonBlank(deepLink, clickUrl));
        }

        return StringConfigUtil.getVideoVast4Template()
                .replace(FormatKey.AD_ID, adId.toString())
                .replace(FormatKey.VIDEO_NAME, video.getName() + "." + video.getSuffix())
                .replace(FormatKey.VIDEO_URL, video.getUrl())
                .replace(FormatKey.VIDEO_DURATION, DateUtil.secondToTime(video.getDuration()))
                .replace(FormatKey.LANDING_PAGE, StringUtils.firstNonBlank(deepLink, clickUrl))
                .replace(FormatKey.MIME_TYPE, VideoMimeEnum.of(video.getSuffix()).getMime())
                .replace(FormatKey.WIDTH, video.getWidth().toString())
                .replace(FormatKey.HEIGHT, video.getHeight().toString())
                .replace(FormatKey.CLICK_TRACK_LIST, clickTracks)
                .replace(FormatKey.IMP_TRACK_LIST, impTracks)
                .replace(FormatKey.COMPANION_AD, companionAdTemplate)
                ;
    }

}
