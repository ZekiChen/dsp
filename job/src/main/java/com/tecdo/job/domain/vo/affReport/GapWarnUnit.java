package com.tecdo.job.domain.vo.affReport;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by Elwin on 2024/1/16
 */
@Data
@AllArgsConstructor
public class GapWarnUnit {
    private String dimension;
    private String dsp_spent;
    private String aff_spent;
    private String gap;
}
