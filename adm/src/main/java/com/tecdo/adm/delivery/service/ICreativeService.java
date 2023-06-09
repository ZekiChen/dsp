package com.tecdo.adm.delivery.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tecdo.adm.api.delivery.entity.Creative;
import com.tecdo.adm.api.delivery.vo.CreativeSpecVO;

import java.util.List;

/**
 * Created by Zeki on 2023/3/10
 */
public interface ICreativeService extends IService<Creative> {

    List<CreativeSpecVO> listSpecs();

    List<Integer> listIdByLikeName(String name);

    List<Integer> listIdBySize(Integer width, Integer height);
}
