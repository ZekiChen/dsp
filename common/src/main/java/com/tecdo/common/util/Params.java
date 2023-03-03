package com.tecdo.common.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tony on 2018/6/6
 */
public class Params {

  private Map<String, Object> map = new HashMap<>();

  private Params() {
  }

  /**
   * Get with default value
   * when params is null or key is not contained or the value of ke is null.
   *
   * @param params
   * @param key
   * @param defaultValue
   * @param <T>
   *
   * @return
   */
  public static <T> T get(Params params, String key, T defaultValue) {
    if (params == null || !params.containsKey(key)) {
      return defaultValue;
    }
    return params.get(key, defaultValue);
  }

  /**
   * Safe way to check if a params which is possibly {@code null} contains a
   * key.
   *
   * @param params
   * @param key
   *
   * @return true if |params| contain specific |key|
   */
  public static boolean contains(Params params, String key) {
    return params != null && params.containsKey(key);
  }

  /**
   * create an instance of Params
   */
  public static Params create() {
    return new Params();
  }

  /**
   * create an instance of Params with one param
   */
  public static Params create(String key, Object value) {
    return create().put(key, value);
  }

  /**
   * create an instance of Params with multi-param
   */
  public static Params create(Params params) {
    return create().merge(params);
  }

  /**
   * put key-value
   */
  public Params put(String key, Object value) {
    map.put(key, value);
    return this;
  }

  /**
   * get value by key, return defaultValue while value is null
   */
  public <T> T get(String key, T defaultValue) {
    Object value = map.getOrDefault(key, defaultValue);
    return value == null ? defaultValue : (T) value;
  }

  /**
   * get value by key
   */
  public <T> T get(String key) {
    return get(key, null);
  }

  /**
   * the size of key-value
   */
  public int size() {
    return map.size();
  }

  /**
   * is the key-value's size empty
   */
  public boolean isEmpty() {
    return map.isEmpty();
  }

  /**
   * remove key
   */
  public Params remove(String key) {
    map.remove(key);
    return this;
  }

  /**
   * is this instance contains the key
   */
  public boolean containsKey(String key) {
    return map.containsKey(key);
  }

  /**
   * merge two params instances
   */
  public Params merge(Params params) {
    map.putAll(params.map);
    return this;
  }

  /**
   * clone params instance
   */
  public Params clone() {
    return create(this);
  }

  public boolean equals(Params params) {
    if (map.size() != params.map.size()) {
      return false;
    }
    for (String key : params.map.keySet()) {
      if (!get(key).equals(params.get(key))) {
        return false;
      }
    }
    return true;
  }

  public String toString() {
    return map.toString();
  }
}
