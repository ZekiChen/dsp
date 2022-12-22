package com.tecdo.util;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapHelper {

  public static <T> T get(Map<Integer, Object> map, Integer key, T defaultValue) {
    final Object v = map != null ? map.get(key) : null;
    return (v == null) ? defaultValue : (T) v;
  }

  public static <E, T> Map<E, T> getMap(Map<Integer, Object> map, Integer key) {
    return get(map, key, null);
  }

  public static <T> T get(Map<String, Object> map, String key, T defaultValue) {
    final Object v = map != null ? map.get(key) : null;
    return (v == null) ? defaultValue : (T) v;
  }

  public static String getString(Map<String, Object> map, String key) {
    return get(map, key, null);
  }

  public static int getInt(Map<String, Object> map, String key) {
    return get(map, key, 0);
  }

  public static long getLong(Map<String, Object> map, String key) {
    return get(map, key, 0L);
  }

  public static <T> List<T> getList(Map<String, Object> map, String key) {
    return get(map, key, null);
  }

  public static <T> Set<T> getSet(Map<String, Object> map, String key) {
    return get(map, key, null);
  }

  public static <E, T> Map<E, T> getMap(Map<String, Object> map, String key) {
    return get(map, key, null);
  }

}
