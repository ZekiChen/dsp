package com.tecdo.domain.biz.dto;

import com.tecdo.entity.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 将 ad-group-campaign 数据打平，平铺到 AdDTO 中
 *
 * Created by Zeki on 2022/12/29
 **/
@Setter
@Getter
@Builder
public class AdDTO implements Serializable {

    /**
     * 广告信息
     */
    private Ad ad;

    /**
     * AD关联的物料集，比如native广告就需要 logo 和 image
     */
    private Map<Integer, Creative> creativeMap;

    /**
     * 广告所属的组信息
     */
    private AdGroup adGroup;

    /**
     * group关联的定向条件集
     */
    private List<TargetCondition> conditions;

    /**
     * 广告所属的 campaign 信息
     */
    private Campaign campaign;

    /**
     * campaign 关联的广告主的 campaign
     */
    private CampaignRtaInfo campaignRtaInfo;
}
