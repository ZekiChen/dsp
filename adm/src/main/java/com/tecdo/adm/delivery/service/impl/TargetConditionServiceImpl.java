package com.tecdo.adm.delivery.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.adm.api.delivery.mapper.TargetConditionMapper;
import com.tecdo.adm.delivery.service.ITargetConditionService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Zeki on 2023/3/6
 */
@Service
public class TargetConditionServiceImpl extends ServiceImpl<TargetConditionMapper, TargetCondition> implements ITargetConditionService {

    @Override
    public void deleteByAdGroupIds(List<Integer> adGroupIds) {
        baseMapper.delete(Wrappers.<TargetCondition>lambdaQuery().in(TargetCondition::getAdGroupId, adGroupIds));
    }

    @Override
    public List<TargetCondition> listCondition(Integer adGroupId) {
        return baseMapper.selectList(Wrappers.<TargetCondition>lambdaQuery().in(TargetCondition::getAdGroupId, adGroupId));
    }
}
