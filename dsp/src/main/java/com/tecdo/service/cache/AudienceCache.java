package com.tecdo.service.cache;

import com.tecdo.common.constant.CacheConstant;
import com.tecdo.entity.AfSync;
import com.tecdo.service.init.AfAudienceSyncManager;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AudienceCache {

  private final static String AF_CACHE = "af";

  private final RedissonClient redissonClient;

  private final AfAudienceSyncManager afAudienceSyncManager;


  public Boolean existInBloomFilter(String key, String value) {
    RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(key);
    if (bloomFilter.isExists()) {
      return bloomFilter.contains(value);
    }
    return false;
  }

  public String getAudienceSyncBloomFilterKey(Integer containerId) {
    List<AfSync> afSyncList = afAudienceSyncManager.getAfSyncList(containerId);
    if (!CollectionUtils.isEmpty(afSyncList)) {
      Optional<AfSync> maxItem =
        afSyncList.stream().max(Comparator.comparing(AfSync::getVersionMillis));
      return maxItem.map(afSync -> CacheConstant.AUDIENCE_CACHE.concat(StrUtil.COLON)
                                                               .concat(AF_CACHE)
                                                               .concat(StrUtil.COLON)
                                                               .concat(maxItem.get()
                                                                              .getContainerId()
                                                                              .toString())
                                                               .concat(StrUtil.COLON)
                                                               .concat(maxItem.get()
                                                                              .getVersionMillis()
                                                                              .toString()))
                    .orElse(null);
    }
    return null;
  }

}
