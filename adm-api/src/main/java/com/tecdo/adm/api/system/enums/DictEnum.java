package com.tecdo.adm.api.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Zeki on 2023/3/9
 */
@Getter
@AllArgsConstructor
public enum DictEnum {

	/**
	 * Lazada RTA人群和Feature的映射关系
	 */
	LAZADA_RTA_FEATURE("lazada_rta_feature"),
	;

	final String name;

}