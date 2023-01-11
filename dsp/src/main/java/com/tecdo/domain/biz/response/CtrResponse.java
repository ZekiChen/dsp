package com.tecdo.domain.biz.response;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * CTR预估响应 顶层对象模型
 *
 * Created by Zeki on 2023/1/9
 **/
@Setter
@Getter
public class CtrResponse implements Serializable {

    /**
     * 广告id，用于关联请求中的adId
     */
    private Integer adId;

    /**
     * 预估的ctr
     */
    private Double pCtr;
}
