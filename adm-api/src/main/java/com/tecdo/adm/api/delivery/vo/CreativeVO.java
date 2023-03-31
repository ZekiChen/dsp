package com.tecdo.adm.api.delivery.vo;

import com.tecdo.adm.api.delivery.entity.Creative;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Zeki on 2023/3/8
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "CreativeVO对象")
public class CreativeVO extends Creative {

	private static final long serialVersionUID = 1L;

}