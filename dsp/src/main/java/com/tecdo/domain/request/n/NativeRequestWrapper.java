package com.tecdo.domain.request.n;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

/**
 * 在1.1版本之前，存在这一层包装
 * 再将这个对象转为json字符串给{@link com.tecdo.domain.request.Native#request}
 */
@Getter
@Setter
public class NativeRequestWrapper {

  @JsonProperty("native")
  private NativeRequest nativeWrapper;

}
