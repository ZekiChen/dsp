package com.tecdo.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.tecdo.common.cache.PacRedis;
import com.tecdo.constant.CacheConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 协助缓存读写
 * <p>
 * Created by Zeki on 2023/2/6
 */
@Service
@RequiredArgsConstructor
public class CacheService {

    private final static String DAY_COUNT_CACHE = "day-count";

    private final PacRedis pacRedis;

    /**
     * 展示次数统计：在每次 imp notice 中写入缓存，value 从 1 开始单调递增。24h 后自动过期
     */
    public void incrImpCount(String campaignId, String deviceId) {
        String key = CacheConstant.IMP_CACHE.concat(DAY_COUNT_CACHE)
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
        String key = CacheConstant.CLICK_CACHE.concat(DAY_COUNT_CACHE)
                .concat(StrUtil.COLON).concat(DateUtil.today())
                .concat(StrUtil.COLON).concat(campaignId)
                .concat(StrUtil.COLON).concat(deviceId);
        pacRedis.incr(key);
        pacRedis.expire(key, CacheConstant.DAY_COUNT_EXP);
    }

    public Integer getImpCountToday(String campaignId, String deviceId) {
        String key = CacheConstant.IMP_CACHE.concat(DAY_COUNT_CACHE)
                .concat(StrUtil.COLON).concat(DateUtil.today())
                .concat(StrUtil.COLON).concat(campaignId)
                .concat(StrUtil.COLON).concat(deviceId);
        return (Integer) Optional.ofNullable(pacRedis.get(key)).orElse(0);
    }

    public Integer getClickCountToday(String campaignId, String deviceId) {
        String key = CacheConstant.CLICK_CACHE.concat(DAY_COUNT_CACHE)
                .concat(StrUtil.COLON).concat(DateUtil.today())
                .concat(StrUtil.COLON).concat(campaignId)
                .concat(StrUtil.COLON).concat(deviceId);
        return (Integer) Optional.ofNullable(pacRedis.get(key)).orElse(0);
    }
}
