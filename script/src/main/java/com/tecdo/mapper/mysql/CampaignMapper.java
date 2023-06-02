package com.tecdo.mapper.mysql;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tecdo.entity.CampaignRtaDTO;

import org.apache.ibatis.annotations.Select;

import java.util.Set;


public interface CampaignMapper extends BaseMapper<CampaignRtaDTO> {

  @Select(
    "SELECT DISTINCT cr.adv_campaign_id advCampaignId,cr.channel,t.`value` country FROM campaign c " +
    "RIGHT JOIN adv a ON c.adv_id = a.id RIGHT JOIN campaign_rta_info cr ON c.id = cr.campaign_id RIGHT JOIN ad_group g ON c.id = g.campaign_id RIGHT JOIN target_condition t ON g.id = t.ad_group_id " +
    "WHERE a.type = 3 AND a.STATUS = 1 AND c.STATUS = 1 AND g.STATUS = 1 AND t.attribute = 'device_country'")
  Set<CampaignRtaDTO> listAdvCampaign();
}
