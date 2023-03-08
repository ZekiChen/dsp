package com.tecdo.adm.api.delivery.vo;

import com.tecdo.adm.api.delivery.entity.Ad;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Zeki on 2023/3/8
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "AdVO对象", description = "AdVO对象")
public class AdVO extends Ad {

	private static final long serialVersionUID = 1L;

}