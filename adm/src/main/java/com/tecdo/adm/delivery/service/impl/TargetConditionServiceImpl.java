package com.tecdo.adm.delivery.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.adm.api.delivery.mapper.TargetConditionMapper;
import com.tecdo.adm.delivery.service.ITargetConditionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Zeki on 2023/3/6
 */
@Service
public class TargetConditionServiceImpl extends ServiceImpl<TargetConditionMapper, TargetCondition> implements ITargetConditionService {

    /**
     * 按照adGroup * attribute维度删除
     * @param groupIdToAttrMap adGroupId -> attributes（多个用逗号分割）
     */
    @Override
    public void deleteByAdGroupIdsAndAttr(Map<Integer, List<String>> groupIdToAttrMap) {
        for (Integer adGroupId : groupIdToAttrMap.keySet()) {
            baseMapper.delete(Wrappers.<TargetCondition>lambdaQuery()
                    .eq(TargetCondition::getAdGroupId, adGroupId)
                    .in(TargetCondition::getAttribute,groupIdToAttrMap.get(adGroupId)));
        }
    }

    @Override
    public void insertConditionsIfHasVal(List<TargetCondition> conditionList) {
        // 过滤掉value为空串的TargetCondition对象
        List<TargetCondition> filteredList = conditionList.stream()
                .filter(targetCondition -> StrUtil.isNotBlank(targetCondition.getValue()))
                .collect(Collectors.toList());

        // 使用baseMapper.saveBatch保存过滤后的列表
        if (filteredList.isEmpty()) return;
        for (TargetCondition condition : filteredList) {
            baseMapper.insert(condition);
        }
    }

    @Override
    public void deleteByAdGroupIds(List<Integer> adGroupIds) {
        baseMapper.delete(Wrappers.<TargetCondition>lambdaQuery().in(TargetCondition::getAdGroupId, adGroupIds));
    }

    @Override
    public List<TargetCondition> listCondition(Integer adGroupId) {
        return baseMapper.selectList(Wrappers.<TargetCondition>lambdaQuery().in(TargetCondition::getAdGroupId, adGroupId));
    }
}
