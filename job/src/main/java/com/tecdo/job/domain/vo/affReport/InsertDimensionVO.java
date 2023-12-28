package com.tecdo.job.domain.vo.affReport;

import lombok.Data;

/**
 * Created by Elwin on 2023/12/25
 */
@Data
public class InsertDimensionVO {
    private String sheetId;
    private String majorDimension;
    private Integer startIndex;
    private Integer endIndex;
}
