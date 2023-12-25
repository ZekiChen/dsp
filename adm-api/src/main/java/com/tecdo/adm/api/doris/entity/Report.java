package com.tecdo.adm.api.doris.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 点击实时数据
 *
 * Created by Zeki on 2023/2/21
 */
@Data
@TableName("pac_dsp_report")
public class Report implements Serializable {

    private Integer campaignId;
    private Integer affiliateId;
    private String bundle;

    private Long impCount;
    private Long clickCount;
    private Double impSuccessPriceTotal;
    private Double ctr;
}