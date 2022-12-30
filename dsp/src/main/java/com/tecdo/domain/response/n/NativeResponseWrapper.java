package com.tecdo.domain.response.n;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

/**
 * 在1.1版本之前，存在这一层包装
 * 再将这个对象转为json字符串给{@link com.tecdo.domain.response.Bid#adm}
 */
@Getter
@Setter
public class NativeResponseWrapper {

  @JsonProperty("native")
  private NativeResponse nativeWrapper;

}
