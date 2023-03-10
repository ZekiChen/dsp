package com.tecdo.adm.delivery.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.delivery.entity.Ad;
import com.tecdo.adm.api.delivery.mapper.AdMapper;
import com.tecdo.adm.delivery.service.IAdService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Zeki on 2023/3/6
 */
@Service
@RequiredArgsConstructor
public class AdServiceImpl extends ServiceImpl<AdMapper, Ad> implements IAdService {

    @Override
    public void deleteByAdGroupIds(List<Integer> adGroupIds) {
        baseMapper.delete(Wrappers.<Ad>lambdaQuery().in(Ad::getGroupId, adGroupIds));
    }
}
