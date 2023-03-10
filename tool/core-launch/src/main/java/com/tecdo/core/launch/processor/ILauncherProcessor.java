package com.tecdo.core.launch.processor;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.Ordered;

/**
 * Created by Zeki on 2023/3/9
 */
public interface ILauncherProcessor extends Ordered, Comparable<ILauncherProcessor> {

    /**
     * 自定义组件服务处理方法
     */
    void processor(SpringApplicationBuilder builder, String appName, String activeProfile);

    @Override
    default int getOrder() {
        return 0;
    }

    @Override
    default int compareTo(ILauncherProcessor launchProcessor) {
        return Integer.compare(this.getOrder(), launchProcessor.getOrder());
    }

}
