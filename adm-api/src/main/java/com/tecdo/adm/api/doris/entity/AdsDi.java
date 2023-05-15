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
@TableName("ads_pac_dsp_di")
public class AdsDi implements Serializable {

    private Integer campaignId;

    private Long impCount;
    private Long clickCount;
    private Double impSuccessPriceTotal;
}