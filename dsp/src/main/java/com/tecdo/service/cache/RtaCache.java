package com.tecdo.service.cache;

import cn.hutool.core.util.StrUtil;
import com.tecdo.common.constant.CacheConstant;
import com.tecdo.service.rta.ae.AeRtaInfoVO;
import com.tecdo.starter.redis.PacRedis;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * RTA调用 缓存操作
 *
 * Created by Zeki on 2023/4/11
 */
@Service
@RequiredArgsConstructor
public class RtaCache {

    private final static String AE_CACHE = "ae:v2";

    private final PacRedis pacRedis;

    public List<AeRtaInfoVO> getAeRtaResponse(Set<String> advCampaignIds, String deviceId) {
        return advCampaignIds.stream().map(cid -> {
            String key = CacheConstant.RTA_CACHE
                    .concat(StrUtil.COLON).concat(AE_CACHE)
                    .concat(StrUtil.COLON).concat(cid)
                    .concat(StrUtil.COLON).concat(deviceId);
            AeRtaInfoVO vo = pacRedis.get(key);
            if (vo == null) {
                vo = new AeRtaInfoVO();
                vo.setAdvCampaignId(cid);
                vo.setTarget(false);
            }
            return vo;
        }).collect(Collectors.toList());
    }

}
