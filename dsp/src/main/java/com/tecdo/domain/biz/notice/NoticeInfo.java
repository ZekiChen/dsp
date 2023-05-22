package com.tecdo.domain.biz.notice;

import com.tecdo.constant.RequestKey;
import com.tecdo.server.request.HttpRequest;
import com.tecdo.service.ValidateCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 将出价的 bidId - NoticeInfo 写入缓存，用于通知请求回调时读取使用
 *
 * Created by Zeki on 2023/4/6
 */
@Setter
@Getter
public class NoticeInfo implements Serializable {

    private Integer campaignId;
    private Integer adGroupId;
    private Integer adId;
    private Integer creativeId;
    private String deviceId;

    // ================ 以下部分非 bid response 时携带 ================
    private String bidSuccessPrice;
    private String eventType;

    private String bidId;
    private String sign;

    private Integer lossCode;

    private ValidateCode validateCode;

    // 以下数据是ae特有的
    private Boolean isRealtime;
    /**
     * 近实时事件，普通用户访问/会员用户访问，uv，0/1/null
     */
    private Integer uvCnt;
    /**
     *  近实时事件，会员用户浏览，0/1/null
     */
    private Integer mbrCnt;

    // 离线事件
    /**
     * 订单金额
     */
    private Double orderAmount;
    /**
     * 付费事件，0/1/null
     */
    private Integer buyerCnt;
    private Double p4pRevenue;
    private Double affiRevenue;
    private Integer newRegister;

    public static NoticeInfo buildInfo(HttpRequest httpRequest) {
        NoticeInfo info = new NoticeInfo();
        info.setBidId(httpRequest.getParamAsStr(RequestKey.BID_ID));
        info.setSign(httpRequest.getParamAsStr(RequestKey.SIGN));
        info.setCampaignId(httpRequest.getParamAsInteger(RequestKey.CAMPAIGN_ID));
        info.setAdGroupId(httpRequest.getParamAsInt(RequestKey.AD_GROUP_ID));
        info.setAdId(httpRequest.getParamAsInt(RequestKey.AD_ID));
        info.setCreativeId(httpRequest.getParamAsInt(RequestKey.CREATIVE_ID));
        info.setDeviceId(httpRequest.getParamAsStr(RequestKey.DEVICE_ID));
        info.setEventType(httpRequest.getParamAsStr(RequestKey.EVENT_TYPE));
        info.setBidSuccessPrice(httpRequest.getParamAsStr(RequestKey.BID_SUCCESS_PRICE));
        info.setLossCode(httpRequest.getParamAsInt(RequestKey.LOSS_CODE));
        return info;
    }
}
