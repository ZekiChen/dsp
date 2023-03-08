package com.tecdo.adm.api.delivery.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tecdo.starter.mp.entity.BaseEntity;
import com.tecdo.starter.mp.enums.BaseStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 广告信息表
 * <p>
 * Created by Zeki on 2022/12/26
 **/
@Data
@TableName("ad")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "Ad对象", description = "Ad对象")
public class Ad extends BaseEntity {

    @ApiModelProperty("广告组ID")
    private Integer groupId;
    @ApiModelProperty("广告名称")
    private String name;
    @ApiModelProperty(value = "广告类型", notes = "AdTypeEnum")
    private Integer type;
    @ApiModelProperty("图片 creative id")
    private Integer image;
    @ApiModelProperty("icon creative id")
    private Integer icon;
    @ApiModelProperty("广告标题")
    private String title;
    @ApiModelProperty("描述")
    private String description;
    @ApiModelProperty("行为召唤按钮描述")
    private String cta;
    @ApiModelProperty("视频 creative id")
    private Integer video;
    @ApiModelProperty(value = "状态", notes = "BaseStatusEnum")
    private Integer status = BaseStatusEnum.ACTIVE.getType();

}