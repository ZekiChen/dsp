package com.tecdo.starter.tool.beans;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * copy key
 *
 * Created by Zeki on 2022/8/16
 **/
@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class PacBeanCopierKey {

	private final Class<?> source;
	private final Class<?> target;
	private final boolean useConverter;
	private final boolean nonNull;
}
