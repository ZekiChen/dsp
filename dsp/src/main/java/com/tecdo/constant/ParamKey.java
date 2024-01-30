package com.tecdo.constant;

public interface ParamKey {

  String REQUEST_ID = "requestId";

  String HTTP_REQUEST = "httpRequest";

  String BID_REQUEST = "bidRequest";
  String AFFILIATE = "affiliate";

  String TASK_ID = "taskId";

  String EXCEPTION_EVENT = "exceptionEvent";

  // control response
  String CHANNEL_CONTEXT = "channelContext";
  String HTTP_CODE = "httpCode";
  String RESPONSE_HEADER = "responseHeader";
  String RESPONSE_BODY = "responseBody";
  String RESPONSE_IMAGE = "responseImage";

  // init db data, add into cache
  String AFFILIATES_CACHE_KEY = "affiliates-cache-key";
  String AFFILIATE_ID_CACHE_KEY = "affiliate-id-cache-key";
  String ADS_CACHE_KEY = "ads-cache-key";
  String CAMPAIGNS_CACHE_KEY = "campaigns-cache-key";
  String RTA_INFOS_CACHE_KEY = "rta-infos-cache-key";
  String CAMPAIGN_BUDGETS_CACHE_KEY = "campaign-budgets-cache-key";
  String AD_GROUP_BUDGETS_CACHE_KEY = "ad-group-budgets-cache-key";
  String AB_TEST_CONFIG_CACHE_KEY = "ab-test-config-key";
  String GP_APP_CACHE_KEY = "gp-app-cache-key";
  String GP_APP_CATEGORY_CACHE_KEY = "gp-app-category-cache-key";
  String GP_APP_TAG_CACHE_KEY = "gp-app-tag-cache-key";
  String IP_TABLE_CACHE_KEY = "ip-table-key";
  String AF_AUDIENCE_SYNC_KEY = "af-audience-sync-key";
  String AF_CONTAINER_SYNC_KEY = "af-container-sync-key";
  String AFF_COUNTRY_BUNDLE_LIST_CACHE_KEY = "aff-country-bundle-list-cache-key";
  String BUNDLE_DATA_GT_SIZE_CACHE_KEY = "bundle-data-gt-size-cache-key";
  String BUNDLE_DATA_CACHE_KEY = "bundle-data-cache-key";
  String ECPX_CACHE_KEY = "ecpx-cache-key";
  String BUNDLE_COST_CACHE_KEY = "bundle-cost-cache-key";
  String BUNDLE_ADGROUP_DATA_CACHE_KEY = "bundle-adgroup-data-cache-key";
  String PIXALATE_FRAUD_CACHE_KEY = "pixalate-fraud-cache-key";
  String AFFILIATE_PMP_ID_CACHE_KEY = "affiliate-pmp-id-cache-key";
  String AFFILIATE_BUNDLE_DATA_CACHE_KEY = "affiliate-bundle-data-cache-key";

  String CHEATING_DATA_FILTER = "cheating-data-filter";

  // task-imp
  String ADS_RECALL_RESPONSE = "ads-recall-response";
  String ADS_PREDICT_RESPONSE = "ads-predict-response";
  String ADS_CALC_PRICE_RESPONSE = "ads-calc-price-response";
  String ADS_PRICE_FILTER_RESPONSE = "ads-price-filter-response";

  String REQUEST_LAZADA_RTA_RESPONSE = "requestLazadaRtaResponse";
  String REQUEST_AE_RTA_RESPONSE = "requestAeRtaResponse";
  String REQUEST_MIRAVIA_RTA_RESPONSE = "requestMiraviaRtaResponse";

  String SORT_AD_RESPONSE = "sortAdResponse";
  String ADS_TASK_RESPONSE = "ads-imp-key";
  String DISTINCT_AD_RESPONSE = "distinct-ad-Response";
}
