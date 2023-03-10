package com.tecdo.xxljob.processor;

import com.tecdo.core.launch.processor.ILauncherProcessor;
import net.dreamlu.mica.auto.annotation.AutoService;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.Ordered;

import java.util.Properties;

/**
 * Created by Zeki on 2023/3/9
 */
@AutoService(ILauncherProcessor.class)
public class XxlJobLauncherProcessor implements ILauncherProcessor {

    @Override
    public void processor(SpringApplicationBuilder builder, String appName, String activeProfile) {
        Properties props = System.getProperties();
        props.setProperty("xxl.job.executor.appname", appName + "-service");  // 有长度要求
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

}
