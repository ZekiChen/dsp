package com.tecdo.filter;

import cn.hutool.core.util.StrUtil;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.adm.api.delivery.enums.AdTypeEnum;
import com.tecdo.adm.api.delivery.enums.ConditionEnum;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.enums.openrtb.VideoPlacementTypeEnum;
import com.tecdo.filter.util.ConditionHelper;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 视频放置类型过滤
 *
 * Created by Zeki on 2023/9/27
 */
@Component
public class VideoPlacementFilter extends AbstractRecallFilter {

    private static final String ATTRIBUTE = ConditionEnum.VIDEO_PLACEMENT.getDesc();

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTO adDTO, Affiliate affiliate) {
        TargetCondition condition = adDTO.getConditionMap().get(ATTRIBUTE);
        if (condition == null) {
            return true;
        }
        switch (AdTypeEnum.of(adDTO.getAd().getType())) {
            case VIDEO:
                Integer placement = imp.getVideo().getPlacement();
                if (placement == null
                        || placement > Integer.parseInt(VideoPlacementTypeEnum.INTERSTITIAL.getValue())) {
                    return Arrays.stream(condition.getValue().split(StrUtil.COMMA))
                            .anyMatch(i -> i.equals(VideoPlacementTypeEnum.OTHER.getValue()));
                }
                return ConditionHelper.compare(placement.toString(), condition.getOperation(), condition.getValue());
            default:
                return true;
        }
    }

}
