package com.tecdo.service.rta;

import com.lazada.lazop.api.LazopClient;
import com.lazada.lazop.api.LazopRequest;
import com.lazada.lazop.api.LazopResponse;
import com.lazada.lazop.util.ApiException;
import com.tecdo.domain.dto.AdDTO;
import com.tecdo.entity.CampaignRtaInfo;
import com.tecdo.entity.RtaInfo;
import com.tecdo.util.JsonHelper;

import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RtaService {

  private static final String SINGAPORE = "https://api.lazada.sg/rest";
  private static final String API_NAME = "/marketing/rta/adrequest";

  private ConcurrentHashMap<String, LazopClient> clientMap = new ConcurrentHashMap<>();

  /**
   * @param rtaInfo
   * @param adList
   * @param country
   * @param gaid
   * @param rtaResMap
   */
  public void requestRta(RtaInfo rtaInfo,
                          List<AdDTO> adList,
                          String country,
                          String gaid,
                          Map<Integer, Target> rtaResMap) {


    Integer advMemberId = rtaInfo.getAdvId();
    String advAppKey = rtaInfo.getAppKey();
    String advAppSecret = rtaInfo.getAppSecret();

    // advCampaignId,List<CampaignId>
    Map<Integer, List<Integer>> advCampaignId2CampaignIdList = //
      adList.stream()
            .map(AdDTO::getCampaignRtaInfo)
            .collect(Collectors.groupingBy(CampaignRtaInfo::getAdvCampaignId,
                                           Collectors.mapping(CampaignRtaInfo::getCampaignId,
                                                              Collectors.toList())));

    StringBuilder sb = new StringBuilder();
    sb.append("[");
    advCampaignId2CampaignIdList.keySet().forEach(advCampaignId -> {
      sb.append("\"").append(advCampaignId).append("\"").append(",");
    });
    sb.delete(sb.length() - 1, sb.length());
    sb.append("]");
    String campaignIdStringList = sb.toString();
    LazopResponse response = null;
    try {
      response = requestRta(country,
                            gaid,
                            campaignIdStringList,
                            String.valueOf(advMemberId),
                            advAppKey,
                            advAppSecret);
    } catch (Exception e) {
      log.error("query rta catch exception", e);
    }
    if (response != null && LazadaCode.SUCCESS.equalsIgnoreCase(response.getCode())) {
      ResponseDTO responseDTO = JsonHelper.parseObject(response.getBody(), ResponseDTO.class);
      String dtoCode = responseDTO.getCode();
      if (LazadaCode.SUCCESS.equalsIgnoreCase(dtoCode)) {
        LazadaRtaData data = responseDTO.getData();
        List<LazadaTarget> targetList = data.getTargetList();
        targetList.forEach(i -> {
          Target target = new Target();
          if (i.isTarget()) {
            target.setTarget(true);
            target.setToken(data.getToken());
          } else {
            target.setTarget(false);
          }
          String advCampaignId = i.getAdvCampaignId();
          List<Integer> campaignIdList =
            advCampaignId2CampaignIdList.get(Integer.valueOf(advCampaignId));
          campaignIdList.forEach(campaignId -> {
            rtaResMap.put(campaignId, target);
          });
        });
      }
    }
  }


  private LazopResponse requestRta(String country,
                                  String gaid,
                                  String campaignIdList,
                                  String advMemberId,
                                  String appKey,
                                  String appSecret) throws ApiException {
    LazopClient client = getClient(advMemberId, appKey, appSecret);
    LazopRequest request = new LazopRequest(API_NAME);
    request.setTimestamp(new Date().getTime());
    request.addApiParameter("country", country);
    request.addApiParameter("gaid", gaid);
    request.addApiParameter("member_id", advMemberId);
    request.addApiParameter("campaign_id_list", campaignIdList);
    LazopResponse response = client.execute(request);
    return response;
  }

  private LazopClient getClient(String advMemberId, String appKey, String appSecret) {

    return clientMap.computeIfAbsent(advMemberId,
                                     key -> new LazopClient(SINGAPORE, appKey, appSecret));
  }
}
