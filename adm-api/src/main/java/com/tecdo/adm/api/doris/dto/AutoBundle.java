package com.tecdo.adm.api.doris.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 自动拉黑业务背景下的Bundle数据流
 * Created by Elwin on 2023/10/25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AutoBundle {
    private String bundleId;
    private Integer adGroupId;
    // 总曝光量
    private Long impCount = 0L;
    // 总点击量
    private Long clickCount = 0L;
    // 近五天bidPriceTotal（前一天开始）
    private Double bidPriceTotal = 0D;
    // 近五天adEstimatedCommission（前一天开始）
    private Double adEstimatedCommission = 0D;
}
