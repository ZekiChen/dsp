package com.tecdo.filter;

import cn.hutool.core.util.StrUtil;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.adm.api.delivery.enums.AdTypeEnum;
import com.tecdo.adm.api.delivery.enums.ConditionEnum;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.enums.openrtb.AdPositionEnum;
import com.tecdo.filter.util.ConditionHelper;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 广告位置过滤
 * @see AdPositionEnum
 *
 * Created by Zeki on 2023/9/25
 */
@Component
public class AdPositionFilter extends AbstractRecallFilter {

    private static final String ATTRIBUTE = ConditionEnum.AD_POSITION.getDesc();

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTO adDTO, Affiliate affiliate) {
        TargetCondition condition = adDTO.getConditionMap().get(ATTRIBUTE);
        if (condition == null) {
            return true;
        }
        AdTypeEnum curAdTypeEnum = AdTypeEnum.of(adDTO.getAd().getType());
        Integer pos;
        switch (curAdTypeEnum) {
            case BANNER:
                pos = imp.getBanner().getPos();
                break;
            case VIDEO:
                pos = imp.getVideo().getPos();
                break;
            default:
                return true;
        }
        if (pos == Integer.parseInt(AdPositionEnum.UNKNOWN.getValue())
                || pos == Integer.parseInt(AdPositionEnum.DEPRECATED.getValue())
                || pos > Integer.parseInt(AdPositionEnum.FULL_SCREEN.getValue())) {
            return Arrays.stream(condition.getValue().split(StrUtil.COMMA))
                    .anyMatch(i -> i.equals(AdPositionEnum.OTHER.getValue()));
        }
        return ConditionHelper.compare(pos.toString(), condition.getOperation(), condition.getValue());
    }
}
