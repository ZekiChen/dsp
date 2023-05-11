package com.tecdo.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tecdo.entity.AfSync;

@DS("afmysql")
public interface AfSyncMapper  extends BaseMapper<AfSync> {
}
