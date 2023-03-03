package com.tecdo.domain.openrtb.response.n;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tecdo.domain.openrtb.response.Bid;

import lombok.Getter;
import lombok.Setter;

/**
 * 在1.1版本之前，存在这一层包装
 * 再将这个对象转为json字符串给{@link Bid#adm}
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NativeResponseWrapper {

  @JsonProperty("native")
  private NativeResponse nativeResponse;

}
