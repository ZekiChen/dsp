package com.tecdo.service.track;

import cn.hutool.core.util.StrUtil;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.biz.dto.AdDTOWrapper;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.util.ParamHelper;
import com.tecdo.util.SignHelper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Zeki on 2023/9/14
 */
@Component
public class ClickTrackBuilder implements ITrackBuilder {

    @Override
    public List<String> build(String sysClickTrack, AdDTOWrapper wrapper, String sign,
                              BidRequest bidRequest, Affiliate affiliate) {
        AdDTO adDTO = wrapper.getAdDTO();
        String clickTrackUrls = adDTO.getAdGroup().getClickTrackUrls();
        List<String> clickTrackList = new ArrayList<>();

        clickTrackList.add(SignHelper.urlAddSign(sysClickTrack, sign));
        if (StrUtil.isNotBlank(clickTrackUrls)) {
            clickTrackList.addAll(Arrays.asList(clickTrackUrls.split(",")));
        }

        return clickTrackList.stream()
                .map(i -> ParamHelper.urlFormat(i, sign, wrapper, bidRequest, affiliate))
                .collect(Collectors.toList());
    }
}
