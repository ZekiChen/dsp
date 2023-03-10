package com.tecdo.starter.tool.function;

import org.springframework.lang.Nullable;

/**
 * 受检的 Callable
 * Created by Zeki on 2022/8/16
 **/
@FunctionalInterface
public interface CheckedCallable<T> {

	/**
	 * Run this callable.
	 *
	 * @return result
	 * @throws Throwable CheckedException
	 */
	@Nullable
	T call() throws Throwable;
}
