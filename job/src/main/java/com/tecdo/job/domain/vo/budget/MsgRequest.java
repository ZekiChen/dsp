package com.tecdo.job.domain.vo.budget;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by Elwin on 2023/9/27
 */
@Data
@AllArgsConstructor
public class MsgRequest {
    private String receive_id;
    private String msg_type;
    private String content; // 为序列化并转义后的MsgContent对象
    private String uuid;
}
