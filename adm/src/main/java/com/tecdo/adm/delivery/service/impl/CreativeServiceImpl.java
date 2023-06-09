package com.tecdo.adm.delivery.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.delivery.entity.Creative;
import com.tecdo.adm.api.delivery.mapper.CreativeMapper;
import com.tecdo.adm.api.delivery.vo.CreativeSpecVO;
import com.tecdo.adm.delivery.service.ICreativeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Zeki on 2023/3/10
 */
@Service
@RequiredArgsConstructor
public class CreativeServiceImpl extends ServiceImpl<CreativeMapper, Creative> implements ICreativeService {

    @Override
    public List<CreativeSpecVO> listSpecs() {
        return baseMapper.listSpecs();
    }

    @Override
    public List<Integer> listIdByLikeName(String name) {
        return baseMapper.listIdByLikeName(name);
    }

    @Override
    public List<Integer> listIdBySize(Integer width, Integer height) {
        return baseMapper.listIdBySize(width, height);
    }
}
