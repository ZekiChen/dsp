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
 * 广告主信息表
 *
 * Created by Zeki on 2023/4/4
 */
@Data
@TableName("adv")
@EqualsAndHashCode(callSuper = true)
@ApiModel("Adv对象")
public class Adv extends BaseEntity {

    @ApiModelProperty("广告主名称")
    private String name;

    @ApiModelProperty(value = "状态", notes = "BaseStatusEnum")
    @TableField(fill = FieldFill.INSERT)
    private Integer status;

}