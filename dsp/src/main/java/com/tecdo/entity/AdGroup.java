package com.tecdo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tecdo.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 广告组信息表
 * <p>
 * Created by Zeki on 2022/12/26
 **/
@Data
@TableName("ad_group")
@EqualsAndHashCode(callSuper = true)
public class AdGroup extends BaseEntity {

    /**
     * campaign id
     */
    private Integer campaignId;

    /**
     * 广告组名
     */
    private String name;

    /**
     * 点击URL
     */
    private String clickUrl;

    /**
     * 深度连接
     */
    private String deeplink;

    /**
     * 上报的展示追踪链集
     */
    private String impTrackUrls;

    /**
     * 上报的点击追踪链集
     */
    private String clickTrackUrls;

    /**
     * 日预算
     */
    private Double dailyBudget;

    /**
     * 竞价策略
     */
    private Double bidStrategy;

    /**
     * 操作价格
     */
    private Double optPrice;

    /**
     * 状态
     */
    private Integer status;
}