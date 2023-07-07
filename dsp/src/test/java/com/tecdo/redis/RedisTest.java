package com.tecdo.redis;

import com.tecdo.service.CacheService;
import com.tecdo.service.rta.ae.AeRtaInfoVO;
import com.tecdo.starter.redis.PacRedis;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

/**
 * Created by Zeki on 2023/5/4
 */
@SpringBootTest
public class RedisTest {

    @Autowired
    private PacRedis pacRedis;
    @Autowired
    private CacheService cacheService;

    @Test
    public void test_PacRedis_protostuff_serialize() {
        AeRtaInfoVO vo = new AeRtaInfoVO();
        vo.setTarget(true);
        vo.setAdvCampaignId("123456");
        vo.setLandingPage("https://test-landingPage.com");
        pacRedis.set("test", vo);
    }

    @Test
    public void test_PacRedis_protostuff_deserialize() {
        AeRtaInfoVO vo = pacRedis.get("test");
        System.out.println(vo);
    }

    @Test
    public void test_PacRedis_setIfAbsent() {
        pacRedis.setIfAbsent("test", 1, 3600L, TimeUnit.SECONDS);
        Boolean test = pacRedis.exists("test");
        System.out.println(test);
    }
}
