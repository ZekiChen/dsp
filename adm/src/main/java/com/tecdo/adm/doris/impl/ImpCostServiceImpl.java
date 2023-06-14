package com.tecdo.adm.doris.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.doris.entity.AdGroupCost;
import com.tecdo.adm.api.doris.mapper.AdGroupCostMapper;
import com.tecdo.adm.doris.IImpCostService;
import com.tecdo.starter.tool.util.DateUtil;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Zeki on 2023/4/5
 */
@Service
public class ImpCostServiceImpl extends ServiceImpl<AdGroupCostMapper, AdGroupCost> implements IImpCostService {

    @Override
    public List<AdGroupCost> listByGroupIds(List<Integer> adGroupIds) {
        LambdaQueryWrapper<AdGroupCost> wrapper = Wrappers.<AdGroupCost>lambdaQuery()
                .eq(AdGroupCost::getCreateDate, DateUtil.today())
                .in(AdGroupCost::getAdGroupId, adGroupIds);
        return list(wrapper);
    }

    @Override
    public List<AdGroupCost> listByCampaignIds(List<Integer> campaignIds) {
        LambdaQueryWrapper<AdGroupCost> wrapper = Wrappers.<AdGroupCost>lambdaQuery()
                .eq(AdGroupCost::getCreateDate, DateUtil.today())
                .in(AdGroupCost::getCampaignId, campaignIds);
        return list(wrapper);
    }
}
