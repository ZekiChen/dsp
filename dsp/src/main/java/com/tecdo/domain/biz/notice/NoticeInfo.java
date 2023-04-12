package com.tecdo.domain.biz.notice;

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
}
