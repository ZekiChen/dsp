package com.tecdo.filter;

import cn.hutool.core.util.StrUtil;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Deal;
import com.tecdo.domain.openrtb.request.Imp;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static com.tecdo.adm.api.delivery.enums.ConditionEnum.DEALS;

/**
 * 私有交易市场，按deals过滤
 * Created by Elwin on 2023/12/8
 */
@Component
public class PmpDealFilter extends AbstractRecallFilter{
    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTO adDTO, Affiliate affiliate) {
        TargetCondition dealsCond = adDTO.getConditionMap().get(DEALS.getDesc());

        // 若无定向deals则不工作
        if (dealsCond == null || StrUtil.isBlank(dealsCond.getValue())) {
            return true;
        }

        // 若imp和ad中的deals有交集，则返回true，否则返回false
        List<String> deals = Arrays.asList(dealsCond.getValue().split(","));
        for (Deal deal : imp.getPmp().getDeals()) {
            if (deals.contains(deal.getId())) {
                return true;
            }
        }

        return false;
    }
}
