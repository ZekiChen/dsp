package com.tecdo.adm.delivery.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.delivery.service.IAdService;
import com.tecdo.adm.api.delivery.entity.Ad;
import com.tecdo.adm.api.delivery.mapper.AdMapper;
import org.springframework.stereotype.Service;

/**
 * Created by Zeki on 2023/3/6
 */
@Service
public class AdServiceImpl extends ServiceImpl<AdMapper, Ad> implements IAdService {
}
