package com.tecdo.adm.delivery.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tecdo.adm.api.delivery.entity.AffCountryBundleBList;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.starter.mp.vo.BaseVO;

import java.util.List;

/**
 * Created by Zeki on 2023/3/6
 */
public interface IAffiliateService extends IService<Affiliate> {

    List<BaseVO> listIdAndName();

    Boolean updateCountryBundleBLists(List<AffCountryBundleBList> bLists);
}
