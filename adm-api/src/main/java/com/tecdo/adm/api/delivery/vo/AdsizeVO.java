package com.tecdo.adm.api.delivery.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by Elwin on 2023/9/12
 */
@Data
@ApiModel("AdSizeVO对象")
public class AdsizeVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("宽度标准")
    private Integer width;
    @ApiModelProperty("高度标准")
    private Integer height;
}
