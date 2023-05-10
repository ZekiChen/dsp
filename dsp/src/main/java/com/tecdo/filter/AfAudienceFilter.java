package com.tecdo.filter;

import cn.hutool.crypto.digest.DigestUtil;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.adm.api.delivery.enums.ConditionEnum;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author sisyphus.su
 * @description: AF人群包过滤器
 * @date: 2023-04-26 11:02
 **/
@Component
@RequiredArgsConstructor
public class AfAudienceFilter  extends AbstractRecallFilter{
    private static final String AUDIENCE = ConditionEnum.AUDIENCE.getDesc();

    private final CacheService cacheService;

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTO adDTO, Affiliate affiliate) {
        TargetCondition condition = adDTO.getConditions().stream().filter(v-> AUDIENCE.equals(v.getAttribute())).findFirst().orElse(null);
        if (condition == null) {
            return true;
        }
        //匹配布隆过滤器 需要调整成sha256
        String deviceId = new String(DigestUtil.sha256(bidRequest.getDevice().getIfa()));

        String[] containerIdList = condition.getValue().split(",");

        for (String containerId : containerIdList) {
            String key = cacheService.getAudienceSyncBloomFilterKey(Long.valueOf(containerId));
            if (key != null && cacheService.existInBloomFilter(key, deviceId)) {
                return true;
            }
        }

        return false;
    }
}
