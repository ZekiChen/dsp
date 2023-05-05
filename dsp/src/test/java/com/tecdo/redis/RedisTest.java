package com.tecdo.redis;

import com.tecdo.domain.biz.notice.NoticeInfo;
import com.tecdo.service.CacheService;
import com.tecdo.service.rta.ae.AeRtaInfoVO;
import com.tecdo.starter.redis.PacRedis;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
    public void test_PacRedis_protostuff_field_ignore_or_not() {
        NoticeInfo info = new NoticeInfo();
        info.setCampaignId(123);
        info.setAdGroupId(456);
        info.setAdId(789);
        info.setCreativeId(101112);
        info.setDeviceId("123asdas1");
        cacheService.getNoticeCache().setNoticeInfo("bid666", info);

        NoticeInfo noticeInfo = cacheService.getNoticeCache().getNoticeInfo("bid666");
        System.out.println(noticeInfo);
    }

}
