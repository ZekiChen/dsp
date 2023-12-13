package com.tecdo.domain.biz.validate;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 欺诈信息
 *
 * Created by Zeki on 2023/12/8
 */
@Getter
@AllArgsConstructor
public class FraudInfo {

    private String type;
    private Double probability;
    private boolean isFilter;
}
