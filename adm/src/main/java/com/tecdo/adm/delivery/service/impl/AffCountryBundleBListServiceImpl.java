package com.tecdo.adm.delivery.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.delivery.entity.AffCountryBundleList;
import com.tecdo.adm.api.delivery.mapper.AffCountryBundleListMapper;
import com.tecdo.adm.delivery.service.IAffCountryBundleBListService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Created by Zeki on 2023/3/15
 */
@Service
@RequiredArgsConstructor
public class AffCountryBundleBListServiceImpl extends ServiceImpl<AffCountryBundleListMapper, AffCountryBundleList> implements IAffCountryBundleBListService {
}
