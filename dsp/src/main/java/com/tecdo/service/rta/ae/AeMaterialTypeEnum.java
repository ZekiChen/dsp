package com.tecdo.service.rta.ae;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by Zeki on 2023/4/11
 */
@Getter
@RequiredArgsConstructor
public enum AeMaterialTypeEnum {

    DPA("DPA"),
    STATIC("STATIC"),
    INSTALL("INSTALL"),
    ;

    private final String desc;
}