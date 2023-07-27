package com.tecdo.common.constant;

/**
 * 缓存名
 *
 * Created by Zeki on 2023/2/6
 **/
public interface CacheConstant {

	// =================================== cache key ===========================================
	String WIN_CACHE = "pac:dsp:win";
	String LOSS_CACHE = "pac:dsp:loss";
	String IMP_CACHE = "pac:dsp:imp";
	String CLICK_CACHE = "pac:dsp:click";
	String RTA_CACHE = "pac:dsp:rta";

	String AUDIENCE_CACHE = "pac:dsp:audience";


	// delivery
	String CAMPAIGN_CACHE = "pac:adm:campaign";
	String AD_GROUP_CACHE = "pac:adm:adGroup";
	String AD_CACHE = "pac:adm:ad";
	String CREATIVE_CACHE = "pac:adm:creative";
	String ADV_CACHE = "pac:adm:adv";
	String AFF_CACHE = "pac:adm:affiliate";
	// system
	String DICT_CACHE = "pac:adm:dict";


	// =================================== key expire ===========================================\
	long DAY_COUNT_EXP = 60 * 60 * 24;  // 24h

}
