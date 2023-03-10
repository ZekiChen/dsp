package com.tecdo.adm.common.ping;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Zeki on 2023/3/8
 */
@RestController
public class HealthController {

    @GetMapping("/ping")
    public void ping() {
    }
}
