package com.tecdo.adm.api.delivery.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 出价算法
 **/
@Getter
@AllArgsConstructor
public enum BidAlgorithmEnum {

    NO("0", "unused"),
    HISTORY_ECPX("1", "Based on historical ECPX bids"),
    LEARNING("2", "Based on reinforcement learning"),
    OTHER("-1", "other");

    private final String type;
    private final String desc;

    public static BidAlgorithmEnum of(String type) {
        return Arrays.stream(BidAlgorithmEnum.values()).filter(e -> e.type.equals(type)).findAny().orElse(OTHER);
    }
}
