package com.tecdo.job.domain.vo.budget;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by Elwin on 2023/9/27
 */
@Data
@AllArgsConstructor
public class MsgContent {
    private String type;
    private ContentData data;
}
