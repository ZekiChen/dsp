package com.tecdo.adm.audience.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.audience.entity.AfContainer;
import com.tecdo.adm.api.audience.vo.SimpleAfContainerVO;
import com.tecdo.adm.api.audience.mapper.AfContainerMapper;
import com.tecdo.adm.audience.service.IAfContainerService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Zeki on 2023/4/5
 */
@Service
public class AfContainerServiceImpl extends ServiceImpl<AfContainerMapper, AfContainer> implements IAfContainerService {

    @Override
    public List<SimpleAfContainerVO> listSimple() {
        return baseMapper.listSimple();
    }
}
