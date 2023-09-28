package com.tecdo.job.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.tecdo.adm.api.doris.entity.AdGroupCost;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 获取campaign, ad_group当日花费的表
 *
 * Created by Elwin on 2023/9/26
 */
@DS("doris-ads")
public interface ImpCostMapper {
    /**
     * 查询当天ad_group花费（0时区）
     * @return AdGroupCost类对象
     */
    @Select("SELECT * FROM imp_cost WHERE to_days(create_date) = to_days(now())")
    public List<AdGroupCost> listByGroup();
}
