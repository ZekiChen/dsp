package com.tecdo.service.cache;

import cn.hutool.core.util.StrUtil;
import com.tecdo.common.constant.CacheConstant;
import com.tecdo.constant.CountryConstant;
import com.tecdo.starter.redis.PacRedis;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.tecdo.constant.CountryConstant.*;

/**
 * 通知相关 缓存操作
 *
 * Created by Zeki on 2023/4/11
 */
@Service
@RequiredArgsConstructor
public class NoticeCache {

    private final PacRedis pacRedis;
    private final RedisTemplate<String, String> mxRedisTemplate;
    private final RedisTemplate<String, String> parRedisTemplate;

    private final static String HAS_WIN_CACHE = "has-win";
    private final static String HAS_LOSS_CACHE = "has-loss";
    private final static String HAS_IMP_CACHE = "has-imp";
    private final static String HAS_CLICK_CACHE = "has-click";

    private static Map<Character, String> areaSlot = new HashMap<>();

    static {
        areaSlot.put('a', SINGAPORE);
        areaSlot.put('b', MEXICO);
        areaSlot.put('c', PARIS);
    }

    @Value("${pac.notice.expire.click}")
    private long clickExpire;
    @Value("${pac.notice.expire.pb}")
    private long pbExpire;

    /**
     * 竞价成功的记录时间为点击窗口
     */
    public boolean winMark(String bidId) {
        String key = CacheConstant.WIN_CACHE
                .concat(StrUtil.COLON).concat(HAS_WIN_CACHE)
                .concat(StrUtil.COLON).concat(bidId);
        return pacRedis.setIfAbsent(key, 1, clickExpire, TimeUnit.SECONDS);
    }

    /**
     * 竞价失败的记录时间为点击窗口
     */
    public boolean lossMark(String bidId) {
        String key = CacheConstant.LOSS_CACHE
          .concat(StrUtil.COLON).concat(HAS_LOSS_CACHE)
          .concat(StrUtil.COLON).concat(bidId);
        return pacRedis.setIfAbsent(key, 1, clickExpire, TimeUnit.SECONDS);
    }

    /**
     * 曝光的记录时间为点击窗口
     */
    public boolean impMark(String bidId) {
        String key = CacheConstant.IMP_CACHE
                .concat(StrUtil.COLON).concat(HAS_IMP_CACHE)
                .concat(StrUtil.COLON).concat(bidId);
        return pacRedis.setIfAbsent(key, 1, clickExpire, TimeUnit.SECONDS);
    }

    /**
     * 点击的记录时间为归因窗口
     */
    public boolean clickMark(String bidId) {
        String key = CacheConstant.CLICK_CACHE
                .concat(StrUtil.COLON).concat(HAS_CLICK_CACHE)
                .concat(StrUtil.COLON).concat(bidId);
        return pacRedis.setIfAbsent(key, 1, pbExpire, TimeUnit.SECONDS);
    }

    public Boolean hasWin(String bidId) {
        String key = CacheConstant.WIN_CACHE
                .concat(StrUtil.COLON).concat(HAS_WIN_CACHE)
                .concat(StrUtil.COLON).concat(bidId);
        return pacRedis.exists(key);
    }

    public Boolean hasImp(String bidId) {
        String key = CacheConstant.IMP_CACHE
                .concat(StrUtil.COLON).concat(HAS_IMP_CACHE)
                .concat(StrUtil.COLON).concat(bidId);
        return pacRedis.exists(key);
    }

    public Boolean hasClick(String bidId) {
        String key = CacheConstant.CLICK_CACHE
                .concat(StrUtil.COLON).concat(HAS_CLICK_CACHE)
                .concat(StrUtil.COLON).concat(bidId);
        return doHasClickByDiffArea(bidId.charAt(0), key);
    }

    private Boolean doHasClickByDiffArea(char slot, String key) {
        switch (areaSlot.get(slot)) {
            case MEXICO:
                return mxRedisTemplate.hasKey(key);
            case PARIS:
                return parRedisTemplate.hasKey(key);
            case SINGAPORE:
            default:
                return pacRedis.exists(key);
        }
    }

}
