package com.tecdo.starter.tool.beans;

import java.lang.annotation.*;

/**
 * copy 字段 配置
 *
 * Created by Zeki on 2022/8/16
 **/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CopyProperty {

	/**
	 * 属性名，用于指定别名，默认使用：field name
	 * @return 属性名
	 */
	String value() default "";

	/**
	 * 忽略：默认为 false
	 * @return 是否忽略
	 */
	boolean ignore() default false;
}
