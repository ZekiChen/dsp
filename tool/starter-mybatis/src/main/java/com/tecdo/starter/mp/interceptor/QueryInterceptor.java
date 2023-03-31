package com.tecdo.starter.mp.interceptor;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.core.Ordered;

/**
 * Created by Zeki on 2023/3/13
 */
@SuppressWarnings({"rawtypes"})
public interface QueryInterceptor extends Ordered {

	void intercept(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql);

	@Override
	default int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}
}
