package com.tecdo.filter;

import cn.hutool.core.util.StrUtil;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.filter.util.ConditionHelper;
import org.springframework.stereotype.Component;

/**
 * 语言 过滤
 * <p>
 * Created by Zeki on 2023/1/3
 **/
@Component
public class DeviceLangFilter extends AbstractRecallFilter {

    private static final String LANGUAGE_ATTR = "device_lang";

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTO adDTO, Affiliate affiliate) {
        TargetCondition condition = adDTO.getConditions().stream().filter(e -> LANGUAGE_ATTR.equals(e.getAttribute())).findFirst().orElse(null);
        if (condition == null) {
            return true;
        }
        if (StrUtil.isBlank(bidRequest.getDevice().getLanguage())) {
            return false;
        }
        return ConditionHelper.compare(bidRequest.getDevice().getLanguage(), condition.getOperation(), condition.getValue());
    }
}
