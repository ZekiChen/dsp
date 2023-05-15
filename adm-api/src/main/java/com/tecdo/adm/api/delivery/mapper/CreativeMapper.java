package com.tecdo.adm.api.delivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tecdo.adm.api.delivery.entity.Creative;
import com.tecdo.adm.api.delivery.vo.CreativeSpecVO;

import java.util.List;

/**
 * 创意物料 Mapper接口
 *
 * Created by Zeki on 2022/12/26
 **/
public interface CreativeMapper extends BaseMapper<Creative> {

    List<CreativeSpecVO> listSpecs();
}