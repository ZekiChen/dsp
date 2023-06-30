package com.tecdo.domain.biz.notice;

import com.tecdo.constant.RequestKeyByImpInfo;
import com.tecdo.server.request.HttpRequest;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Zeki on 2023/5/4
 */
@Setter
@Getter
public class ImpInfoNoticeInfo extends NoticeInfo {

    private String width;
    private String height;
    private String zIndex;
    private String maxIndex;
    private String display;
    private String viewWidth;
    private String viewHeight;
    private String imgTop;
    private String imgBottom;
    private String imgLeft;
    private String imgRight;

    public static ImpInfoNoticeInfo buildInfo(HttpRequest httpRequest) {
        ImpInfoNoticeInfo info = new ImpInfoNoticeInfo();
        info.setBidId(httpRequest.getParamAsStr(RequestKeyByImpInfo.BID_ID));
        info.setSign(httpRequest.getParamAsStr(RequestKeyByImpInfo.SIGN));
        info.setCampaignId(httpRequest.getParamAsInteger(RequestKeyByImpInfo.CAMPAIGN_ID));
        info.setAdGroupId(httpRequest.getParamAsInt(RequestKeyByImpInfo.AD_GROUP_ID));
        info.setAdId(httpRequest.getParamAsInt(RequestKeyByImpInfo.AD_ID));
        info.setCreativeId(httpRequest.getParamAsInt(RequestKeyByImpInfo.CREATIVE_ID));
        info.setDeviceId(httpRequest.getParamAsStr(RequestKeyByImpInfo.DEVICE_ID));
        info.setAffiliateId(httpRequest.getParamAsInt(RequestKeyByImpInfo.AFFILIATE_ID));

        info.setWidth(httpRequest.getParamAsStr(RequestKeyByImpInfo.WIDTH));
        info.setHeight(httpRequest.getParamAsStr(RequestKeyByImpInfo.HEIGHT));
        info.setZIndex(httpRequest.getParamAsStr(RequestKeyByImpInfo.Z_INDEX));
        info.setMaxIndex(httpRequest.getParamAsStr(RequestKeyByImpInfo.MAX_INDEX));
        info.setDisplay(httpRequest.getParamAsStr(RequestKeyByImpInfo.DISPLAY));
        info.setViewWidth(httpRequest.getParamAsStr(RequestKeyByImpInfo.VIEW_WIDTH));
        info.setViewHeight(httpRequest.getParamAsStr(RequestKeyByImpInfo.VIEW_HEIGHT));
        info.setImgTop(httpRequest.getParamAsStr(RequestKeyByImpInfo.IMG_TOP));
        info.setImgBottom(httpRequest.getParamAsStr(RequestKeyByImpInfo.IMG_BOTTOM));
        info.setImgLeft(httpRequest.getParamAsStr(RequestKeyByImpInfo.IMG_LEFT));
        info.setImgRight(httpRequest.getParamAsStr(RequestKeyByImpInfo.IMG_RIGHT));
        return info;
    }
}
