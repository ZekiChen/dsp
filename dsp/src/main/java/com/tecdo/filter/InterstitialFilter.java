package com.tecdo.filter;

import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.adm.api.delivery.enums.AdTypeEnum;
import com.tecdo.adm.api.delivery.enums.ConditionEnum;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.biz.dto.AdDTOWrapper;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.filter.util.ConditionHelper;
import org.springframework.stereotype.Component;

/**
 * 插屏广告过滤
 *
 * Created by Zeki on 2023/9/25
 */
@Component
public class InterstitialFilter extends AbstractRecallFilter {

    private static final String IMAGE_INSTL = ConditionEnum.IMAGE_INSTL.getDesc();
    private static final String VIDEO_INSTL = ConditionEnum.VIDEO_INSTL.getDesc();

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTOWrapper adDTOWrapper, Affiliate affiliate) {
        AdDTO adDTO = adDTOWrapper.getAdDTO();
        TargetCondition imageCond = adDTO.getConditionMap().get(IMAGE_INSTL);
        TargetCondition videoCond = adDTO.getConditionMap().get(VIDEO_INSTL);
        if (imageCond == null && videoCond == null) {
            return true;
        }
        String instl = imp.getInstl().toString();
        switch (AdTypeEnum.of(adDTO.getAd().getType())) {
            case BANNER:
            case NATIVE:
                return imageCond == null
                        || ConditionHelper.compare(instl, imageCond.getOperation(), imageCond.getValue());
            case VIDEO:
                return videoCond == null
                        || ConditionHelper.compare(instl, videoCond.getOperation(), videoCond.getValue());
            default:
                return true;
        }
    }
}
