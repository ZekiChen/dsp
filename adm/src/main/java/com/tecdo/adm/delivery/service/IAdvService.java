package com.tecdo.adm.delivery.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tecdo.adm.api.delivery.entity.Adv;
import com.tecdo.starter.mp.vo.BaseVO;

import java.util.List;

/**
 * Created by Zeki on 2023/4/5
 */
public interface IAdvService extends IService<Adv> {

    List<BaseVO> listIdAndName();
}
