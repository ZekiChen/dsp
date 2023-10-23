package com.tecdo.job.handler.condition;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Created by Zeki on 2023/10/23
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AutoBundleJob {

    @XxlJob("autoBundleRefresh")
    public void autoBundleRefresh() {

    }
}
