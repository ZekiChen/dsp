package com.tecdo.core.launch.thread.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by Zeki on 2023/2/28
 */
@Getter
@Setter
@ConfigurationProperties("pac.thread-pool")
public class ThreadProperties {

    /**
     * 核心线程数
     */
    private String coreSize;
}
