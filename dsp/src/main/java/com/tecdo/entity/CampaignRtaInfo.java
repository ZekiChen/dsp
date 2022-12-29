package com.tecdo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tecdo.entity.base.BaseEntity;
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
public class CampaignRtaInfo extends BaseEntity {

    /**
     * campaign表id
     */
    private Integer campaignId;

    /**
     * 广告主 campaign id
     */
    private Integer advCampaignId;

    /**
     * 广告主id
     */
    private Integer advId;

    /**
     * 目标受众
     */
    private String rtaFuture;

}