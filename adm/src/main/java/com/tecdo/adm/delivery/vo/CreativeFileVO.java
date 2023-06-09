package com.tecdo.adm.delivery.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * Created by Zeki on 2023/3/8
 */
@Data
@ApiModel(value = "CreativeFileVO对象")
public class CreativeFileVO implements Serializable {

	private static final long serialVersionUID = 1L;

	private MultipartFile file;
	private String name;
	private Integer type;
	private Integer width;
	private Integer height;
	private String catIab;
}