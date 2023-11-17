package com.tecdo.adm.api.delivery.vo;

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
public class AutoBundleUpdateVO implements Serializable {
    @ApiModelProperty("广告组ID集")
    @NotEmpty
    private List<Integer> adGroupIds;

    @ApiModelProperty("自动拉黑条件集合")
    private List<TargetConditionVO> conditionVOs;
}