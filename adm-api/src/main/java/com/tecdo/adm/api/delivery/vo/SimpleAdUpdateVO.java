package com.tecdo.adm.api.delivery.vo;

import com.tecdo.starter.mp.entity.IdEntity;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Zeki on 2023/3/8
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "Object")
public class SimpleAdUpdateVO extends IdEntity {

	private static final long serialVersionUID = 1L;

	private String name;
}