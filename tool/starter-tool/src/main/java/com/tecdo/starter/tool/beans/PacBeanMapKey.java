package com.tecdo.starter.tool.beans;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

/**
 * bean map key，提高性能
 *
 * Created by Zeki on 2022/8/16
 **/
@EqualsAndHashCode
@AllArgsConstructor
public class PacBeanMapKey {

	private final Class type;
	private final int require;
}
