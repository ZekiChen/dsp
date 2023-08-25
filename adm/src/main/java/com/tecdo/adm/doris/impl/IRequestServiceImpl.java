package com.tecdo.adm.doris.impl;

import com.tecdo.adm.api.doris.mapper.RequestMapper;
import com.tecdo.adm.doris.IRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Zeki on 2023/4/5
 */
@Service
@RequiredArgsConstructor
public class IRequestServiceImpl implements IRequestService {

    private final RequestMapper requestMapper;


    @Override
    public String countDevice(String startDate, String endDate,
                              List<String> affiliates,
                              List<String> countries,
                              List<String> deviceOSs,
                              List<String> inBundles, List<String> exBundles) {

        return requestMapper.countDevice(startDate, endDate, affiliates, countries, deviceOSs, inBundles, exBundles);
    }
}
