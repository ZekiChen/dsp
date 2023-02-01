package com.tecdo.domain.biz.log;

import lombok.Builder;

import java.io.Serializable;

/**
 * 成功 BidResponse 才会记录的响应日志
 * <p>
 * Created by Zeki on 2023/1/31
 */
@Builder
public class ResponseLog implements Serializable {

    private String bidId;
    private Integer campaignId;
    private Integer adGroupId;
    private Integer adId;
    private Integer creativeId;

    /**
     * 竞价价格
     */
    private Double bidPrice;

    /**
     * 预估ctr
     */
    private Double pCtr;

    /**
     * 英语预估ctr的模型版本
     */
    private String pCtrVersion;

}
