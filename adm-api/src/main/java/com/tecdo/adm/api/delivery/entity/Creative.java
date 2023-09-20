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
 * 创意素材表
 * <p>
 * Created by Zeki on 2022/12/26
 **/
@Data
@TableName("creative")
@EqualsAndHashCode(callSuper = true)
@ApiModel("Creative对象")
public class Creative extends BaseEntity {

    @ApiModelProperty("素材名称")
    private String name;
    @ApiModelProperty(value = "素材类型", notes = "CreativeTypeEnum")
    private Integer type;
    @ApiModelProperty("素材宽度")
    private Integer width;
    @ApiModelProperty("素材高度")
    private Integer height;
    @ApiModelProperty("素材URL")
    private String url;
    @ApiModelProperty("素材后缀")
    private String suffix;
    @ApiModelProperty("音频/视频时长")
    private Integer duration;
    @ApiModelProperty(value = "素材IAB类别", notes = "ContentCategoryEnum")
    private String catIab;
    @ApiModelProperty(value = "素材品牌", notes = "Creative brand")
    private String brand;
    @ApiModelProperty(value = "状态", notes = "BaseStatusEnum")
    @TableField(fill = FieldFill.INSERT)
    private Integer status;

}