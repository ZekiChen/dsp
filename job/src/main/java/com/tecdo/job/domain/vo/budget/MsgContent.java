package com.tecdo.job.domain.vo.budget;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by Elwin on 2023/9/27
 */
@Data
@AllArgsConstructor
public class MsgContent {
    // 飞书群消息模板号
    private String type;
    // 消息对象
    private ContentData data;
}
