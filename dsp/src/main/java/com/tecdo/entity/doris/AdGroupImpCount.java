package com.tecdo.entity.doris;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 展示实时数据
 *
 * Created by Zeki on 2023/2/21
 */
@Data
@TableName("pac_dsp_imp_count")
public class AdGroupImpCount implements Serializable {

    /**
     * 广告活动 id
     */
    private String campaignId;

    /**
     * 广告组 id
     */
    private String adGroupId;

    /**
     * 展示数量
     */
    private Integer value;

    /**
     * 创建时间
     */
    private Date createDate;
}