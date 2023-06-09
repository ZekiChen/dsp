package com.tecdo.adm.audience.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tecdo.adm.api.audience.entity.AfContainer;
import com.tecdo.adm.api.audience.vo.SimpleAfContainerVO;

import java.util.List;

/**
 * Created by Zeki on 2023/4/5
 */
public interface IAfContainerService extends IService<AfContainer> {

    List<SimpleAfContainerVO> listSimple();
}
