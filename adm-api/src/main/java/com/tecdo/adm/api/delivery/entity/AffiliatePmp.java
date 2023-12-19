package com.tecdo.adm.api.delivery.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tecdo.starter.mp.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 渠道-deals表
 * Created by Elwin on 2023/12/11
 */
@Data
@TableName("affiliate_pmp")
@EqualsAndHashCode(callSuper = true)
@ApiModel("AffiliatePmp对象")
public class AffiliatePmp extends BaseEntity {
    @ApiModelProperty("渠道id")
    private Integer affiliateId;
    @ApiModelProperty("交易标识")
    private String dealId;
    @ApiModelProperty("私有包描述")
    private String name;
}
