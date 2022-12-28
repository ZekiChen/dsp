package com.tecdo.domain.base;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 扩展信息
 *
 * Created by Zeki on 2022/12/22
 **/
@Setter
@Getter
public class Extension implements Serializable {

    /**
     * 特定交易的OpenRTB协议的扩展占位符
     */
    private Object ext;
}
