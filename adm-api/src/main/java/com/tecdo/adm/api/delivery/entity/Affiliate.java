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
 * 渠道信息表
 * <p>
 * Created by Zeki on 2022/12/26
 **/
@Data
@TableName("affiliate")
@EqualsAndHashCode(callSuper = true)
@ApiModel("Affiliate对象")
public class Affiliate extends BaseEntity {

    @ApiModelProperty("渠道名称")
    private String name;
    @ApiModelProperty("身份token")
    private String secret;
    @ApiModelProperty("竞价协议")
    private String api;
    @ApiModelProperty(value = "状态", notes = "BaseStatusEnum")
    @TableField(fill = FieldFill.INSERT)
    private Integer status;
    @ApiModelProperty("测试期胜率要求")
    private Double requireWinRate;
}