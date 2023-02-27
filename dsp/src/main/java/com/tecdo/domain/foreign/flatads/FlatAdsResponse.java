package com.tecdo.domain.foreign.flatads;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * FlatAds Base VO
 *
 * Created by Zeki on 2023/2/24
 */
@Getter
@Setter
public class FlatAdsResponse implements Serializable {

    private List<FlatAdsReportVO> data;
    private String msg;
    private Integer status;
}
