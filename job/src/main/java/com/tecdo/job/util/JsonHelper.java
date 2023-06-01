package com.tecdo.job.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class JsonHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(JsonHelper.class);
  private static final ObjectMapper MAPPER;

  static {
    MAPPER = new ObjectMapper();
    MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  public static byte[] toJSONByteArray(Object obj) {
    byte[] out = null;
    try {
      out = MAPPER.writeValueAsBytes(obj);
    } catch (Throwable e) {
      LOGGER.error("to json byte array error!", e);
    }
    return out;
  }


  public static String toJSONString(Object object) {
    String json = null;
    try {
      json = MAPPER.writeValueAsString(object);
    } catch (Throwable e) {
      LOGGER.error("json error: {}", object);
      LOGGER.error("to json string error!", e);
    }
    return json;
  }

  public static <T> T parseObject(byte[] json, Class<T> clazz) {
    T object = null;
    try {
      object = MAPPER.readValue(json, clazz);
    } catch (Throwable e) {
      LOGGER.error("parse json byte array object error!", e);
    }
    return object;
  }

  public static <T> T parseObject(String json, Class<T> clazz) {
    T object = null;
    try {
      object = MAPPER.readValue(json, clazz);
    } catch (Throwable e) {
      LOGGER.error("parse json object error!", e);
    }
    return object;
  }

  public static <T> T parseObject(String json, JavaType type) {
    T object = null;
    try {
      object = MAPPER.readValue(json, type);
    } catch (Throwable e) {
      LOGGER.error("parse json object with type error!", e);
    }
    return object;
  }

  public static <T> List<T> parseArray(String json, Class<T> clazz) {
    CollectionType type = MAPPER.getTypeFactory().constructCollectionType(List.class, clazz);
    List<T> object = null;
    try {
      object = MAPPER.readValue(json, type);
    } catch (Throwable e) {
      LOGGER.error("parse json array error!", e);
    }
    return object;
  }

  public static <T, U> Map<T, U> parseMap(String json, Class<T> key, Class<U> value) {
    MapType type = MAPPER.getTypeFactory().constructMapType(Map.class, key, value);
    Map<T, U> object = null;
    try {
      object = MAPPER.readValue(json, type);
    } catch (Throwable e) {
      LOGGER.error("parse json array error!", e);
    }
    return object;
  }

  public static <T> T convertValue(Object data, Class<T> type) {
    return MAPPER.convertValue(data, type);
  }

  public static <T> T convertValue(Object data, TypeReference<T> toValueTypeRef) {
    return MAPPER.convertValue(data, toValueTypeRef);
  }
}
