package com.tecdo;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.tecdo.common.constant.AppConstant;
import com.tecdo.core.launch.PacApplication;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableApolloConfig
@MapperScan("com.tecdo.mapper.**")
public class Script {

    public static void main(String[] args) {
        PacApplication.run(AppConstant.SCRIPT, Script.class, args);
    }
}
