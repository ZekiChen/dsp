package com.tecdo.constant;

/**
 * 缓存名
 *
 * Created by Zeki on 2023/2/6
 **/
public interface CacheConstant {

	// =================================== cache key ===========================================
	String SERVICE = "pac:dsp:";

	String WIN_CACHE = SERVICE + "win:";
	String IMP_CACHE = SERVICE + "imp:";
	String CLICK_CACHE = SERVICE + "click:";


	// =================================== key expire ===========================================

	long DAY_COUNT_EXP = 60 * 60 * 24;  // 24h
}
