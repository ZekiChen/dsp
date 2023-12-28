package com.tecdo.adm.api.delivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.starter.mp.vo.BaseVO;

import java.util.List;

/**
 * 渠道信息 Mapper接口
 *
 * Created by Zeki on 2022/12/26
 **/
public interface AffiliateMapper extends BaseMapper<Affiliate> {
    List<BaseVO> listIdAndName();
}