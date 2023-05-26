package com.tecdo.mapper.doris;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tecdo.adm.api.delivery.entity.CampaignRtaInfo;
import com.tecdo.adm.api.delivery.entity.RtaInfo;
import com.tecdo.adm.api.delivery.mapper.CampaignRtaInfoMapper;
import com.tecdo.adm.api.delivery.mapper.RtaInfoMapper;
import com.tecdo.adm.api.doris.entity.AdGroupCost;
import com.tecdo.adm.api.doris.mapper.AdGroupCostMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * 测试数据中台提供的 Doris 连通性
 *
 * Created by Zeki on 2023/2/21
 */
@SpringBootTest
public class DorisMapperTest {

    // Doris
    @Autowired
    private AdGroupCostMapper costMapper;
    // MySQL
    @Autowired
    private RtaInfoMapper rtaInfoMapper;
    @Autowired
    private CampaignRtaInfoMapper campaignRtaInfoMapper;

    /**
     * 测试 Mapper 是否正确访问指定的 Doris
     */
    @Test
    public void test_AdGroupCostMapper_availability() {
        List<AdGroupCost> costs = costMapper.selectList(Wrappers.<AdGroupCost>query().last("limit 1"));
        System.out.println(costs);
    }

    /**
     * 测试 Mapper 是否正确访问默认的 MySQL
     */
    @Test
    public void test_RtaInfoMapper_availability() {
        List<RtaInfo> rtaInfos = rtaInfoMapper.selectList(Wrappers.<RtaInfo>query().last("limit 1"));
        System.out.println(rtaInfos);
    }

    @Test
    public void test_CampaignRtaInfoMapper_availability() {
        QueryWrapper<CampaignRtaInfo> wrapper = Wrappers.<CampaignRtaInfo>query().last("limit 1");
        List<CampaignRtaInfo> campaignRtaInfos = campaignRtaInfoMapper.selectList(wrapper);
        System.out.println(campaignRtaInfos);
    }
}
