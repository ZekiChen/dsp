package com.tecdo.job.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Created by Elwin on 2023/9/26
 */
public interface BudgetMapper {
    /**
     * 获取花费大于当天预算的ad_group id
     * @param impCost 花费
     * @return 符合条件的id列表
     */
    @Select("SELECT id FROM ad_group WHERE daily_budget <= impCost")
    public List<Integer> getOverBudgetAdGroups(@Param("impCost") double impCost);

    @Select("SELECT id FROM campaign WHERE daily_budget <= impCost")
    public List<Integer> getOverBudgetCampaigns(@Param("impCost") double impCost);
}
