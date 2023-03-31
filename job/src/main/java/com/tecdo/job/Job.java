package com.tecdo.job;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.tecdo.common.constant.AppConstant;
import com.tecdo.core.launch.PacApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * xxl-job 定时任务
 *
 * Created by Zeki on 2023/2/28
 */
@SpringBootApplication
@EnableApolloConfig
@EnableScheduling
@MapperScan("com.tecdo.**.mapper.**")
public class Job {

    public static void main(String[] args) {
        PacApplication.run(AppConstant.JOB, Job.class, args);
    }
}
