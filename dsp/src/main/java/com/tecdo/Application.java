package com.tecdo;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.tecdo.common.launch.PacApplication;
import com.tecdo.constant.AppConstant;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by Zeki on 2022/12/28
 **/
@SpringBootApplication
@EnableApolloConfig
@EnableScheduling
@MapperScan("com.tecdo.mapper.**")
public class Application {

    public static void main(String[] args) {
        PacApplication.run(AppConstant.APP_NAME_DSP, Application.class, args);
    }
}