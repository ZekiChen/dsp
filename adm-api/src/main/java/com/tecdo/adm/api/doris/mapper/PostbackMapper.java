package com.tecdo.adm.api.doris.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tecdo.adm.api.delivery.dto.ReportEventDTO;
import com.tecdo.adm.api.doris.entity.Postback;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 展示实时数据 Mapper
 *
 * Created by Zeki on 2023/2/21
 */
@DS("doris-ads")
public interface PostbackMapper extends BaseMapper<Postback> {

    ReportEventDTO getRepostEventForLazada(@Param("createTimes") List<String> createTimes,
                                           @Param("adGroupIds") List<Integer> adGroupIds);
}
