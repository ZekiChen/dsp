package com.tecdo.domain.biz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Elwin on 2023/12/14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BidfloorDTO {
    private Float bidfloor;
    private String dealid;
}
