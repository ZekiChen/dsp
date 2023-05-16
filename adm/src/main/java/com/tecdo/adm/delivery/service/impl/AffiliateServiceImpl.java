package com.tecdo.adm.delivery.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.delivery.entity.AffCountryBundleList;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.mapper.AffiliateMapper;
import com.tecdo.adm.delivery.service.IAffCountryBundleBListService;
import com.tecdo.adm.delivery.service.IAffiliateService;
import com.tecdo.starter.mp.vo.BaseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Zeki on 2023/3/15
 */
@Service
@RequiredArgsConstructor
public class AffiliateServiceImpl extends ServiceImpl<AffiliateMapper, Affiliate> implements IAffiliateService {

    private final IAffCountryBundleBListService affCountryBundleBListService;

    @Override
    public List<BaseVO> listIdAndName() {
        return baseMapper.listIdAndName();
    }

    @Override
    @Transactional
    public Boolean updateCountryBundleLists(List<AffCountryBundleList> lists) {
        if (CollUtil.isEmpty(lists)) {
            return false;
        }
        Integer affiliateId = lists.get(0).getAffiliateId();
        affCountryBundleBListService.remove(Wrappers.<AffCountryBundleList>lambdaQuery().eq(AffCountryBundleList::getAffiliateId, affiliateId));
        affCountryBundleBListService.saveBatch(lists);
        List<Integer> bListIds = lists.stream().filter(e -> StrUtil.isBlank(e.getBundle()))
                .map(AffCountryBundleList::getId).collect(Collectors.toList());
        affCountryBundleBListService.removeBatchByIds(bListIds);
        return true;
    }

    @Override
    public List<AffCountryBundleList> affCountryBundleLists(Integer affiliateId) {
        return affCountryBundleBListService.list(Wrappers.<AffCountryBundleList>lambdaQuery()
                .eq(AffCountryBundleList::getAffiliateId, affiliateId));
    }
}
