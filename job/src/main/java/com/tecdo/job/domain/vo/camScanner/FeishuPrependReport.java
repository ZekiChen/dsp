package com.tecdo.job.domain.vo.camScanner;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by Elwin on 2023/9/19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeishuPrependReport {
    private String range;
    private List<List<Object>> values;
}
