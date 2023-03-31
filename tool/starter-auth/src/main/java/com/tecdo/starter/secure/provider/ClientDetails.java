package com.tecdo.starter.secure.provider;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Zeki on 2023/3/14
 */
@Data
public class ClientDetails implements IClientDetails {

	@ApiModelProperty(value = "客户端id")
	private String clientId;
	@ApiModelProperty(value = "客户端密钥")
	private String clientSecret;
	@ApiModelProperty(value = "令牌过期秒数")
	private Integer accessTokenValidity;
	@ApiModelProperty(value = "刷新令牌过期秒数")
	private Integer refreshTokenValidity;

}
