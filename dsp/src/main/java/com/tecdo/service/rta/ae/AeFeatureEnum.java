package com.tecdo.service.rta.ae;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Zeki on 2023/4/26
 */
@Getter
@AllArgsConstructor
public enum AeFeatureEnum {

    HY_V3_vc1(101, "HY V3 vc1"),
    HY_V3_vc0(102, "HY V3 vc0"),
    HY_V3_pur1(103, "HY V3 pur1"),
    HY_V3_pur0(104, "HY V3 pur0"),
    HY_V3_atc1(105, "HY V3 atc1"),
    HY_V3_atc0(106, "HY V3 atc0"),
    ;

    private final int code;
    private final String desc;
}
