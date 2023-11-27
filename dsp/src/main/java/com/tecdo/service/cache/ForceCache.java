package com.tecdo.service.cache;

import cn.hutool.core.util.StrUtil;
import com.tecdo.common.constant.CacheConstant;
import com.tecdo.starter.redis.PacRedis;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Created by Zeki on 2023/11/27
 */
@Service
@RequiredArgsConstructor
public class ForceCache {

    private final PacRedis pacRedis;
    private final static String HAS_FORCE_IMP_CACHE = "has-force-imp";

    @Value("${pac.notice.expire.click}")
    private long clickExpire;

    /**
     * 强跳曝光的记录时间为点击窗口
     */
    public boolean impMarkIfAbsent(String bidId) {
        String key = CacheConstant.IMP_CACHE
                .concat(StrUtil.COLON).concat(HAS_FORCE_IMP_CACHE)
                .concat(StrUtil.COLON).concat(bidId);
        return pacRedis.setIfAbsent(key, 1, clickExpire, TimeUnit.SECONDS);
    }

}
