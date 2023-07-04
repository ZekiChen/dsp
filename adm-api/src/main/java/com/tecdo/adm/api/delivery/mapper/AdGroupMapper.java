package com.tecdo.adm.api.delivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tecdo.adm.api.delivery.entity.AdGroup;
import com.tecdo.starter.mp.entity.StatusEntity;
import com.tecdo.starter.mp.vo.BaseVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 广告组信息 Mapper接口
 *
 * Created by Zeki on 2022/12/26
 **/
public interface AdGroupMapper extends BaseMapper<AdGroup> {

    List<BaseVO> listIdAndName();

    IPage<AdGroup> customPage(IPage<AdGroup> page, @Param("adGroup") AdGroup adGroup,
                              @Param("campaignIds") List<Integer> campaignIds,
                              @Param("campaignName") String campaignName,
                              @Param("adIds") List<Integer> adIds,
                              @Param("adName") String adName,
                              @Param("affiliateIds") List<String> affiliateIds,
                              @Param("countries") List<String> countries);

    List<Integer> listIdByLikeCampaignName(@Param("campaignName") String campaignName);
    List<Integer> listIdByLikeAdGroupName(@Param("name") String name);

    List<Integer> listIdByAdvIds(@Param("advIds") List<Integer> advIds);

    List<Integer> listIdByCampaignIds(@Param("campaignIds") List<Integer> campaignIds);

    List<StatusEntity> listStatus(@Param("ids") List<Integer> ids);

    List<Integer> listIdByCountryAndCIds(@Param("country") String country, @Param("campaignIds") List<Integer> campaignIds);
}