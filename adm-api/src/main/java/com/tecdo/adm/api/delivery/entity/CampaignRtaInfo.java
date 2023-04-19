package com.tecdo.adm.api.delivery.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tecdo.starter.mp.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * campaign RTA信息表
 * <p>
 * Created by Zeki on 2022/12/26
 **/
@Data
@TableName("campaign_rta_info")
@EqualsAndHashCode(callSuper = true)
@ApiModel("CampaignRtaInfo对象")
public class CampaignRtaInfo extends BaseEntity {

    @ApiModelProperty("campaign表id")
    private Integer campaignId;
    @ApiModelProperty("广告主 campaign id")
    private String advCampaignId;
    @ApiModelProperty("广告主id")  // TODO 目前双写，后续删
    private Integer advId;
    @ApiModelProperty("广告主id")
    private Integer advMemId;
    @ApiModelProperty("RTA受众特征")
    private Integer rtaFeature;
    @ApiModelProperty("渠道名称")
    private String channel;

}