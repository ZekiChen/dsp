package com.tecdo.adm.api.delivery.vo;

import com.sun.org.apache.xpath.internal.operations.Bool;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * Created by Elwin on 2023/11/15
 */
@Data
@ApiModel(value = "Object")
public class LimitBundleUpdateVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("广告组ID集")
    @NotEmpty
    private List<Integer> adGroupIds;

    @ApiModelProperty("曝光限制修改标识")
    private Boolean isImpUpdate;

    @ApiModelProperty("点击限制修改标识")
    private Boolean isClickUpdate;

    @ApiModelProperty("花费限制修改标识")
    private Boolean isCostUpdate;

    @ApiModelProperty("比较符")
    private String operation;

    @ApiModelProperty("曝光限制")
    private String impValue;

    @ApiModelProperty("点击限制")
    private String clickValue;

    @ApiModelProperty("花费限制")
    private String costValue;
}
