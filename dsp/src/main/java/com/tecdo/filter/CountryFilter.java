package com.tecdo.filter;

import cn.hutool.core.util.StrUtil;
import com.tecdo.domain.request.BidRequest;
import com.tecdo.domain.request.Imp;
import com.tecdo.domain.request.User;
import com.tecdo.entity.TargetCondition;
import com.tecdo.filter.util.ConditionCompareUtil;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by Zeki on 2023/1/3
 **/
@Component
public class CountryFilter extends AbstractRecallFilter {

    private static final String COUNTRY_ATTR = "country";

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, List<TargetCondition> conditions) {
        TargetCondition condition = conditions.stream().filter(e -> COUNTRY_ATTR.equals(e.getAttribute())).findFirst().orElse(null);
        // 该 AD 不存在需要当前 filter 处理的条件，即该 AD 对目标国家没有要求
        if (condition == null) {
            return true;
        }
        // 该 AD 对目标国家有要求：
        // 1. 但是 request 中没有传递 country 信息，不召回
        User user = bidRequest.getUser();
        if (user == null || user.getGeo() == null || StrUtil.isBlank(user.getGeo().getCountry())) {
            return false;
        }
        // 2. request 中有传递 country，进行匹配校验
        return ConditionCompareUtil.compare(user.getGeo().getCountry(), condition.getOperation(), condition.getValue());
    }
}
