package com.tecdo.adm.delivery.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.delivery.entity.Adsize;
import com.tecdo.adm.api.delivery.entity.Creative;
import com.tecdo.adm.api.delivery.mapper.AdsizeMapper;
import com.tecdo.adm.api.delivery.mapper.CreativeMapper;
import com.tecdo.adm.api.delivery.vo.AdsizeVO;
import com.tecdo.adm.delivery.service.IAdsizeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdsizeServiceImpl extends ServiceImpl<AdsizeMapper, Adsize> implements IAdsizeService {
    @Override
    public List<AdsizeVO> standardSpecs()  {
        return baseMapper.standardSpecs();
    }
}
