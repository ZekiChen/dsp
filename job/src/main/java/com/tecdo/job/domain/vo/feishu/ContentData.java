package com.tecdo.job.domain.vo.feishu;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by Elwin on 2023/9/27
 */
@Data
@AllArgsConstructor
public class ContentData<T> {
    private String template_id;
    private T template_variable;
}
