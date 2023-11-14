package com.tecdo.adm.delivery.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tecdo.adm.api.delivery.entity.MultiBidStrategy;

import java.util.List;

/**
 * Created by Elwin on 2023/11/3
 */
public interface IMultiBidStrategyService extends IService<MultiBidStrategy> {
    List<MultiBidStrategy> listByAdGroupId(List<Integer> adGroupId);
    void deleteByAdGroupIds(List<Integer> adGroupIds);
    void insertOrUpdate(List<MultiBidStrategy> strategies);
}
