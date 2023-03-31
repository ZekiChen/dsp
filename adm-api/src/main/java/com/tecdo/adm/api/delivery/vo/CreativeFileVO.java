package com.tecdo.adm.api.delivery.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by Zeki on 2023/3/8
 */
@Data
@ApiModel(value = "CreativeFileVO对象")
public class CreativeFileVO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("素材id")
	private Integer creativeId;

	@ApiModelProperty("文件地址（走CDN）")
	private String url;

	@ApiModelProperty(value = "存桶里的路径", notes = "例如：upload/20230315/70fdc9d57918c52c9ee76c26a4af4b00.png")
	private String name;

	@ApiModelProperty(value = "原始文件名", notes = "例如：测试.png")
	private String originalName;
}