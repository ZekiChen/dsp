package com.tecdo.service.cache;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.tecdo.common.constant.CacheConstant;
import com.tecdo.starter.redis.PacRedis;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Created by Zeki on 2023/12/6
 */
@Service
@RequiredArgsConstructor
public class PixalateCache {

    private final PacRedis pacRedis;

    private final static String FRAUD_IP_CACHE = "ip";
    private final static String FRAUD_DEVICE_ID_CACHE = "deviceId";
    private final static String DAY_COUNT_CACHE = "day-count:v2";

    /**
     * 判断 ip 是否在作弊池中
     */
    public String getFraudByIp(String ip) {
        String key = CacheConstant.PIXALATE_CACHE
                .concat(StrUtil.COLON).concat(FRAUD_IP_CACHE)
                .concat(StrUtil.COLON).concat(ip);
        return pacRedis.get(key);
    }

    /**
     * 判断 设备id 是否在作弊池中
     */
    public String getFraudByDeviceId(String deviceId) {
        String key = CacheConstant.PIXALATE_CACHE
                .concat(StrUtil.COLON).concat(FRAUD_DEVICE_ID_CACHE)
                .concat(StrUtil.COLON).concat(deviceId);
        return pacRedis.get(key);
    }

    public void incrCheckCount() {
        String key = CacheConstant.PIXALATE_COUNT_CACHE
          .concat(StrUtil.COLON).concat(DAY_COUNT_CACHE)
          .concat(StrUtil.COLON).concat(DateUtil.today());
        pacRedis.incr(key);
        pacRedis.expire(key, CacheConstant.DAY_COUNT_EXP);
    }

    public Integer getCheckCountToday() {
        String key = CacheConstant.PIXALATE_COUNT_CACHE
          .concat(StrUtil.COLON).concat(DAY_COUNT_CACHE)
          .concat(StrUtil.COLON).concat(DateUtil.today());
        return pacRedis.getCounter(key);
    }
}
