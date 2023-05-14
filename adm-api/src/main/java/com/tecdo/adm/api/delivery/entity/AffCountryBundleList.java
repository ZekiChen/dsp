package com.tecdo.adm.api.delivery.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tecdo.starter.mp.entity.IdEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 渠道*国家*bundle黑白名单表
 *
 * Created by Zeki on 2023/5/11
 */
@Data
@TableName("aff_country_bundle_list")
@ApiModel(value = "AffCountryBundleList对象")
public class AffCountryBundleList extends IdEntity {

    @ApiModelProperty("渠道ID")
    @NotNull
    private Integer affiliateId;
    @ApiModelProperty("国家三位码")
    @NotBlank
    private String country;
    @ApiModelProperty(value = "操作符", example = "include/exclude")
    @NotBlank
    private String operation;
    @ApiModelProperty("包名，多个逗号分割")
    private String bundle;

}