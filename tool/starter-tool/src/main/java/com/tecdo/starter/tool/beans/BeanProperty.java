package com.tecdo.starter.tool.beans;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Bean属性
 *
 * Created by Zeki on 2022/8/16
 **/
@Getter
@AllArgsConstructor
public class BeanProperty {

	private final String name;
	private final Class<?> type;
}
