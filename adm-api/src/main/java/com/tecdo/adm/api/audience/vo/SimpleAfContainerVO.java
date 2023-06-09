package com.tecdo.adm.api.audience.vo;

import com.tecdo.starter.mp.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Zeki on 2023/3/15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "SimpleAfContainerVO对象")
public class SimpleAfContainerVO extends BaseVO {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("af账号下的广告主")
	private String afAdvertiser;
	@ApiModelProperty("af账号下的app id")
	private String afAppId;
}