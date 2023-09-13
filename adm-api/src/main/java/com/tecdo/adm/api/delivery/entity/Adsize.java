package com.tecdo.adm.api.delivery.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tecdo.starter.mp.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 广告标准规格表
 *
 * Created by Elwin on 2023/9/12
 */
@Data
@TableName("adsize")
@EqualsAndHashCode(callSuper = true)
@ApiModel("AdSize对象")
public class Adsize extends BaseEntity {
    @ApiModelProperty("素材宽度")
    private Integer width;
    @ApiModelProperty("素材高度")
    private Integer height;
}
