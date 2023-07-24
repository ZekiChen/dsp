package com.tecdo.job.handler.sdk.lazada.match.api;

import java.util.Map;


/**
 * Lazada Open Platform API basic request.
 *
 * @author carver.gu
 * @since Feb 4, 2018
 */
public class LazopRequest {

  /**
   * User custom request query parameters.
   */
  protected LazopHashMap apiParams;

  /**
   * HTTP header parameters.
   */
  protected LazopHashMap headerParams;


  private Long timestamp;
  private String apiName;
  private String httpMethod = Constants.METHOD_POST;

  public LazopRequest() {

  }

  /**
   * create LazopRequest with apiName
   *
   * @param apiName
   */
  public LazopRequest(String apiName) {
    this.apiName = apiName;
  }

  public void addApiParameter(String key, String value) {
    if (this.apiParams == null) {
      this.apiParams = new LazopHashMap();
    }
    this.apiParams.put(key, value);
  }

  public void addHeaderParameter(String key, String value) {
    if (this.headerParams == null) {
      this.headerParams = new LazopHashMap();
    }
    this.headerParams.put(key, value);
  }

  public LazopHashMap getApiParams() {
    return apiParams;
  }

  public Map<String, String> getHeaderParams() {
    return this.headerParams;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }

  public String getApiName() {
    return apiName;
  }

  public void setApiName(String apiName) {
    this.apiName = apiName;
  }

  public String getHttpMethod() {
    return httpMethod;
  }

  public void setHttpMethod(String httpMethod) {
    this.httpMethod = httpMethod;
  }

  public void setHeaderParams(LazopHashMap headerParams) {
    this.headerParams = headerParams;
  }

}
