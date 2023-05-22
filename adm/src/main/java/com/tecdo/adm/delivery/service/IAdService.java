package com.tecdo.adm.delivery.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tecdo.adm.api.delivery.entity.Ad;
import com.tecdo.adm.api.delivery.vo.SimpleAdUpdateVO;

import java.util.List;

/**
 * Created by Zeki on 2023/3/6
 */
public interface IAdService extends IService<Ad> {

    List<Ad> listByAdGroupId(Integer adGroupId);

    void deleteByAdGroupIds(List<Integer> adGroupIds);

    boolean copy(Integer sourceAdId, List<Integer> targetAdGroupIds, Integer targetAdStatus);

    boolean editListInfo(SimpleAdUpdateVO vo);
}
