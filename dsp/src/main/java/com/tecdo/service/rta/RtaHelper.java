package com.tecdo.service.rta;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.tecdo.adm.api.delivery.entity.CampaignRtaInfo;
import com.tecdo.adm.api.delivery.entity.RtaInfo;
import com.tecdo.adm.api.delivery.enums.AdvTypeEnum;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.biz.dto.AdDTOWrapper;
import com.tecdo.service.rta.api.LazopClient;
import com.tecdo.service.rta.api.LazopRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class RtaHelper {

  private static final String RTA_SERVER_URL = SpringUtil.getProperty("foreign.lazada.rta.url");
  private static final String API_NAME = SpringUtil.getProperty("foreign.lazada.rta.api");

  private static final ConcurrentHashMap<String, LazopClient> clientMap = new ConcurrentHashMap<>();

  public static void requestRta(RtaInfo rtaInfo,
                                List<AdDTOWrapper> adList,
                                String country,
                                String gaid,
                                Map<Integer, Target> rtaResMap) {


    Integer advMemberId = rtaInfo.getAdvMemId();
    String advAppKey = rtaInfo.getAppKey();
    String advAppSecret = rtaInfo.getAppSecret();

    // advCampaignId,List<CampaignId>
    Map<String, Set<Integer>> advCampaignId2CampaignIdSet = //
      adList.stream()
            .map(AdDTOWrapper::getAdDTO)
            .map(AdDTO::getCampaignRtaInfo)
            .collect(Collectors.groupingBy(CampaignRtaInfo::getAdvCampaignId,
                                           Collectors.mapping(CampaignRtaInfo::getCampaignId,
                                                              Collectors.toSet())));

    StringBuilder sb = new StringBuilder();
    sb.append("[");
    advCampaignId2CampaignIdSet.keySet().forEach(advCampaignId -> {
      sb.append("\"").append(advCampaignId).append("\"").append(",");
    });
    sb.delete(sb.length() - 1, sb.length());
    sb.append("]");
    String campaignIdStringList = sb.toString();
    ResponseDTO response = null;
    try {
      response = requestRta(country,
                            gaid,
                            campaignIdStringList,
                            String.valueOf(advMemberId),
                            advAppKey,
                            advAppSecret);
    } catch (Exception e) {
      String adGroupIds = adList.stream()
              .map(w -> w.getAdDTO().getAdGroup().getId().toString())
              .distinct()
              .collect(Collectors.joining(StrUtil.COMMA));
      log.error("query lazada rta catch exception, country: {}, adGroupIds: {},", country, adGroupIds, e);
    }
    if (response != null) {
      if (LazadaCode.SUCCESS.equalsIgnoreCase(response.getCode()) && response.getData() != null) {
        LazadaRtaData data = response.getData();
        List<LazadaTarget> targetList = data.getTargetList();
        targetList.forEach(i -> {
          Target target = new Target();
          target.setAdvType(AdvTypeEnum.LAZADA_RTA.getType());
          if (i.isTarget()) {
            target.setTarget(true);
            target.setToken(data.getToken());
          } else {
            target.setTarget(false);
          }
          String advCampaignId = i.getAdvCampaignId();
          Set<Integer> campaignIdSet = advCampaignId2CampaignIdSet.get(advCampaignId);
          campaignIdSet.forEach(campaignId -> {
            rtaResMap.put(campaignId, target);
          });
        });
      } else {
        log.error("query rta failed,message:{}", response.getMessage());
      }
    }
  }


  private static ResponseDTO requestRta(String country,
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
    ResponseDTO response = client.execute(request, AdvTypeEnum.LAZADA_RTA);
    return response;
  }

  private static LazopClient getClient(String advMemberId, String appKey, String appSecret) {

    return clientMap.computeIfAbsent(advMemberId,
                                     key -> new LazopClient(RTA_SERVER_URL, appKey, appSecret));
  }
}
