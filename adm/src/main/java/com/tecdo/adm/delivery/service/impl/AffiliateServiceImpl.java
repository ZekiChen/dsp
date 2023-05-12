package com.tecdo.adm.delivery.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.delivery.entity.AffCountryBundleBList;
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
    public Boolean updateCountryBundleBLists(List<AffCountryBundleBList> bLists) {
        if (CollUtil.isEmpty(bLists)) {
            return false;
        }
        Integer affiliateId = bLists.get(0).getAffiliateId();
        affCountryBundleBListService.remove(Wrappers.<AffCountryBundleBList>lambdaQuery().eq(AffCountryBundleBList::getAffiliateId, affiliateId));
        affCountryBundleBListService.saveBatch(bLists);
        List<Integer> bListIds = bLists.stream().filter(e -> StrUtil.isBlank(e.getBundle()))
                .map(AffCountryBundleBList::getId).collect(Collectors.toList());
        affCountryBundleBListService.removeBatchByIds(bListIds);
        return true;
    }
}
