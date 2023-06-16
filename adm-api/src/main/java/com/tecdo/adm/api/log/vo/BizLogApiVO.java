package com.tecdo.adm.api.log.vo;

import com.tecdo.adm.api.log.entity.BizLogApi;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Zeki on 2023/6/15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "VO对象")
public class BizLogApiVO extends BizLogApi {

	private static final long serialVersionUID = 1L;

	/**
	 * 操作类型名称
	 */
	private String optTypeName;
	/**
	 * 业务类型名称
	 */
	private String bizTypeName;

}
