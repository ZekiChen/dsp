package com.tecdo.service.cache;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.tecdo.common.constant.CacheConstant;
import com.tecdo.starter.redis.PacRedis;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 频率控制 缓存操作
 *
 * Created by Zeki on 2023/4/11
 */
@Service
@RequiredArgsConstructor
public class FrequencyCache {

    private final PacRedis pacRedis;

    private final static String DAY_COUNT_CACHE = "day-count";

    /**
     * 展示次数统计：在每次 imp notice 中写入缓存，value 从 1 开始单调递增。24h 后自动过期
     */
    public void incrImpCount(String campaignId, String deviceId) {
        String key = CacheConstant.IMP_CACHE
                .concat(StrUtil.COLON).concat(DAY_COUNT_CACHE)
                .concat(StrUtil.COLON).concat(DateUtil.today())
                .concat(StrUtil.COLON).concat(campaignId)
                .concat(StrUtil.COLON).concat(deviceId);
        pacRedis.incr(key);
        pacRedis.expire(key, CacheConstant.DAY_COUNT_EXP);
    }

    /**
     * 点击次数统计：在每次 click notice 中写入缓存，value 从 1 开始单调递增。24h 后自动过期
     */
    public void incrClickCount(String campaignId, String deviceId) {
        String key = CacheConstant.CLICK_CACHE
                .concat(StrUtil.COLON).concat(DAY_COUNT_CACHE)
                .concat(StrUtil.COLON).concat(DateUtil.today())
                .concat(StrUtil.COLON).concat(campaignId)
                .concat(StrUtil.COLON).concat(deviceId);
        pacRedis.incr(key);
        pacRedis.expire(key, CacheConstant.DAY_COUNT_EXP);
    }

    public Integer getImpCountToday(String campaignId, String deviceId) {
        String key = CacheConstant.IMP_CACHE
                .concat(StrUtil.COLON).concat(DAY_COUNT_CACHE)
                .concat(StrUtil.COLON).concat(DateUtil.today())
                .concat(StrUtil.COLON).concat(campaignId)
                .concat(StrUtil.COLON).concat(deviceId);
        return pacRedis.getCounter(key);
    }

    public Integer getClickCountToday(String campaignId, String deviceId) {
        String key = CacheConstant.CLICK_CACHE
                .concat(StrUtil.COLON).concat(DAY_COUNT_CACHE)
                .concat(StrUtil.COLON).concat(DateUtil.today())
                .concat(StrUtil.COLON).concat(campaignId)
                .concat(StrUtil.COLON).concat(deviceId);
        return pacRedis.getCounter(key);
    }
}
