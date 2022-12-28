package com.tecdo.domain.request;

import com.tecdo.domain.base.Extension;
import lombok.Getter;
import lombok.Setter;

/**
 * 度量指标，帮助你深入了解展示，从而辅助你做出决策，如近期平均可见性、CTR等
 * 每个指标都由其类型标识，报告指标的值，并可选地标识测量值的来源或供应商
 *
 * Created by Zeki on 2022/12/22
 **/
@Setter
@Getter
public class Metric extends Extension {

    /**
     * 使用 ADX 策划的字符串名称呈现的度量类型，应该预先发布给竞价者（必须）
     */
    private String type;

    /**
     * 表示度量值的数字。概率必须在 0.0～1.0 范围内（必须）
     */
    private float value;

    /**
     * 来源，应该事先向竞争者公布。如果 ADX 本身是源，而不是第三方，建议使用“EXCHANGE”。（推荐）
     */
    private String vendor;
}
