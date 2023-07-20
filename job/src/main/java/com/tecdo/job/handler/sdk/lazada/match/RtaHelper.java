package com.tecdo.job.handler.sdk.lazada.match;

import com.tecdo.job.handler.sdk.lazada.match.api.LazopClient;
import com.tecdo.job.handler.sdk.lazada.match.api.LazopRequest;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RtaHelper {

  private static final String RTA_SERVER_URL = SpringUtil.getProperty("foreign.lazada.rta.url");
  private static final String API_NAME = SpringUtil.getProperty("foreign.lazada.rta.api");

  private static final ConcurrentHashMap<String, LazopClient> clientMap = new ConcurrentHashMap<>();

  public static int requestRta(String country,
                               String gaid,
                               String advCampaignId,
                               String advMemberId,
                               String advAppKey,
                               String advAppSecret) {


    StringBuilder sb = new StringBuilder();
    sb.append("[").append("\"").append(advCampaignId).append("\"").append("]");
    String campaignIdStringList = sb.toString();
    ResponseDTO response = null;
    try {
      response =
        doRequestRta(country, gaid, campaignIdStringList, advMemberId, advAppKey, advAppSecret);
    } catch (Exception e) {
      log.error("query rta catch exception", e);
    }
    if (response != null) {
      if (LazadaCode.SUCCESS.equalsIgnoreCase(response.getCode()) && response.getData() != null) {
        LazadaRtaData data = response.getData();
        List<LazadaTarget> targetList = data.getTargetList();
        Optional<LazadaTarget> any = targetList.stream().filter(LazadaTarget::isTarget).findAny();
        return any.isPresent() ? Status.ACTIVE : Status.PAUSED;
      } else {
        log.error("query rta failed,message:{}", response.getMessage());
        return Status.UNKNOWN;
      }
    }
    return Status.UNKNOWN;
  }


  private static ResponseDTO doRequestRta(String country,
                                          String gaid,
                                          String campaignIdList,
                                          String advMemberId,
                                          String appKey,
                                          String appSecret) throws Exception {
    LazopClient client = getClient(advMemberId, appKey, appSecret);
    LazopRequest request = new LazopRequest(API_NAME);
    request.setTimestamp(new Date().getTime());
    request.addApiParameter("country", country);
    request.addApiParameter("gaid", gaid);
    request.addApiParameter("member_id", advMemberId);
    request.addApiParameter("campaign_id_list", campaignIdList);
    ResponseDTO response = client.execute(request);
    return response;
  }

  private static LazopClient getClient(String advMemberId, String appKey, String appSecret) {

    return clientMap.computeIfAbsent(advMemberId,
                                     key -> new LazopClient(RTA_SERVER_URL, appKey, appSecret));
  }
}
