package com.tecdo.filter;

import cn.hutool.core.util.StrUtil;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.*;
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
        Site site = bidRequest.getSite();
        App app = bidRequest.getApp();
        if ((site == null && app == null) || (site != null && app != null)) {
            return false;
        }
        Content content = site != null ? site.getContent() : app.getContent();
        if (StrUtil.isBlank(content.getLanguage())) {
            return false;
        }
        return ConditionUtil.compare(content.getLanguage(), condition.getOperation(), condition.getValue());
    }
}
