package com.tecdo.adm.api.delivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tecdo.adm.api.delivery.entity.Ad;
import com.tecdo.adm.api.delivery.vo.SimpleAdVO;
import com.tecdo.starter.mp.entity.StatusEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 广告信息 Mapper接口
 *
 * Created by Zeki on 2022/12/26
 **/
public interface AdMapper extends BaseMapper<Ad> {

    List<SimpleAdVO> listSimpleAd(@Param("adGroupIds") List<Integer> adGroupIds);

    List<Integer> listIdByGroupIds(@Param("adGroupIds") List<Integer> adGroupIds);

    List<StatusEntity> listStatus(@Param("ids") List<Integer> ids);
}