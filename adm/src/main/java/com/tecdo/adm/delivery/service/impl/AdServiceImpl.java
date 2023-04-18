package com.tecdo.adm.delivery.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.delivery.entity.Ad;
import com.tecdo.adm.api.delivery.mapper.AdMapper;
import com.tecdo.adm.delivery.service.IAdService;
import com.tecdo.starter.mp.entity.BaseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Zeki on 2023/3/6
 */
@Service
@RequiredArgsConstructor
public class AdServiceImpl extends ServiceImpl<AdMapper, Ad> implements IAdService {

    @Override
    public List<Ad> listByAdGroupId(Integer adGroupId) {
        return baseMapper.selectList(Wrappers.<Ad>lambdaQuery().eq(Ad::getGroupId, adGroupId));
    }

    @Override
    public void deleteByAdGroupIds(List<Integer> adGroupIds) {
        baseMapper.delete(Wrappers.<Ad>lambdaQuery().in(Ad::getGroupId, adGroupIds));
    }

    @Override
    public boolean copy(Integer sourceAdId, List<Integer> targetAdGroupIds, Integer targetAdStatus) {
        if (CollUtil.isEmpty(targetAdGroupIds)) {
            return false;
        }
        Ad sourceAd = getById(sourceAdId);
        List<Ad> targetAds = targetAdGroupIds.stream().map(groupId -> {
            Ad targetAd = BeanUtil.copyProperties(sourceAd, Ad.class);
            resetBaseEntity(targetAd);
            targetAd.setGroupId(groupId);
            targetAd.setStatus(targetAdStatus);
            return targetAd;
        }).collect(Collectors.toList());
        saveBatch(targetAds);
        return true;
    }

    private static void resetBaseEntity(BaseEntity entity) {
        entity.setId(null);
        entity.setCreateTime(null);
        entity.setUpdateTime(null);
    }
}
