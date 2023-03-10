package com.tecdo.adm.delivery.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tecdo.adm.api.delivery.entity.Ad;

import java.util.List;

/**
 * Created by Zeki on 2023/3/6
 */
public interface IAdService extends IService<Ad> {

    void deleteByAdGroupIds(List<Integer> adGroupIds);
}
