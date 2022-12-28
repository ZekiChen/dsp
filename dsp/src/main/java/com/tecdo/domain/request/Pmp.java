package com.tecdo.domain.request;

import com.tecdo.domain.base.Extension;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 包含本展示涉及的买卖双方的直接交易相关的私有市场信息。真实的交易信息使用一组 Deal 对象表示
 *
 * Created by Zeki on 2022/12/23
 **/
@Setter
@Getter
public class Pmp extends Extension {

    /**
     * 标识在Deal对象中指明的席位的竞拍合格标准
     * 0-接受所有竞拍； 1-竞拍受deals属性中描述的规则的限制
     */
    private Integer private_auction = 0;

    /**
     * 一组Deal对象，用于传输适用于本次展示的交易信息
     */
    private List<Deal> deals;
}
