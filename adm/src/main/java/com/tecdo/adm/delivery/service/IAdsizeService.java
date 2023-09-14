package com.tecdo.adm.delivery.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tecdo.adm.api.delivery.entity.Adsize;
import com.tecdo.adm.api.delivery.vo.AdsizeVO;

import java.util.List;

/**
 * Created by Elwin on 2023/9/13
 */
public interface IAdsizeService extends IService<Adsize> {
    List<AdsizeVO> standardSpecs();
}
