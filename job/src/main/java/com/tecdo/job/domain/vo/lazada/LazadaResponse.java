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
public class LazadaResponse<T> implements Serializable {

    private LazadaPage<T> data;
    private boolean success;
    private String code;
    @JSONField(name = "request_id")
    private String requestId;

}
