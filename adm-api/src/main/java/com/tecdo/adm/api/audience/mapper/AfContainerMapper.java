package com.tecdo.adm.api.audience.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tecdo.adm.api.audience.entity.AfContainer;
import com.tecdo.adm.api.audience.vo.SimpleAfContainerVO;

import java.util.List;

/**
 * Created by Zeki on 2023/4/4
 */
@DS("afmysql")
public interface AfContainerMapper extends BaseMapper<AfContainer> {

    List<SimpleAfContainerVO> listSimple();

}