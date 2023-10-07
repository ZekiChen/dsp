package com.tecdo.job.domain.vo.budget;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by Elwin on 2023/9/27
 */
@Data
@AllArgsConstructor
public class ContentData {
    private String template_id;
    private BudgetWarn template_variable;
}
