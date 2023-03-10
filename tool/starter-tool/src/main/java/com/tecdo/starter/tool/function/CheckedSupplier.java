package com.tecdo.starter.tool.function;

import org.springframework.lang.Nullable;

/**
 * 受检的 Supplier
 *
 * Created by Zeki on 2022/8/16
 **/
@FunctionalInterface
public interface CheckedSupplier<T> {

	/**
	 * Run the Supplier
	 *
	 * @return T
	 * @throws Throwable CheckedException
	 */
	@Nullable
	T get() throws Throwable;

}
