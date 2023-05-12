package com.tecdo.adm.api.delivery.vo;

import com.tecdo.adm.api.delivery.entity.AffCountryBundleBList;
import com.tecdo.adm.api.delivery.entity.Affiliate;
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
@ApiModel(value = "AffiliateVO对象")
public class AffiliateVO extends Affiliate {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("渠道*国家*bundle黑名单集合")
	private List<AffCountryBundleBList> affCountryBundleBLists;

}