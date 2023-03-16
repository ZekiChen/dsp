package com.tecdo.starter.mp.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by Zeki on 2023/3/14
 */
@Data
@ApiModel("BaseVO对象")
public class BaseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Integer id;
    @ApiModelProperty("名称")
    private String name;
}
