package com.tecdo.adm.api.doris.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 点击实时数据
 *
 * Created by Zeki on 2023/2/21
 */
@Data
@TableName("pac_dsp_click")
public class AdGroupClick implements Serializable {

    /**
     * 广告活动 id
     */
    private String campaignId;

    /**
     * 广告组 id
     */
    private String adGroupId;

    /**
     * 点击数
     */
    private Long clickCount;

    /**
     * 创建时间
     */
    private Date createDate;
}