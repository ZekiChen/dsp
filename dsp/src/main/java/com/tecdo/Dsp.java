package com.tecdo;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.tecdo.common.constant.AppConstant;
import com.tecdo.core.launch.PacApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * DSP 竞价引擎
 * <p>
 * Created by Zeki on 2022/12/28
 **/
@SpringBootApplication
@EnableApolloConfig
@MapperScan("com.tecdo.**.mapper.**")
public class Dsp {

    public static long serverStartTime;

    public static void main(String[] args) {
        serverStartTime = System.currentTimeMillis();
        PacApplication.run(AppConstant.DSP, Dsp.class, args);
    }
}
