package com.tecdo.adm.api.doris.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 花费实时数据
 *
 * Created by Zeki on 2023/2/21
 */
@Data
@TableName("imp_cost")
public class AdGroupCost implements Serializable {

    /**
     * 广告活动 id
     */
    private String campaignId;

    /**
     * 广告组 id
     */
    private String adGroupId;

    /**
     * 花费金额
     */
    private Double sumSuccessPrice;

    /**
     * 创建时间
     */
    private Date createDate;
}