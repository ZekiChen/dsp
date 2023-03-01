package com.tecdo.common.launch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.*;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static com.tecdo.common.constant.LaunchConstant.*;

/**
 * 应用启动器
 * <p>
 * Created by Zeki on 2023/2/22
 */
@Slf4j
public class PacApplication {

    /**
     * 服务启动器，微服务核心拓展、集成不同组件
     *
     * @param appName       应用名称
     * @param primarySource 资源类
     * @param args          启动参数
     * @return org.springframework.context.ConfigurableApplicationContext
     */
    public static ConfigurableApplicationContext run(String appName, Class<?> primarySource, String... args) {
        SpringApplicationBuilder builder = createSpringApplicationBuilder(appName, primarySource, args);
        return builder.run(args);
    }

    private static SpringApplicationBuilder createSpringApplicationBuilder(String appName, Class<?> primarySource, String[] args) {
        Assert.hasText(appName, "[appName]服务名不能为空");
        SpringApplicationBuilder builder = new SpringApplicationBuilder(primarySource);

        // 获取当前环境变量，默认 dev
        String activeProfile = getCurrentEnv(args, builder);
        log.info("{} 服务启动中, 当前运行环境：{}", appName, activeProfile);

        Properties props = System.getProperties();
        props.setProperty("spring.profiles.active", activeProfile);
        props.setProperty("env", activeProfile);  // Apollo
        props.setProperty("xxl.job.executor.appname", appName + "-service");  // 有长度要求
        props.setProperty("logging.config", String.format("classpath:log/log4j2-%s.xml", activeProfile));

        return builder;
    }

    private static String getCurrentEnv(String[] args, SpringApplicationBuilder builder) {
        ConfigurableEnvironment environment = new StandardEnvironment();
        MutablePropertySources propertySources = environment.getPropertySources();
        propertySources.addFirst(new SimpleCommandLinePropertySource(args));
        propertySources.addLast(new MapPropertySource(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME, environment.getSystemProperties()));
        propertySources.addLast(new SystemEnvironmentPropertySource(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, environment.getSystemEnvironment()));
        // 获取配置的环境变量
        String[] activeProfiles = environment.getActiveProfiles();
        // 配置的环境变量与预设环境变量：dev、test、prod 取交集，得到 activeProfile
        List<String> profiles = Arrays.asList(environment.getActiveProfiles());
        List<String> presetProfiles = new ArrayList<>(Arrays.asList(ENV_DEV, ENV_TEST, ENV_PROD));
        presetProfiles.retainAll(profiles);
        List<String> activeProfileList = new ArrayList<>(presetProfiles);
        String activeProfile;
        // 默认为 dev，存在两个以上环境抛出异常
        if (activeProfileList.isEmpty()) {
            activeProfile = ENV_DEV;
            activeProfileList.add(ENV_DEV);
            builder.profiles(ENV_DEV);
        } else if (activeProfileList.size() == 1) {
            activeProfile = activeProfileList.get(0);
        } else {
            throw new RuntimeException(String.format("同时存在环境变量:[%s]", StringUtils.arrayToCommaDelimitedString(activeProfiles)));
        }
        return activeProfile;
    }

}
