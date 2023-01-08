package com.tecdo.filter;

import cn.hutool.core.util.StrUtil;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.*;
import com.tecdo.entity.TargetCondition;
import com.tecdo.filter.util.ConditionUtil;
import org.springframework.stereotype.Component;

/**
 * 目标渠道 过滤
 *
 * Created by Zeki on 2023/1/3
 **/
@Component
public class AffiliateFilter extends AbstractRecallFilter {

    private static final String AFF_ATTR = "affiliate";

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTO adDTO) {
        TargetCondition condition = adDTO.getConditions().stream().filter(e -> AFF_ATTR.equals(e.getAttribute())).findFirst().orElse(null);
        // 该 AD 不存在需要当前 filter 处理的条件，即该 AD 对目标渠道没有要求
        if (condition == null) {
            return true;
        }
        // 该 AD 对目标渠道有要求：
        // 1. 但是 request 中没有传递 site/app 信息，不召回
        // 2. request 中 site 和 app 信息都传递了，不召回，因为同一时间一次 bid 只会有一种客户端
        Site site = bidRequest.getSite();
        App app = bidRequest.getApp();
        if ((site == null && app == null) || (site != null && app != null)) {
            return false;
        }
        Publisher publisher = site != null ? site.getPublisher() : app.getPublisher();
        if (StrUtil.isBlank(publisher.getName())) {
            return false;
        }
        // 3. site/app 中有传递 publisher name，进行匹配校验
        return ConditionUtil.compare(publisher.getName(), condition.getOperation(), condition.getValue());
    }
}
