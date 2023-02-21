package com.tecdo.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.tecdo.common.cache.PacRedis;
import com.tecdo.constant.CacheConstant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 协助缓存读写
 * <p>
 * Created by Zeki on 2023/2/6
 */
@Service
@RequiredArgsConstructor
public class CacheService {

    private final PacRedis pacRedis;

    private final static String DAY_COUNT_CACHE = "day-count";
    private final static String HAS_WIN_CACHE = "has-win";
    private final static String HAS_IMP_CACHE = "has-imp";
    private final static String HAS_CLICK_CACHE = "has-click";

    @Value("${pac.notice.expire.click}")
    private long clickExpire;

    @Value("${pac.notice.expire.pb}")
    private long pbExpire;

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

    /**
     * 竞价成功和曝光的记录时间为点击的归因窗口
     */
    public boolean winMark(String bidId) {
        String key = CacheConstant.WIN_CACHE.concat(HAS_WIN_CACHE).concat(StrUtil.COLON).concat(bidId);
        return pacRedis.setIfAbsent(key, 1, clickExpire, TimeUnit.SECONDS);
    }

    public boolean impMark(String bidId) {
        String key = CacheConstant.IMP_CACHE.concat(HAS_IMP_CACHE).concat(StrUtil.COLON).concat(bidId);
        return pacRedis.setIfAbsent(key, 1, clickExpire, TimeUnit.SECONDS);
    }

    /**
     * 点击的记录时间为pb的归因窗口
     */
    public boolean clickMark(String bidId) {
        String key = CacheConstant.CLICK_CACHE.concat(HAS_CLICK_CACHE).concat(StrUtil.COLON).concat(bidId);
        return pacRedis.setIfAbsent(key, 1, pbExpire, TimeUnit.SECONDS);
    }

    public Boolean hasWin(String bidId) {
        String key = CacheConstant.WIN_CACHE.concat(HAS_WIN_CACHE).concat(StrUtil.COLON).concat(bidId);
        return pacRedis.exists(key);
    }

    public Boolean hasImp(String bidId) {
        String key = CacheConstant.IMP_CACHE.concat(HAS_IMP_CACHE).concat(StrUtil.COLON).concat(bidId);
        return pacRedis.exists(key);
    }

    public Boolean hasClick(String bidId) {
        String key = CacheConstant.CLICK_CACHE.concat(HAS_CLICK_CACHE).concat(StrUtil.COLON).concat(bidId);
        return pacRedis.exists(key);
    }
}