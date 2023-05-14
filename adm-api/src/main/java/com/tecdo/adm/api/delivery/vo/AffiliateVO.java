package com.tecdo.adm.api.delivery.vo;

import com.tecdo.adm.api.delivery.entity.Affiliate;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Zeki on 2023/3/15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "AffiliateVO对象")
public class AffiliateVO extends Affiliate {

	private static final long serialVersionUID = 1L;

}