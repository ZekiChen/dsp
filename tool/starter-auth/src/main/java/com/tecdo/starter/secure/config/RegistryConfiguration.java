package com.tecdo.starter.secure.config;

import com.tecdo.starter.secure.registry.SecureRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

/**
 * Created by Zeki on 2023/3/15
 */
@Order
@AutoConfiguration(before = SecureConfiguration.class)
public class RegistryConfiguration {

    @Bean
    @ConditionalOnMissingBean(SecureRegistry.class)
    public SecureRegistry secureRegistry() {
        return new SecureRegistry();
    }
}
