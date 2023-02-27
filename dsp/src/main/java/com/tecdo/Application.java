package com.tecdo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by Zeki on 2022/12/28
 **/
@SpringBootApplication
@EnableScheduling
@MapperScan("com.tecdo.mapper.**")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
