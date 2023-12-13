package com.tecdo.adm.api.delivery.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tecdo.starter.mp.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 欺诈类型容忍度
 *
 * Created by Zeki on 2023/12/8
 */
@Data
@TableName("pixalate_fraud_tolerance")
@EqualsAndHashCode(callSuper = true)
@ApiModel("PixalateFraudTolerance对象")
public class PixalateFraudTolerance extends BaseEntity {

    @ApiModelProperty("0-简单，1-复杂")
    private Integer ivtClass;
    @ApiModelProperty("欺诈类型")
    private String fraudType;
    @ApiModelProperty("欺诈类型名称")
    private String fraudTypeName;
    @ApiModelProperty("欺诈类型描述")
    private String description;
    @ApiModelProperty("容忍度")
    private Double probability;
    @ApiModelProperty(value = "状态", notes = "BaseStatusEnum")
    @TableField(fill = FieldFill.INSERT)
    private Integer status;
}