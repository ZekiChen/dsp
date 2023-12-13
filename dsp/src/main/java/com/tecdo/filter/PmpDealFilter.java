package com.tecdo.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.entity.AffiliatePmp;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Deal;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.service.init.AffiliatePmpManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.tecdo.adm.api.delivery.enums.ConditionEnum.DEALS;

/**
 * 私有交易市场，按deals过滤
 * Created by Elwin on 2023/12/8
 */
@Component
@RequiredArgsConstructor
public class PmpDealFilter extends AbstractRecallFilter{
    private final AffiliatePmpManager pmpManager;
    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTO adDTO, Affiliate affiliate) {
        TargetCondition dealsCond = adDTO.getConditionMap().get(DEALS.getDesc());

        boolean hasDealsCond = dealsCond != null && StrUtil.isNotBlank(dealsCond.getValue());
        boolean hasPmp = imp.getPmp() != null && CollUtil.isNotEmpty(imp.getPmp().getDeals());
        /*
         * cond && pmp -> 开始判断匹配情况
         * !cond && !pmp -> return true(filter不工作)
         * cond && !pmp -> return false(包含deals定向，则不接受非pmp请求)
         * !cond && pmp -> return false(不包含deals定向，则不接受pmp请求)
         */
        if (!(hasDealsCond && hasPmp)) return !hasDealsCond && !hasPmp;

        String[] condDeals = dealsCond.getValue().split(",");
        List<String> pmpDeals = imp.getPmp().getDeals()
                .stream()
                .map(Deal::getId)
                .collect(Collectors.toList());

        // return 是否imp和condition中的deals有交集
        for (String deal : condDeals) {
            AffiliatePmp affiliatePmp = pmpManager.getAffiliatePmp(Integer.valueOf(deal));
            // affiliate * deal维度相等
            if (Objects.equals(affiliatePmp.getAffiliateId(), affiliate.getId()) && pmpDeals.contains(affiliatePmp.getDealId())) {
                return true;
            }
        }

        return false;
    }
}
