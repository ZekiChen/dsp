package com.tecdo.starter.tool.function;

import org.springframework.lang.Nullable;

/**
 * 受检的 function
 *
 * Created by Zeki on 2022/8/16
 **/
@FunctionalInterface
public interface CheckedFunction<T, R> {

	/**
	 * Run the Function
	 *
	 * @param t T
	 * @return CheckedFunctionR R
	 * @throws Throwable CheckedException
	 */
	@Nullable
	R apply(@Nullable T t) throws Throwable;

}
