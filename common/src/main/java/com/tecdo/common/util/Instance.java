package com.tecdo.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link Instance} supports global singleton patterns:
 * <p/>
 * <h1>Global Singleton</h1>
 * <p>
 * Directly invoke <code>Instance.of(X.class)</code> to get a gloabl singleton
 * instance, but the X class must own a non-public non-parametric constructor.
 * </p>
 */
public class Instance {
  private static final Logger LOGGER = LoggerFactory.getLogger(Instance.class);
  private static final Map<Class<?>, Object> sInstanceMap;

  static {
    sInstanceMap = new ConcurrentHashMap<>();
  }

  /**
   * Generate a global instance automatically
   * Note that class must without public default constructor
   *
   * @param cls
   *   clazz
   * @param <T>
   *   type
   *
   * @return instance
   */
  @SuppressWarnings("unchecked")
  public static <T> T of(Class<T> cls) {
    if (cls == null) {
      return null;
    }

    // 1) get existed instance
    Object instance = sInstanceMap.get(cls);

    // 2) create instance automatically
    if (instance == null) {
      try {
        Constructor<T> ctr = cls.getDeclaredConstructor();

        /**
         * Only a class without public default constructor can be
         * used to gen global instance automatically
         */
        if (Modifier.isPublic(ctr.getModifiers())) {
          LOGGER.warn("Try to gen global instance with public default constructor {}", cls);
        }

        ctr.setAccessible(true);
        sInstanceMap.put(cls, instance = ctr.newInstance());
      } catch (Throwable e) {
        LOGGER.error("Gen global instance error", e);
      }
    }

    return (T) instance;
  }

  public static void destroy(Class<?> cls) {
    sInstanceMap.remove(cls);
  }

  public static void reset() {
    sInstanceMap.clear();
  }

}
