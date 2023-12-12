package com.tecdo.service;

import cn.hutool.core.util.StrUtil;
import com.tecdo.adm.api.delivery.entity.AffiliatePmp;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.Deal;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.service.init.AffiliatePmpManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.tecdo.adm.api.delivery.enums.ConditionEnum.DEALS;

/**
 * Created by Elwin on 2023/12/12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PmpService {
    private final AffiliatePmpManager pmpManager;

    /**
     * 获取pmp背景下的底价
     * @param adDTO adDTO
     * @param imp imp
     * @param defaultValue imp的默认底价
     * @return deals交集底价，若无则返回defaultValue
     */
    public Float getBidfloor(AdDTO adDTO, Imp imp, Integer affiliateId, Float defaultValue) {
        TargetCondition pmpCond = adDTO.getConditionMap().get(DEALS.getDesc());
        Float bidfloor = Float.MAX_VALUE;
        List<String> condDeals = Arrays.asList(pmpCond.getValue().split(",")); // affiliate_pmp表id
        Map<String, Deal> pmpDealMap = imp.getPmp().getDeals().stream()
                .collect(Collectors.toMap(Deal::getId, deal -> deal));

        /*
         * 通过遍历ad的定向deals
         * 探索deals中匹配到的最小bidfloor
         */
        for (String condDeal : condDeals) {
            AffiliatePmp affiliatePmp = pmpManager.getAffiliatePmp(Integer.valueOf(condDeal));
            // affiliate * deal维度相等
            if (Objects.equals(affiliatePmp.getAffiliateId(), affiliateId) && pmpDealMap.containsKey(affiliatePmp.getDealId())) {
                Float tmp_bid = pmpDealMap.get(affiliatePmp.getDealId()).getBidfloor();
                bidfloor = Math.min(bidfloor, tmp_bid);
            }
        }

        // 若没有命中定向条件则返回defaultValue
        return Float.compare(bidfloor, Float.MAX_VALUE) < 0 ? bidfloor : defaultValue;
    }

    /**
     * 目标ad是否存在deal定向条件
     * @param adDTO ad
     * @return true or false
     */
    public boolean hasDealCond(AdDTO adDTO) {
        TargetCondition pmpCond = adDTO.getConditionMap().get(DEALS.getDesc());
        return pmpCond != null && StrUtil.isNotBlank(pmpCond.getValue());
    }
}
