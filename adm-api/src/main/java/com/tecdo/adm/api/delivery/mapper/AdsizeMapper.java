package com.tecdo.adm.api.delivery.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tecdo.adm.api.delivery.entity.Adsize;
import com.tecdo.adm.api.delivery.vo.AdsizeVO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 创意的标准规格 Mapper接口
 *
 * Created by Elwin on 2023/9/12
 */
@DS("doris-ads")
public interface AdsizeMapper extends BaseMapper<Adsize> {
    @Select("select * from adsize")
    List<AdsizeVO> standardSpecs();
}
