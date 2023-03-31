package com.tecdo.service.rta.api;

import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.OkHttps;
import com.tecdo.service.rta.ResponseDTO;
import com.tecdo.util.JsonHelper;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;
import java.util.Set;


/**
 * rewrite from lazada sdk
 */
public class LazopClient {

  protected String serverUrl;
  protected String appKey;
  protected String appSecret;

  protected String signMethod = Constants.SIGN_METHOD_SHA256;

  public LazopClient(String serverUrl, String appKey, String appSecret) {
    this.appKey = appKey;
    this.appSecret = appSecret;
    this.serverUrl = serverUrl;
  }

  public ResponseDTO execute(LazopRequest request) throws Exception {

    RequestContext requestContext = new RequestContext();
    LazopHashMap bizParams = request.getApiParams();
    requestContext.setQueryParams(bizParams);
    requestContext.setApiName(request.getApiName());

    // add common parameters
    LazopHashMap commonParams = new LazopHashMap();
    commonParams.put(Constants.APP_KEY, appKey);
    Long timestamp = request.getTimestamp();
    if (timestamp == null) {
      timestamp = System.currentTimeMillis();
    }

    commonParams.put(Constants.TIMESTAMP, new Date(timestamp));
    commonParams.put(Constants.SIGN_METHOD, signMethod);

    requestContext.setCommonParams(commonParams);

    ResponseDTO response = null;


    // compute request signature
    commonParams.put(Constants.SIGN,
                     LazopUtils.signApiRequest(requestContext, appSecret, signMethod));
    String rpcUrl = buildRestUrl(this.serverUrl, request.getApiName());
    String urlQuery = buildQuery(requestContext.getCommonParams(), Constants.CHARSET_UTF8);
    String fullUrl = buildRequestUrl(rpcUrl, urlQuery);

    HttpResult httpResult = OkHttps.sync(fullUrl)
                                   .bodyType(OkHttps.FORM)
                                   .setBodyPara(bizParams)
                                   .addHeader(request.getHeaderParams())
                                   .post();
    if (httpResult.isSuccessful()) {
      response = JsonHelper.parseObject(httpResult.getBody().toString(), ResponseDTO.class);
    } else {
      if (httpResult.getError() != null) {
        throw httpResult.getError();
      } else {
        throw new Exception("failed to request rta,http status is " + httpResult.getStatus());
      }
    }

    return response;
  }

  public static String buildRequestUrl(String url, String... queries) {
    if (queries == null || queries.length == 0) {
      return url;
    }

    StringBuilder newUrl = new StringBuilder(url);
    boolean hasQuery = url.contains("?");
    boolean hasPrepend = url.endsWith("?") || url.endsWith("&");

    for (String query : queries) {
      if (!LazopUtils.isEmpty(query)) {
        if (!hasPrepend) {
          if (hasQuery) {
            newUrl.append("&");
          } else {
            newUrl.append("?");
            hasQuery = true;
          }
        }
        newUrl.append(query);
        hasPrepend = false;
      }
    }
    return newUrl.toString();
  }

  public static String buildRestUrl(String url, String apiName) {
    if (apiName == null || apiName.length() == 0) {
      return url;
    }

    boolean hasPrepend = url.endsWith("/");
    if (hasPrepend) {
      return url + apiName.substring(1);
    } else {
      return url + apiName;
    }
  }

  public static String buildQuery(Map<String, String> params, String charset) throws IOException {
    if (params == null || params.isEmpty()) {
      return null;
    }

    StringBuilder query = new StringBuilder();
    Set<Map.Entry<String, String>> entries = params.entrySet();
    boolean hasParam = false;

    for (Map.Entry<String, String> entry : entries) {
      String name = entry.getKey();
      String value = entry.getValue();
      // ignore blank parameter
      if (LazopUtils.areNotEmpty(name, value)) {
        if (hasParam) {
          query.append("&");
        } else {
          hasParam = true;
        }

        query.append(name).append("=").append(URLEncoder.encode(value, charset));
      }
    }

    return query.toString();
  }

}
