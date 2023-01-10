package com.tecdo.filter;

import cn.hutool.core.util.StrUtil;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Content;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.entity.TargetCondition;
import com.tecdo.filter.util.ConditionUtil;
import org.springframework.stereotype.Component;

/**
 * 语言 过滤
 *
 * Created by Zeki on 2023/1/3
 **/
@Component
public class ContentLangFilter extends AbstractRecallFilter {

    private static final String LANGUAGE_ATTR = "content_language";

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTO adDTO) {
        TargetCondition condition = adDTO.getConditions().stream().filter(e -> LANGUAGE_ATTR.equals(e.getAttribute())).findFirst().orElse(null);
        if (condition == null) {
            return true;
        }
        Content content = bidRequest.getApp().getContent();
        if (content == null || StrUtil.isBlank(content.getLanguage())) {
            return false;
        }
        return ConditionUtil.compare(content.getLanguage(), condition.getOperation(), condition.getValue());
    }
}
