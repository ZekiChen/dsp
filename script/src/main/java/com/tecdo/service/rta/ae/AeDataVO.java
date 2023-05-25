package com.tecdo.service.rta.ae;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Zeki on 2023/4/4
 */
public class AeDataVO<T> implements Serializable {

    @JSONField(name = "target_list")
    private List<T> targetList;

    public List<T> getTargetList() {
        return targetList;
    }

    public void setTargetList(List<T> targetList) {
        this.targetList = targetList;
    }
}
