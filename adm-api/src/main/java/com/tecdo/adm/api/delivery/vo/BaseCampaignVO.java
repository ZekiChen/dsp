package com.tecdo.adm.api.delivery.vo;

import com.tecdo.starter.mp.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Created by Zeki on 2023/3/15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "CampaignGroupVO对象")
public class BaseCampaignVO extends BaseVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("AdGroup的id-name集合")
    private List<BaseVO> baseAdGroupVOs;
}