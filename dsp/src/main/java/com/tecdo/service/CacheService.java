package com.tecdo.service;

import com.tecdo.service.cache.*;

import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
    private final AudienceCache audienceCache;
    private final ForceCache forceCache;
}
