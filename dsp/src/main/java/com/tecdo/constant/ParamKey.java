package com.tecdo.constant;

public interface ParamKey {

  String REQUEST_ID = "requestId";

  String HTTP_REQUEST = "httpRequest";

  String IMP = "imp";
  String BID_REQUEST = "bidRequest";
  String AFFILIATE = "affiliate";
  String SORT_AD_RESPONSE = "sortAdResponse";

  String REQUEST_RTA_RESPONSE = "requestRtaResponse";

  String TASK_ID = "taskId";

  // control response
  String CHANNEL_CONTEXT = "channelContext";
  String HTTP_CODE = "httpCode";
  String RESPONSE_HEADER = "responseHeader";
  String RESPONSE_BODY = "responseBody";

  // init db data, add into cache
  String AFFILIATES_CACHE_KEY = "affiliates-cache-key";
  String ADS_CACHE_KEY = "ads-cache-key";
  String RTA_INFOS_CACHE_KEY = "rta-infos-cache-key";
  String CAMPAIGN_BUDGETS_CACHE_KEY = "campaign-budgets-cache-key";
  String AD_GROUP_BUDGETS_CACHE_KEY = "ad-group-budgets-cache-key";
  String AB_TEST_CONFIG_CACHE_KEY = "ab-test-config-key";
  String GP_APP_CACHE_KEY = "gp-app-cache-key";
  String IP_TABLE_CACHE_KEY = "ip-table-key";

  String AF_AUDIENCE_SYNC_KEY = "af-audience-sync-key";

  // task-imp
  String ADS_TASK_RESPONSE = "ads-imp-key";
  String ADS_RECALL_RESPONSE = "ads-recall-response";
  String ADS_P_CTR_RESPONSE = "ads-predict-ctr-response";
  String ADS_CALC_PRICE_RESPONSE = "ads-calc-price-response";
}
