package com.tecdo.adm.delivery.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tecdo.adm.api.delivery.entity.TargetCondition;

import java.util.List;

/**
 * Created by Zeki on 2023/3/6
 */
public interface ITargetConditionService extends IService<TargetCondition> {

    void deleteByAdGroupIds(List<Integer> adGroupIds);

    List<TargetCondition> listCondition(Integer adGroupId);
}
