package com.tecdo.util;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MyBatisUtil {

  private static final String RESOURCE = "/mybatis-conf.xml";
  private static InputStream msInputStream;
  private static Map<String, SqlSessionFactory> factoryMap = new ConcurrentHashMap<>();

  private static final Logger logger = LoggerFactory.getLogger(MyBatisUtil.class);

  //environment
  public static final String MS = "ms";

  static {
    try {
      msInputStream = MyBatisUtil.class.getResourceAsStream(RESOURCE);
      SqlSessionFactory msFactory = new SqlSessionFactoryBuilder().build(msInputStream, MS);
      factoryMap.put(MS, msFactory);

    } catch (Throwable t) {
      logger.error("init MyBatisUtil cause an exception", t);
    }
  }

  public static SqlSession getSqlSession(String environment, boolean autoCommit) {
    SqlSessionFactory factory = factoryMap.get(environment);
    if (factory == null) {
      logger.error("cannot find sqlSession for environment {}", environment);
      return null;
    }
    return factory.openSession(autoCommit);
  }
}