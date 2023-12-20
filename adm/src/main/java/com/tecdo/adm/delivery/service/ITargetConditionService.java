package com.tecdo.adm.delivery.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tecdo.adm.api.delivery.entity.TargetCondition;

import java.util.List;
import java.util.Map;

/**
 * Created by Zeki on 2023/3/6
 */
public interface ITargetConditionService extends IService<TargetCondition> {

    /**
     * 按照adGroup * attribute维度删除
     * @param groupIdToAttrMap adGroupId -> attributes（多个用逗号分割）
     */
    void deleteByAdGroupIdsAndAttr(Map<Integer, List<String>> groupIdToAttrMap);

    /**
     * 插入value非空的condition（value为空认为删除）
     * @param conditionList 待插入的conditionList
     */
    void insertConditionsIfHasVal(List<TargetCondition> conditionList);

    void deleteByAdGroupIds(List<Integer> adGroupIds);

    List<TargetCondition> listCondition(Integer adGroupId);
}
