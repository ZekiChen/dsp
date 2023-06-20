package com.tecdo.adm.api.delivery.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tecdo.starter.mp.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 广告组信息表
 * <p>
 * Created by Zeki on 2022/12/26
 **/
@Data
@TableName("ad_group")
@EqualsAndHashCode(callSuper = true)
@ApiModel("AdGroup对象")
public class AdGroup extends BaseEntity {

    @ApiModelProperty("广告活动ID")
    private Integer campaignId;
    @ApiModelProperty("广告组名")
    private String name;
    @ApiModelProperty("Landing URL")
    private String clickUrl;
    @ApiModelProperty("深度链接")
    private String deeplink;
    @ApiModelProperty("上报的展示追踪链集")
    private String impTrackUrls;
    @ApiModelProperty("上报的点击追踪链集")
    private String clickTrackUrls;
    @ApiModelProperty("日预算")
    private Double dailyBudget;
    @ApiModelProperty(value = "竞价策略", notes = "BidStrategyEnum")
    private Integer bidStrategy;
    @ApiModelProperty("操作价格")
    private Double optPrice;
    @ApiModelProperty("底价乘数,只在bidStrategy=4时生效")
    private Double bidMultiplier;
    @ApiModelProperty("竞价概率,只在bidStrategy=4时生效")
    private Double bidProbability;
    @ApiModelProperty("备注")
    private String remark;
    @ApiModelProperty(value = "状态", notes = "BaseStatusEnum")
    @TableField(fill = FieldFill.INSERT)
    private Integer status;
    @ApiModelProperty("是否开启bundle测试期,1为开启，0为不开启")
    private Boolean bundleTestEnable;
    @ApiModelProperty("强制跳转链接")
    private String forceLink;
}