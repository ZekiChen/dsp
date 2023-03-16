package com.tecdo.adm.api.delivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tecdo.adm.api.delivery.entity.AdGroup;
import com.tecdo.starter.mp.vo.BaseVO;

import java.util.List;

/**
 * 广告组信息 Mapper接口
 *
 * Created by Zeki on 2022/12/26
 **/
public interface AdGroupMapper extends BaseMapper<AdGroup> {

    List<BaseVO> listIdAndName();
}