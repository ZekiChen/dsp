package com.tecdo.adm.api.delivery.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tecdo.starter.mp.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 广告定向条件表
 * <p>
 * Created by Zeki on 2022/12/26
 **/
@Data
@TableName("target_condition")
@EqualsAndHashCode(callSuper = true)
@ApiModel("TargetCondition对象")
public class TargetCondition extends BaseEntity {

    @ApiModelProperty("广告组ID")
    private Integer adGroupId;
    @ApiModelProperty("召回条件")
    private String attribute;
    @ApiModelProperty("比较符")
    private String operation;
    @ApiModelProperty("条件值")
    private String value;
}