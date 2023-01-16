package com.tecdo.constant;

public interface ParamKey {

    String REQUEST_ID = "requestId";

    String HTTP_REQUEST = "httpRequest";

    // control response
    String CHANNEL_CONTEXT = "channelContext";
    String HTTP_CODE = "httpCode";
    String RESPONSE_HEADER = "responseHeader";
    String RESPONSE_BODY = "responseBody";

    // init db data, add into cache
    String AFFILIATES_CACHE_KEY = "affiliates-cache-key";
    String ADS_CACHE_KEY = "ads-cache-key";
    String RTA_INFOS_CACHE_KEY = "rta-infos-cache-key";

    // task-imp
    String ADS_IMP_KEY = "ads-imp-key";
}
