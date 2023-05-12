package com.tecdo.service;

import com.tecdo.service.cache.FrequencyCache;
import com.tecdo.service.cache.NoticeCache;
import com.tecdo.service.cache.RtaCache;
import lombok.Getter;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.tecdo.common.constant.CacheConstant;
import com.tecdo.entity.AfSync;
import com.tecdo.service.init.AfAudienceSyncManager;
import com.tecdo.starter.redis.PacRedis;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 协助缓存读写
 * <p>
 * Created by Zeki on 2023/2/6
 */
@Getter
@Service
@RequiredArgsConstructor
public class CacheService {

    private final FrequencyCache frequencyCache;
    private final NoticeCache noticeCache;
    private final RtaCache rtaCache;
    private final PacRedis pacRedis;

    private final RedissonClient redissonClient;

    private final AfAudienceSyncManager afAudienceSyncManager;

    private final static String DAY_COUNT_CACHE = "day-count";
    private final static String HAS_WIN_CACHE = "has-win";
    private final static String HAS_IMP_CACHE = "has-imp";
    private final static String HAS_CLICK_CACHE = "has-click";
    private final static String AF_CACHE = "af";

    @Value("${pac.notice.expire.click}")
    private long clickExpire;

    @Value("${pac.notice.expire.pb}")
    private long pbExpire;

    public Boolean existInBloomFilter(String key, String value) {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(key);
        if (bloomFilter.isExists()) {
            return bloomFilter.contains(value);
        }
        return false;
    }

    public String getAudienceSyncBloomFilterKey(Integer containerId) {
        List<AfSync> afSyncList = afAudienceSyncManager.getAfSyncList(containerId);
        if(!CollectionUtils.isEmpty(afSyncList)) {
            Optional<AfSync> maxItem = afSyncList.stream()
                    .max(Comparator.comparing(AfSync::getVersionMillis));
            return maxItem.map(afSync -> CacheConstant.AUDIENCE_CACHE
              .concat(StrUtil.COLON).concat(AF_CACHE)
              .concat(StrUtil.COLON).concat(maxItem.get().getContainerId().toString())
              .concat(StrUtil.COLON).concat(maxItem.get().getVersionMillis().toString()))
                          .orElse(null);
        }
        return null;
    }
}
