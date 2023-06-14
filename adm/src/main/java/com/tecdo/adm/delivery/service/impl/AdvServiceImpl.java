package com.tecdo.adm.delivery.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.delivery.entity.Adv;
import com.tecdo.adm.api.delivery.mapper.AdvMapper;
import com.tecdo.adm.api.delivery.vo.SimpleAdvVO;
import com.tecdo.adm.delivery.service.IAdvService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Zeki on 2023/4/5
 */
@Service
public class AdvServiceImpl extends ServiceImpl<AdvMapper, Adv> implements IAdvService {

    @Override
    public List<SimpleAdvVO> listIdAndName() {
        return baseMapper.listIdAndName();
    }
}
