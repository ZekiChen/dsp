package com.tecdo.adm.api.delivery.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tecdo.starter.mp.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * RTA信息表
 * <p>
 * Created by Zeki on 2022/12/26
 **/
@Data
@TableName("rta_info")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "RtaInfo对象", description = "RtaInfo对象")
public class RtaInfo extends BaseEntity {

    @ApiModelProperty("广告主ID")
    private Integer advId;
    @ApiModelProperty("app key")
    private String appKey;
    @ApiModelProperty("app secret")
    private String appSecret;

}