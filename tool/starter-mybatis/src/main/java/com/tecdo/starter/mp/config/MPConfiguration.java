package com.tecdo.starter.mp.config;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.tecdo.starter.mp.interceptor.PacPaginationInterceptor;
import com.tecdo.starter.mp.interceptor.QueryInterceptor;
import com.tecdo.starter.mp.props.MPProperties;
import lombok.AllArgsConstructor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Created by Zeki on 2023/3/13
 */
@AutoConfiguration
@AllArgsConstructor
@MapperScan("com.tecdo.pac.**.mapper.**")
@EnableConfigurationProperties(MPProperties.class)
public class MPConfiguration {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(MPProperties mpProperties, ObjectProvider<QueryInterceptor[]> queryInterceptors) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PacPaginationInterceptor paginationInterceptor = buildPaginationInterceptor(mpProperties, queryInterceptors);
        interceptor.addInnerInterceptor(paginationInterceptor);
        return interceptor;
    }

    private PacPaginationInterceptor buildPaginationInterceptor(MPProperties mpProperties, ObjectProvider<QueryInterceptor[]> queryInterceptors) {
        PacPaginationInterceptor paginationInterceptor = new PacPaginationInterceptor();
        QueryInterceptor[] queryInterceptorsArr = queryInterceptors.getIfAvailable();
        if (ObjectUtils.isNotEmpty(queryInterceptorsArr)) {
            paginationInterceptor.setQueryInterceptors(queryInterceptorsArr);
        }
        paginationInterceptor.setOverflow(mpProperties.getOverflow());
        paginationInterceptor.setOptimizeJoin(mpProperties.getOptimizeJoin());
        return paginationInterceptor;
    }
}
