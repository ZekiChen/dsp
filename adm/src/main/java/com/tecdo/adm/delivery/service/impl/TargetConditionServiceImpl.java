package com.tecdo.adm.delivery.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
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
    public boolean deleteByAdGroupIds(List<Integer> adGroupId) {
        Wrapper<TargetCondition> wrapper = Wrappers.<TargetCondition>lambdaQuery().in(TargetCondition::getAdGroupId, adGroupId);
        return baseMapper.delete(wrapper) > 0;
    }
}
