package com.tecdo.log;

import com.alibaba.fastjson2.JSON;
import com.tecdo.domain.biz.dto.AdDTOWrapper;
import com.tecdo.domain.biz.log.ResponseLog;
import com.tecdo.domain.openrtb.response.BidResponse;
import com.tecdo.util.CreativeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 构建 ResponseLog 并持久化至本地文件中
 * <p>
 * Created by Zeki on 2023/1/31
 */
public class ResponseLogger {

    private final static Logger responseLogger = LoggerFactory.getLogger("response_log");

    public static void log(BidResponse bidResponse, AdDTOWrapper wrapper) {
        ResponseLog responseLog = buildResponseLog(bidResponse, wrapper);
        responseLogger.info(JSON.toJSONString(responseLog));
    }

    private static ResponseLog buildResponseLog(BidResponse bidResponse, AdDTOWrapper wrapper) {
        return ResponseLog.builder()
                .bidId(bidResponse.getId())
                .campaignId(wrapper.getAdDTO().getCampaign().getId())
                .adGroupId(wrapper.getAdDTO().getAdGroup().getId())
                .adId(wrapper.getAdDTO().getAd().getId())
                .creativeId(CreativeUtil.getCreativeId(wrapper.getAdDTO().getAd()))
                .bidPrice(wrapper.getBidPrice())
                .pCtr(wrapper.getPCtr())
                .pCtrVersion(wrapper.getPCtrVersion())
                .build();
    }
}
