package com.tecdo.adm.api.delivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tecdo.adm.api.delivery.entity.Adv;
import com.tecdo.adm.api.delivery.vo.SimpleAdvVO;

import java.util.List;

/**
 * Created by Zeki on 2023/4/4
 */
public interface AdvMapper extends BaseMapper<Adv> {

    List<SimpleAdvVO> listIdAndName();
}