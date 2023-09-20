package com.tecdo.job.domain.vo.camScanner;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 飞书设置单元格数据类型，包含数据范围range
 * Created by Elwin on 2023/9/20
 */
@Data
@AllArgsConstructor
public class FeishuSetUnitStyle {
    private String range;
    private UnitStyle style;
}

