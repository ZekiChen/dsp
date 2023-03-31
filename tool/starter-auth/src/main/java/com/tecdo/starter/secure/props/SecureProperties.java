package com.tecdo.starter.secure.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by Zeki on 2023/3/15
 */
@Data
@ConfigurationProperties("pac.secure")
public class SecureProperties {

    /**
     * 开启鉴权规则
     */
    private Boolean enabled = false;
}
