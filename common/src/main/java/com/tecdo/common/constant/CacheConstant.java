package com.tecdo.common.constant;

/**
 * 缓存名
 *
 * Created by Zeki on 2023/2/6
 **/
public interface CacheConstant {

	// =================================== cache key ===========================================
	String WIN_CACHE = "pac:dsp:win";
	String IMP_CACHE = "pac:dsp:imp";
	String CLICK_CACHE = "pac:dsp:click";

	// delivery
	String CAMPAIGN_CACHE = "pac:adm:campaign";
	String AD_GROUP_CACHE = "pac:adm:adGroup";
	String AD_CACHE = "pac:adm:ad";
	// system
	String DICT_CACHE = "pac:adm:dict";


	// =================================== key expire ===========================================\
	long DAY_COUNT_EXP = 60 * 60 * 24;  // 24h

}
