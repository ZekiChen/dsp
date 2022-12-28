package com.tecdo.domain.request;

import com.tecdo.domain.base.Extension;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 述限制买卖双方之间交易的一些条款。它在 Pmp 集合中的出现表示该展示符合交易描述的条款。
 *
 * Created by Zeki on 2022/12/23
 **/
@Setter
@Getter
public class Deal extends Extension {

    /**
     * 直接交易的唯一标识（必须）
     */
    private String id;

    /**
     * 本次展示的最低竞价，以CPM为单位
     */
    private Float bidfloor = 0f;

    /**
     * 使用 ISO-4217 码表指定的货币。如果交易平台允许，这可能与竞价者返回的竞价货币类型不一致
     */
    private String bidfloorcur = "USD";

    /**
     * 可选的覆盖竞价请求中的竞拍类型
     * 1-第一价格竞拍； 2-第二价格竞拍； 3-使用bidfloor可以作为交易价格
     */
    private Integer at;

    /**
     * 允许参与本次交易竞价的买方席位白名单。席位ID需要ADX和竞拍者提前协商，忽略本属性标示没有席位限制
     */
    private List<String> wseat;

    /**
     * 允许参与本次交易竞价的广告主域名列表（例如，advertiser.com)。忽略本属性标示没有广告主限制。
     */
    private List<String> wadomain;
}
