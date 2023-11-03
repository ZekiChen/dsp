package com.tecdo.adm.delivery.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.delivery.entity.MultiBidStrategy;
import com.tecdo.adm.api.delivery.mapper.MultiBidStrategyMapper;
import com.tecdo.adm.delivery.service.IMultiBidStrategyService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Elwin on 2023/11/3
 */
@Service
public class IMultiBidStrategyServiceImpl extends ServiceImpl<MultiBidStrategyMapper, MultiBidStrategy> implements IMultiBidStrategyService {
    @Override
    public List<MultiBidStrategy> listByAdGroupId(List<Integer> adGroupId) {
        return baseMapper.selectList(Wrappers.<MultiBidStrategy>lambdaQuery().in(MultiBidStrategy::getAdGroupId, adGroupId));
    }

    @Override
    public void deleteByAdGroupIds(List<Integer> adGroupIds) {
        baseMapper.delete(Wrappers.<MultiBidStrategy>lambdaQuery().in(MultiBidStrategy::getAdGroupId, adGroupIds));
    }
}
