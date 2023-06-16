package com.tecdo.adm.api.log.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Created by Zeki on 2023/6/14
 */
@Getter
@AllArgsConstructor
public enum OptTypeEnum {

//    INSERT(1, "Created"),
    UPDATE(2, "Edit"),
    DELETE(3, "Delete");
//    SELECT(4, "Query");

    private final Integer type;
    private final String desc;

    public static OptTypeEnum of(int type) {
        return Arrays.stream(OptTypeEnum.values()).filter(e -> e.type == type).findFirst().orElse(null);
    }
}
