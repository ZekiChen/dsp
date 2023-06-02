package com.tecdo.job.domain.vo.lazada;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by Zeki on 2023/2/24
 */
@Setter
@Getter
public class LazadaReportVO implements Serializable {

    private String date;
    private String country;
    @JSONField(name = "first_launch")
    private Long event1;
    @JSONField(name = "valid_launch_uv")
    private Long event2;
    @JSONField(name = "quality_event")
    private Long event3;

}
