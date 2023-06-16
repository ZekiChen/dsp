package com.tecdo.starter.mp.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 基础实体：主键ID + 状态Status
 *
 * Created by Zeki on 2022/8/24
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class StatusEntity extends IdEntity {

    @ApiModelProperty(value = "状态", notes = "BaseStatusEnum")
    @TableField(fill = FieldFill.INSERT)
    private Integer status;
}