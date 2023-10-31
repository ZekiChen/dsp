package com.tecdo.adm.api.delivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 广告定向条件 Mapper接口
 *
 * Created by Zeki on 2022/12/26
 **/
public interface TargetConditionMapper extends BaseMapper<TargetCondition> {
    /**
     * 获取处于开启状态且有bundle拉黑定向条件的ad_group_id
     * @param attributes 涉及的定向条件
     * @return 定向条件列表
     */
    List<TargetCondition> blackConditionList(@Param("attributes") List<String> attributes);

    /**
     * 循环批量更新adgroup的bundle黑名单
     * @param blackList 黑名单
     */
    void updateAutoBundleList(@Param("blackList") List<TargetCondition> blackList);
}