package com.tecdo.filter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.tecdo.adm.api.audience.entity.AfContainer;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.adm.api.delivery.enums.ConditionEnum;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.enums.biz.AudienceEncryptEnum;
import com.tecdo.service.CacheService;
import com.tecdo.service.init.AfAudienceSyncManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author sisyphus.su
 * @description: AF人群包过滤器
 * @date: 2023-04-26 11:02
 **/
@Component
@RequiredArgsConstructor
public class AfAudienceFilter  extends AbstractRecallFilter{

    private final AfAudienceSyncManager afAudienceManager;

    private static final String ATTRIBUTE = ConditionEnum.AUDIENCE_AF.getDesc();

    private final CacheService cacheService;

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTO adDTO, Affiliate affiliate) {
        TargetCondition condition = adDTO.getConditionMap().get(ATTRIBUTE);
        if (condition == null) {
            return true;
        }
        String deviceId = bidRequest.getDevice().getIfa();
        String[] containerIdList = condition.getValue().split(",");

        for (String containerId : containerIdList) {
            String key = cacheService.getAudienceCache().getAudienceSyncBloomFilterKey(Integer.valueOf(containerId));
            AfContainer container = afAudienceManager.getAfContainer(Integer.valueOf(containerId));
            if (StrUtil.isBlank(key) || container == null) {
                continue;
            }
            switch (condition.getOperation()) {
                case Constant.INCLUDE:
                    if (existInBloomFilter(key, deviceId, container.getEncrypt())) {
                        return true;
                    }
                    break;
                case Constant.EXCLUDE:
                    if (existInBloomFilter(key, deviceId, container.getEncrypt())) {
                        return false;
                    }
                    break;
                default:
                    break;
            }
        }
        return Constant.EXCLUDE.equals(condition.getOperation());
    }

    private boolean existInBloomFilter(String key, String deviceId, int encrypt) {
        if (encrypt == AudienceEncryptEnum.NO.getCode()) {
            return cacheService.getAudienceCache().existInBloomFilter(key, deviceId);
        } else if (encrypt == AudienceEncryptEnum.SHA256.getCode()) {
            return cacheService.getAudienceCache().existInBloomFilter(key, DigestUtil.sha256Hex(deviceId));
        } else {
            return false;
        }
    }
}
