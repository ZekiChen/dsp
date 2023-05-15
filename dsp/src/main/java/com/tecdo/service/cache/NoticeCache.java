package com.tecdo.service.cache;

import cn.hutool.core.util.StrUtil;
import com.tecdo.common.constant.CacheConstant;
import com.tecdo.domain.biz.notice.NoticeInfo;
import com.tecdo.starter.redis.PacRedis;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 通知相关 缓存操作
 *
 * Created by Zeki on 2023/4/11
 */
@Service
@RequiredArgsConstructor
public class NoticeCache {

    private final PacRedis pacRedis;

    private final static String HAS_WIN_CACHE = "has-win";
    private final static String HAS_IMP_CACHE = "has-imp";
    private final static String HAS_CLICK_CACHE = "has-click";

    private final static String BID_ID_CACHE = "bidId:v2";

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
        return pacRedis.exists(key);
    }

    public void setNoticeInfo(String bidId, NoticeInfo noticeInfo) {
        String key = CacheConstant.NOTICE_CACHE
                .concat(StrUtil.COLON).concat(BID_ID_CACHE)
                .concat(StrUtil.COLON).concat(bidId);
        pacRedis.setIfAbsent(key, noticeInfo, pbExpire, TimeUnit.SECONDS);
    }

    public NoticeInfo getNoticeInfo(String bidId) {
        String key = CacheConstant.NOTICE_CACHE
                .concat(StrUtil.COLON).concat(BID_ID_CACHE)
                .concat(StrUtil.COLON).concat(bidId);
        return pacRedis.get(key);
    }
}
