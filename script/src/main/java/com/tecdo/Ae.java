package com.tecdo;


import com.ejlchina.data.TypeRef;
import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.OkHttps;
import com.google.common.util.concurrent.RateLimiter;
import com.tecdo.core.launch.thread.ThreadPool;
import com.tecdo.entity.CampaignRtaDTO;
import com.tecdo.enums.AeMaterialTypeEnum;
import com.tecdo.mapper.doris.RequestMapper;
import com.tecdo.mapper.mysql.CampaignMapper;
import com.tecdo.service.rta.ae.AeDataVO;
import com.tecdo.service.rta.ae.AeResponse;
import com.tecdo.service.rta.ae.AeRtaInfoVO;
import com.tecdo.service.rta.ae.AeRtaProductInfoVO;
import com.tecdo.starter.redis.PacRedis;
import com.tecdo.util.AeSignHelper;
import com.tecdo.util.JsonHelper;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class Ae {

  private final RequestMapper requestMapper;

  private final CampaignMapper campaignMapper;

  private final PacRedis pacRedis;

  private final ThreadPool threadPool;

  private final static Logger aeRtaResponseLogger = LoggerFactory.getLogger("ae_rta_log");

  private static final String AE_RTA_URL = "https://us.rta.gloadlink.com/rta/v2/request/general";

  private static final String PID_PLACEHOLDER = "_AF_PID_";

  private static final String AE_RTA_CACHE = "pac:dsp:rta:ae:v2";

  private static final String HOUR_FORMAT = "yyyy-MM-dd_HH";

  @Value("${pac.script.ae.cache-expire:86400}")
  private int CACHE_EXPIRE;

  @Value("${pac.script.ae.pid:liquidnet_int}")
  private String PID;

  @Value("${pac.script.ae.batch-size:200}")
  private int batchSize;

  @Value("${pac.script.ae.timeout:3}")
  private int timeout;

  @Value("${pac.script.ae.default-last-time:-3}")
  private int defaultLastTime;

  @Value("${pac.script.ae.max-qps:500}")
  private int maxQps;

  private String[] getStartAndEndTime() {
    Calendar nowTime = Calendar.getInstance();
    String endTime = DateUtil.format(nowTime.getTime(), HOUR_FORMAT);
    nowTime.add(Calendar.DATE, defaultLastTime);
    String startTime = DateUtil.format(nowTime.getTime(), HOUR_FORMAT);
    return new String[] {startTime, endTime};
  }

  private List<String> getTimeRange(String start, String end) {
    DateTime startDate = DateUtil.parse(start, HOUR_FORMAT);
    DateTime endDate = DateUtil.parse(end, HOUR_FORMAT);
    Calendar left = Calendar.getInstance();
    left.setTime(startDate);
    Calendar right = Calendar.getInstance();
    right.setTime(endDate);
    if (left.compareTo(right) >= 0) {
      throw new RuntimeException();
    }
    List<String> timeRange = new ArrayList<>();

    while (left.compareTo(right) <= 0) {
      timeRange.add(DateUtil.format(left.getTime(), HOUR_FORMAT));
      left.add(Calendar.HOUR, 1);
    }

    return timeRange;
  }

  public void run(String... args) {
    String country = "MEX";
    String[] startAndEndTime = getStartAndEndTime();
    String start = startAndEndTime[0];
    String end = startAndEndTime[1];
    DateUtil.format(new Date(), "yyyy-MM-dd_HH");
    if (args != null && args.length > 0) {
      country = args[0];
    }
    if (args != null && args.length == 3) {
      start = args[1];
      end = args[2];
    }
    List<String> timeRange = getTimeRange(start, end);
    RateLimiter rateLimiter = RateLimiter.create(maxQps);


    Set<CampaignRtaDTO> campaignRtaDTOS = campaignMapper.listAdvCampaign();

    for (int cur = 0; cur < timeRange.size() - 1; cur++) {
      try {
        String startTime = timeRange.get(cur);
        String endTime = timeRange.get(cur + 1);
        List<String> deviceIdList = requestMapper.listDeviceId(country, startTime, endTime);
        log.info("{} deviceId size: {}", startTime, deviceIdList.size());
        List<Future> futureList = new ArrayList<>();
        for (String deviceId : deviceIdList) {
          try {
            campaignRtaDTOS.parallelStream()
                           .filter(dto -> !pacRedis.exists(AE_RTA_CACHE.concat(":")
                                                                       .concat(dto.getAdvCampaignId())
                                                                       .concat(":")
                                                                       .concat(deviceId)))
                           .collect(Collectors.groupingBy(CampaignRtaDTO::getChannel,
                                                          Collectors.mapping(CampaignRtaDTO::getAdvCampaignId,
                                                                             Collectors.toList())))
                           // AE限制每次最多只能传 10 个 advCampaignId，因此超过 10 个则分批调用
                           .forEach((channel, advCampaignIds) -> //
                                      IntStream.range(0, (advCampaignIds.size() + 9) / 10)
                                               .mapToObj(i -> advCampaignIds.subList(i * 10,
                                                                                     Math.min(
                                                                                       (i + 1) * 10,
                                                                                       advCampaignIds.size())))
                                               .forEach(list -> {
                                                 futureList.add(threadPool.submit(() -> {
                                                   callAeRtaAndCache(deviceId,
                                                                     list,
                                                                     channel,
                                                                     rateLimiter);
                                                 }));
                                               }));
            if (futureList.size() >= batchSize) {
              for (Future future : futureList) {
                try {
                  future.get(timeout, TimeUnit.SECONDS);
                } catch (Throwable e) {
                  e.printStackTrace();
                }
              }
              futureList.clear();
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
        for (Future future : futureList) {
          try {
            future.get(timeout, TimeUnit.SECONDS);
          } catch (Throwable e) {
            e.printStackTrace();
          }
        }
        log.info("{} finish", startTime);
      } catch (Exception e) {
        cur--;
        log.error(timeRange.get(cur) + "catch exception, retry", e);
      }
    }

  }


  private void callAeRtaAndCache(String deviceId,
                                 List<String> advCampaignIds,
                                 String channel,
                                 RateLimiter rateLimiter) {
    long timestamp = System.currentTimeMillis();
    Map<String, Object> aeRequestParam = new HashMap<>();
    aeRequestParam.put("adid", deviceId);
    aeRequestParam.put("campaignIds", advCampaignIds);
    aeRequestParam.put("channel", channel);
    aeRequestParam.put("timestamp", timestamp);
    aeRequestParam.put("sign", AeSignHelper.getSign(deviceId, channel, timestamp, advCampaignIds));

    rateLimiter.acquire();
    HttpResult result =
      OkHttps.sync(AE_RTA_URL).bodyType(OkHttps.JSON).addBodyPara(aeRequestParam).post();
    if (!result.isSuccessful()) {
      log.error("ae rta response is not successful, status: {}, reason: {}",
                result.getStatus(),
                result.getError());
      return;
    }
    AeResponse<AeDataVO<AeRtaInfoVO>> aeResponse =
      result.getBody().toBean(new TypeRef<AeResponse<AeDataVO<AeRtaInfoVO>>>() {
      });
    if (!aeResponse.succeed() || aeResponse.getData() == null) {
      log.error("ae rta response code is not 0 or data is null, code: {}, message: {}",
                aeResponse.getCode(),
                aeResponse.getMessage());
      return;
    }
    List<AeRtaInfoVO> targetList = aeResponse.getData().getTargetList();
    if (CollectionUtils.isEmpty(targetList)) {
      log.error("ae rta response target list is empty, code: {}, message: {}",
                aeResponse.getCode(),
                aeResponse.getMessage());
      return;
    }
    for (AeRtaInfoVO vo : targetList) {
      if (vo.getTarget()) {
        aeRtaResponseLogger.info(JsonHelper.toJSONString(vo));
      }
      fillPidInLandingPage(vo);
      String cacheKey =
        AE_RTA_CACHE.concat(":").concat(vo.getAdvCampaignId()).concat(":").concat(deviceId);
      pacRedis.setEx(cacheKey, vo, Duration.ofSeconds(CACHE_EXPIRE));
    }
  }

  private void fillPidInLandingPage(AeRtaInfoVO vo) {
    if (StringUtils.isNotBlank(vo.getMaterialType()) && vo.getTarget()) {
      switch (AeMaterialTypeEnum.of(vo.getMaterialType())) {
        case DPA:
          List<AeRtaProductInfoVO> productList = vo.getProductList();
          if (!CollectionUtils.isEmpty(productList)) {
            // 目前我们一个竞价请求对应只出价一个曝光展示位，所以每次都只曝光第一个商品（AE推荐）
            // 因此，这里先只替换第一条 landingPage 的主体宏，提升性能，且存回对象上一层的landingPage
            vo.setLandingPage(productList.get(0).getLandingPage().replace(PID_PLACEHOLDER, PID));
            // 由于其他参数目前我们也没有用到，包括AE返回的素材，因此这部分也暂时不写回缓存
            vo.setProductList(null);
          }
          break;
        case STATIC:
        case INSTALL:
          vo.setLandingPage(vo.getLandingPage().replace(PID_PLACEHOLDER, PID));
          break;
        case UNKNOWN:
          log.error("unknown material type: {}", vo.getMaterialType());
      }
    }
  }
}
