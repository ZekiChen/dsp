package com.tecdo.starter.tool.function;

/**
 * 受检的 runnable
 *
 * Created by Zeki on 2022/8/16
 **/
@FunctionalInterface
public interface CheckedRunnable {

	/**
	 * Run this runnable.
	 *
	 * @throws Throwable CheckedException
	 */
	void run() throws Throwable;

}
