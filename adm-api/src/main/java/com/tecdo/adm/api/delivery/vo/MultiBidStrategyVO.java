package com.tecdo.adm.api.delivery.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tecdo.adm.api.delivery.entity.MultiBidStrategy;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Elwin on 2023/11/3
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "MultiBidStrategyVO对象")
public class MultiBidStrategyVO extends MultiBidStrategy {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private Integer adGroupId;
}
