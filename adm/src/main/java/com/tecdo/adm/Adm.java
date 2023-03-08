package com.tecdo.adm;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.tecdo.common.constant.AppConstant;
import com.tecdo.core.launch.PacApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 广告管理
 *
 * Created by Zeki on 2023/3/6
 */
@EnableApolloConfig
@SpringBootApplication
@MapperScan("com.tecdo.**.mapper.**")
public class Adm {
    public static void main(String[] args) {
        PacApplication.run(AppConstant.ADM, Adm.class, args);
    }
}