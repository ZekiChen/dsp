package com.tecdo.starter.tool.function;

import org.springframework.lang.Nullable;

/**
 * 受检的 Consumer
 *
 * Created by Zeki on 2022/8/16
 **/
@FunctionalInterface
public interface CheckedConsumer<T> {

	/**
	 * Run the Consumer
	 *
	 * @param t T
	 * @throws Throwable UncheckedException
	 */
	@Nullable
	void accept(@Nullable T t) throws Throwable;

}
