package com.tecdo.service;

import com.tecdo.service.cache.FrequencyCache;
import com.tecdo.service.cache.NoticeCache;
import com.tecdo.service.cache.RtaCache;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

}
