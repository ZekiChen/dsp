package com.tecdo.adm.api.delivery.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tecdo.starter.mp.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 广告推广活动表
 * <p>
 * Created by Zeki on 2022/12/26
 **/
@Data
@TableName("campaign")
@EqualsAndHashCode(callSuper = true)
@ApiModel("Campaign对象")
public class Campaign extends BaseEntity {

    @ApiModelProperty("广告活动名称")
    private String name;
    @ApiModelProperty("日预算")
    private Double dailyBudget;
    @ApiModelProperty("包名")
    private String packageName;
    @ApiModelProperty("分类（多个用逗号分隔）")
    private String category;
    @ApiModelProperty("推广单子的域名")
    private String domain;
    @ApiModelProperty(value = "状态", notes = "BaseStatusEnum")
    private Integer status;

}