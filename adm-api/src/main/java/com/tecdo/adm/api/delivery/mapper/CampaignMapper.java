package com.tecdo.adm.api.delivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tecdo.adm.api.delivery.dto.SimpleCampaignDTO;
import com.tecdo.adm.api.delivery.entity.Campaign;
import com.tecdo.starter.mp.vo.BaseVO;

import java.util.List;

/**
 * 广告campaign Mapper接口
 *
 * Created by Zeki on 2022/12/26
 **/
public interface CampaignMapper extends BaseMapper<Campaign> {

    List<BaseVO> listIdAndName();

    List<SimpleCampaignDTO> listCampaignWithGroupIdName();
}