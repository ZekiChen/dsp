package com.tecdo.service;

import cn.hutool.core.collection.CollUtil;
import com.tecdo.adm.api.delivery.entity.AffiliatePmp;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.biz.dto.BidfloorDTO;
import com.tecdo.domain.openrtb.request.Deal;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.service.init.AffiliatePmpManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    public BidfloorDTO getBidfloor(AdDTO adDTO, Imp imp, Integer affiliateId, Float defaultValue) {
        TargetCondition pmpCond = adDTO.getConditionMap().get(DEALS.getDesc());
        float bidfloor = Float.MAX_VALUE;
        String[] condDeals = pmpCond.getValue().split(","); // affiliate_pmp表id
        Map<String, Deal> pmpDealMap = imp.getPmp().getDeals().stream()
                .collect(Collectors.toMap(Deal::getId, deal -> deal));
        String targetDealid = null;

        /*
         * 通过遍历ad的定向deals
         * 探索deals中匹配到的最小bidfloor
         */
        for (String condDeal : condDeals) {
            AffiliatePmp affiliatePmp = pmpManager.getAffiliatePmp(Integer.valueOf(condDeal));
            // affiliate * deal维度相等
            if (Objects.equals(affiliatePmp.getAffiliateId(), affiliateId) && pmpDealMap.containsKey(affiliatePmp.getDealId())) {
                Float tmpBid = pmpDealMap.get(affiliatePmp.getDealId()).getBidfloor();
                bidfloor = Math.min(bidfloor, tmpBid);
                targetDealid = affiliatePmp.getDealId();
            }
        }

        // 若没有命中定向条件则返回defaultValue，且targetDealid为null
        bidfloor = Float.compare(bidfloor, Float.MAX_VALUE) < 0 ? bidfloor : defaultValue;
        return new BidfloorDTO(bidfloor, targetDealid);
    }

    /**
     * 判断imp是否包含pmp对象
     * @param imp 展示位
     * @return imp是否包含pmp对象
     */
    public boolean isPmpRequest(Imp imp) {
        return imp.getPmp() != null && CollUtil.isNotEmpty(imp.getPmp().getDeals());
    }
}
