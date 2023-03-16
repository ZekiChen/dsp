package com.tecdo.adm.delivery.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.mapper.AffiliateMapper;
import com.tecdo.adm.delivery.service.IAffiliateService;
import com.tecdo.starter.mp.vo.BaseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Zeki on 2023/3/15
 */
@Service
@RequiredArgsConstructor
public class AffiliateServiceImpl extends ServiceImpl<AffiliateMapper, Affiliate> implements IAffiliateService {

    @Override
    public List<BaseVO> listIdAndName() {
        return baseMapper.listIdAndName();
    }
}
