package com.tecdo.job;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.tecdo.common.launch.PacApplication;
import com.tecdo.common.constant.AppConstant;
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
@MapperScan("com.tecdo.job.mapper.**")
public class Job {

    public static void main(String[] args) {
        PacApplication.run(AppConstant.APP_NAME_JOB, Job.class, args);
    }
}
