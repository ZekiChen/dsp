package com.tecdo.filter;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.adm.api.delivery.enums.ConditionEnum;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.filter.util.ConditionHelper;
import com.tecdo.util.ExtHelper;
import org.springframework.stereotype.Component;

/**
 * schain 过滤，用于追溯ADX来源
 *
 * Created by Zeki on 2023/10/23
 */
@Component
public class SChainFilter extends AbstractRecallFilter {

    private static final String ATTRIBUTE = ConditionEnum.SCHAIN.getDesc();

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTO adDTO, Affiliate affiliate) {
        TargetCondition condition = adDTO.getConditionMap().get(ATTRIBUTE);
        if (condition == null) {
            return true;
        }
        String sChain = ExtHelper.listSChain(bidRequest.getSource());
        if (StrUtil.isBlank(sChain)) {
            return true;
        }
        String[] sChains = sChain.split(StrUtil.COMMA);
        if (NumberUtil.isNumber(condition.getValue())) {  // 代理层数过滤，如过滤超过2层代理的流量
            String layer = String.valueOf(sChains.length);
            return ConditionHelper.compare(layer, condition.getOperation(), condition.getValue());
        } else {  // 代理名称过滤，只投放包含/不包含特定代理的流量
            return ConditionHelper.compare(sChain, condition.getOperation(), condition.getValue());
        }
    }
}
