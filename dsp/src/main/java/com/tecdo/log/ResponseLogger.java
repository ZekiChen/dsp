package com.tecdo.log;

import com.alibaba.fastjson2.JSON;
import com.tecdo.domain.biz.dto.AdDTOWrapper;
import com.tecdo.domain.biz.log.ResponseLog;
import com.tecdo.entity.CampaignRtaInfo;
import com.tecdo.util.CreativeHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Optional;

import cn.hutool.core.date.DateUtil;

/**
 * 构建 ResponseLog 并持久化至本地文件中
 * <p>
 * Created by Zeki on 2023/1/31
 */
public class ResponseLogger {

  private final static Logger responseLogger = LoggerFactory.getLogger("response_log");

  public static void log(AdDTOWrapper wrapper) {
    ResponseLog responseLog = buildResponseLog(wrapper);
    responseLogger.info(JSON.toJSONString(responseLog));
  }

  private static ResponseLog buildResponseLog(AdDTOWrapper wrapper) {
    Integer creativeId = CreativeHelper.getCreativeId(wrapper.getAdDTO().getAd());
    return ResponseLog.builder()
                      .createTime(DateUtil.format(new Date(), "yyyy-MM-dd_HH"))
                      .bidId(wrapper.getBidId())
                      .campaignId(wrapper.getAdDTO().getCampaign().getId())
                      .campaignName(wrapper.getAdDTO().getCampaign().getName())
                      .adGroupId(wrapper.getAdDTO().getAdGroup().getId())
                      .adGroupName(wrapper.getAdDTO().getAdGroup().getName())
                      .adId(wrapper.getAdDTO().getAd().getId())
                      .adName(wrapper.getAdDTO().getAd().getName())
                      .creativeId(creativeId)
                      .creativeName(wrapper.getAdDTO().getCreativeMap().get(creativeId).getName())
                      .packageName(wrapper.getAdDTO().getCampaign().getPackageName())
                      .category(wrapper.getAdDTO().getCampaign().getCategory())
                      .feature(Optional.ofNullable(wrapper.getAdDTO().getCampaignRtaInfo())
                                       .map(CampaignRtaInfo::getRtaFeature)
                                       .orElse(null))
                      .bidPrice(wrapper.getBidPrice())
                      .pCtr(wrapper.getPCtr())
                      .pCtrVersion(wrapper.getPCtrVersion())
                      .build();
  }
}
