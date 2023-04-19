package com.tecdo.adm.api.delivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tecdo.adm.api.delivery.entity.AdGroup;
import com.tecdo.starter.mp.vo.BaseVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 广告组信息 Mapper接口
 *
 * Created by Zeki on 2022/12/26
 **/
public interface AdGroupMapper extends BaseMapper<AdGroup> {

    List<BaseVO> listIdAndName();

    IPage<AdGroup> customPage(IPage<AdGroup> page, @Param("param") AdGroup adGroup,
                              @Param("campaignIds") List<Integer> campaignIds, @Param("affiliateName") String affiliateName);
}