package com.tecdo.domain.biz;

import com.tecdo.adm.api.delivery.enums.AdTypeEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * 参与竞价的广告物料
 * <p>
 * Created by Zeki on 2023/2/1
 */
@Setter
@Getter
public class BidCreative {

    /**
     * 广告类型
     * @see AdTypeEnum
     */
    private Integer Type;

    /**
     * 物料宽度
     */
    private String width;

    /**
     * 物料高度
     */
    private String height;

    /**
     * 广告在屏幕上的位置
     */
    private Integer pos = 0;
}