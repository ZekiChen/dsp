package com.tecdo.adm.api.delivery.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tecdo.starter.mp.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 多阶段竞价策略表
 * <p>
 * Created by Elwin on 2023/11/2
 **/
@Data
@TableName("multi_bid_strategy")
@EqualsAndHashCode(callSuper = true)
@ApiModel("MultiBidStrategy对象")
public class MultiBidStrategy extends BaseEntity {
    @ApiModelProperty(value = "adGroupId")
    private Integer adGroupId;
    @ApiModelProperty(value = "策略阶段", notes = "MultiBidStateEnum")
    private Integer stage;
    @ApiModelProperty(value = "竞价策略", notes = "BidStrategyEnum")
    private Integer bidStrategy;
    @ApiModelProperty("操作价格")
    private Double optPrice;
    @ApiModelProperty("底价乘数,只在bidStrategy=4时生效")
    private Double bidMultiplier;
    @ApiModelProperty("竞价概率,只在bidStrategy=4时生效")
    private Double bidProbability;
    @ApiModelProperty("是否开启bundle测试期,1为开启，0为不开启")
    private Boolean bundleTestEnable;
    @ApiModelProperty("出价算法优化")
    private String bidAlgorithm;

    @ApiModelProperty("曝光条件")
    private Integer impCond;
    @ApiModelProperty("点击条件")
    private Integer clickCond;
    @ApiModelProperty("花费条件")
    private Double costCond;
}
