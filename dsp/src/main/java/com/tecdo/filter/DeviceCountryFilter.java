package com.tecdo.filter;

import cn.hutool.core.util.StrUtil;
import com.tecdo.adm.api.delivery.enums.ConditionEnum;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.biz.dto.AdDTOWrapper;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Device;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.filter.util.ConditionHelper;
import org.springframework.stereotype.Component;

/**
 * 国家 过滤
 *
 * Created by Zeki on 2023/1/3
 **/
@Component
public class DeviceCountryFilter extends AbstractRecallFilter {

    private static final String ATTRIBUTE = ConditionEnum.DEVICE_COUNTRY.getDesc();

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTOWrapper adDTOWrapper, Affiliate affiliate) {
        AdDTO adDTO = adDTOWrapper.getAdDTO();
        TargetCondition condition = adDTO.getConditionMap().get(ATTRIBUTE);
        if (condition == null) {
            return true;
        }
        Device device = bidRequest.getDevice();
        if (device.getGeo() == null || StrUtil.isBlank(device.getGeo().getCountry())) {
            return false;
        }
        return ConditionHelper.compare(device.getGeo().getCountry(), condition.getOperation(), condition.getValue());
    }
}
