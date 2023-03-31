package com.tecdo.starter.mp.interceptor;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.util.ObjectUtils;

/**
 * Created by Zeki on 2023/3/13
 */
public class QueryInterceptorExecutor {

    public static void exec(QueryInterceptor[] interceptors, Executor executor, MappedStatement ms, Object parameter,
                            RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        if (ObjectUtils.isEmpty(interceptors)) {
            return;
        }
        for (QueryInterceptor interceptor : interceptors) {
            interceptor.intercept(executor, ms, parameter, rowBounds, resultHandler, boundSql);
        }
    }
}
