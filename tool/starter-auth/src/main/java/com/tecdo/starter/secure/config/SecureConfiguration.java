package com.tecdo.starter.secure.config;

import com.tecdo.starter.secure.props.SecureProperties;
import com.tecdo.starter.secure.provider.ClientDetailsServiceImpl;
import com.tecdo.starter.secure.provider.IClientDetailsService;
import com.tecdo.starter.secure.registry.SecureRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Created by Zeki on 2023/3/14
 */
@Order
@AutoConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties(SecureProperties.class)
public class SecureConfiguration {
//public class SecureConfiguration implements WebMvcConfigurer {

    private final JdbcTemplate jdbcTemplate;

    private final SecureProperties secureProperties;
    private final SecureRegistry secureRegistry;

//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        // 设置请求授权
//    }

    @Bean
    @ConditionalOnMissingBean(IClientDetailsService.class)
    public IClientDetailsService clientDetailsService() {
        return new ClientDetailsServiceImpl(jdbcTemplate);
    }

}
