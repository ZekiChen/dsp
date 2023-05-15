package com.tecdo.mapper.doris;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tecdo.entity.doris.GooglePlayApp;

@DS("doris-ods")
public interface GooglePlayAppMapper extends BaseMapper<GooglePlayApp> {
}
