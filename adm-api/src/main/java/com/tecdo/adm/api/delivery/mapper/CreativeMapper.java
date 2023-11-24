package com.tecdo.adm.api.delivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tecdo.adm.api.delivery.entity.Creative;
import com.tecdo.adm.api.delivery.vo.CreativeSpecVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 创意物料 Mapper接口
 *
 * Created by Zeki on 2022/12/26
 **/
public interface CreativeMapper extends BaseMapper<Creative> {

    List<CreativeSpecVO> listSpecs();

    List<Integer> listIdByLikeName(@Param("name") String name);

    List<Integer> listIdBySize(@Param("width") Integer width, @Param("height") Integer height);

    @Select("SELECT MAX(external_id) FROM creative")
    Integer getMaxExteranlId();
}