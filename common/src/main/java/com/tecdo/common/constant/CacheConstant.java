package com.tecdo.common.constant;

/**
 * 缓存名
 *
 * Created by Zeki on 2023/2/6
 **/
public interface CacheConstant {

	// =================================== cache key ===========================================
	String WIN_CACHE = "pac:dsp:win:";
	String IMP_CACHE = "pac:dsp:imp:";
	String CLICK_CACHE = "pac:dsp:click:";

	String EXAMPLE_CACHE = "pac:adm:example:";

	// =================================== key expire ===========================================\
	long DAY_COUNT_EXP = 60 * 60 * 24;  // 24h

}
