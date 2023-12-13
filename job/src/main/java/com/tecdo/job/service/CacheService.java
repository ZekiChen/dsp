package com.tecdo.job.service;

import com.tecdo.job.service.cache.PixalateCache;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Created by Zeki on 2023/12/6
 */
@Getter
@Service
@RequiredArgsConstructor
public class CacheService {

    private final PixalateCache pixalateCache;
}
