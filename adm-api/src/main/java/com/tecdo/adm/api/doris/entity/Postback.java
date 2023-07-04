package com.tecdo.adm.api.doris.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by Zeki on 2023/7/4
 */
@Data
@TableName("pac_dsp_postback")
public class Postback implements Serializable {

    /**
     * 创建时间，如 2023-04-23_03
     */
    private String createTime;

    private String campaignId;
    private String adGroupId;

    private Long event1Count;
    private Long event2Count;
    private Long event3Count;

}