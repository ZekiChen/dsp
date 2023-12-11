package com.tecdo.util;

import cn.hutool.core.util.StrUtil;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.Deal;
import com.tecdo.domain.openrtb.request.Imp;

import java.util.Arrays;
import java.util.List;

import static com.tecdo.adm.api.delivery.enums.ConditionEnum.DEALS;

/**
 * Created by Elwin on 2023/12/8
 */
public class PmpHelper {
    /**
     * 获取匹配上的deals中bidfloor的最小值
     * @param adDTO 广告
     * @param imp 展示位
     * @param defaultValue 默认值
     * @return bidfloor
     */
    public static Float getBidfloor(AdDTO adDTO, Imp imp, Float defaultValue) {
        TargetCondition pmpCond = adDTO.getConditionMap().get(DEALS.getDesc());

        Float bidfloor = Float.MAX_VALUE;
        List<String> deals = Arrays.asList(pmpCond.getValue().split(","));

        /*
         * 通过遍历publisher提供的deals
         * 探索deals中最小bidfloor
         */
        for (Deal deal : imp.getPmp().getDeals()) {
            // deal命中定向条件
            if (deals.contains(deal.getId())) {
                bidfloor = Math.min(bidfloor, deal.getBidfloor());
            }
        }

        // 若没有命中定向条件则返回defaultValue
        return Float.compare(bidfloor, Float.MAX_VALUE) < 0 ? bidfloor : defaultValue;
    }

    /**
     * 目标ad是否存在deal定向条件
     * @param adDTO ad
     * @return true or false
     */
    public static boolean hasDealCond(AdDTO adDTO) {
        TargetCondition pmpCond = adDTO.getConditionMap().get(DEALS.getDesc());
        return pmpCond != null && StrUtil.isNotBlank(pmpCond.getValue());
    }
}
