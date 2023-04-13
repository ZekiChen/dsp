package com.tecdo.adm.foreign.ae.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tecdo.adm.api.delivery.dto.AdGroupDTO;
import com.tecdo.adm.api.delivery.dto.CampaignDTO;
import com.tecdo.adm.api.delivery.entity.AdGroup;
import com.tecdo.adm.api.delivery.entity.Campaign;
import com.tecdo.adm.api.delivery.entity.CampaignRtaInfo;
import com.tecdo.adm.api.delivery.enums.AdvEnum;
import com.tecdo.adm.api.doris.entity.AdGroupClick;
import com.tecdo.adm.api.doris.entity.AdGroupCost;
import com.tecdo.adm.api.doris.entity.AdGroupImpCount;
import com.tecdo.adm.api.doris.mapper.AdGroupClickMapper;
import com.tecdo.adm.api.doris.mapper.AdGroupCostMapper;
import com.tecdo.adm.api.doris.mapper.AdGroupImpCountMapper;
import com.tecdo.adm.api.foreign.ae.vo.request.AeDailyCostVO;
import com.tecdo.adm.api.foreign.ae.vo.response.AeDataVO;
import com.tecdo.adm.api.foreign.ae.vo.response.AeReportVO;
import com.tecdo.adm.common.cache.AdGroupCache;
import com.tecdo.adm.common.cache.AdvCache;
import com.tecdo.adm.common.cache.CampaignCache;
import com.tecdo.adm.foreign.ae.service.IAeReportService;
import com.tecdo.starter.mp.entity.IdEntity;
import com.tecdo.starter.tool.util.BeanUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Zeki on 2023/4/3
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AeReportServiceImpl implements IAeReportService {

    private final AdGroupCostMapper costMapper;
    private final AdGroupImpCountMapper impCountMapper;
    private final AdGroupClickMapper clickMapper;

    @Override
    public AeDataVO<AeReportVO> listAdvCampaignDailyReport(AeDailyCostVO vo) {
        AeDataVO<AeReportVO> aeDataVO = new AeDataVO<>();
        String advCampaignIds = String.join(StrUtil.COMMA, vo.getAdvCampaignIds());
        List<CampaignRtaInfo> campaignRtaInfos = CampaignCache.listCampaignRta(vo.getChannel(), advCampaignIds);
        if (CollUtil.isEmpty(campaignRtaInfos)) {
            return aeDataVO;
        }
        List<Integer> campaignIds = campaignRtaInfos.stream()
                .map(CampaignRtaInfo::getCampaignId).collect(Collectors.toList());
        String campaignIdsStr = campaignIds.stream().map(Object::toString).collect(Collectors.joining(StrUtil.COMMA));
        List<Campaign> campaigns = CampaignCache.listCampaign(campaignIdsStr);

        List<CampaignDTO> campaignDTOs = new ArrayList<>();
        for (Campaign campaign : campaigns) {
            String advName = AdvCache.getAdv(campaign.getAdvId()).getName();
            if (AdvEnum.AE.getDesc().equalsIgnoreCase(advName)) {
                CampaignDTO campaignDTO = Objects.requireNonNull(BeanUtil.copy(campaign, CampaignDTO.class));
                campaignDTO.setAdvName(advName);
                List<AdGroup> adGroups = AdGroupCache.listAdGroup(campaign.getId());
                campaignDTO.setAdGroupDTOs(BeanUtil.copy(adGroups, AdGroupDTO.class));
                campaignDTO.setCampaignRtaInfo(CampaignCache.getCampaignRta(campaign.getId()));
                campaignDTOs.add(campaignDTO);
            }
        }

        if (CollUtil.isNotEmpty(campaignDTOs)) {
            campaignIds = campaignDTOs.stream().map(IdEntity::getId).collect(Collectors.toList());

            Map<String, Double> campaignCostMap = campaignCost(vo.getBizDate(), campaignIds);
            Map<String, Long> campaignImpMap = campaignImp(vo.getBizDate(), campaignIds);
            Map<String, Long> campaignClickMap = campaignClick(vo.getBizDate(), campaignIds);

            List<AeReportVO> aeReports = new ArrayList<>();
            for (CampaignDTO dto : campaignDTOs) {
                int cost = campaignCostMap.getOrDefault(dto.getId().toString(), 0d).intValue();
                int imps = campaignImpMap.getOrDefault(dto.getId().toString(), 0L).intValue();
                int clicks = campaignClickMap.getOrDefault(dto.getId().toString(), 0L).intValue();

                AeReportVO aeReport = AeReportVO.builder()
                        .campaignId(dto.getCampaignRtaInfo().getAdvCampaignId())
                        .cost(cost)
                        .impressions(imps)
                        .clicks(clicks)
                        .cpm(imps != 0 ? (cost / imps * 1000) : null)
                        .cpc(clicks != 0 ? (cost / clicks) : null)
                        .ctr(imps != 0 ? (clicks / imps) : null)
                        .build();
                aeReports.add(aeReport);
            }
            aeDataVO.setDataList(aeReports);
        }
        return aeDataVO;
    }

    private Map<String, Double> campaignCost(Date bizDate, List<Integer> campaignIds) {
        LambdaQueryWrapper<AdGroupCost> costWrapper = Wrappers.<AdGroupCost>lambdaQuery()
                .eq(AdGroupCost::getCreateDate, bizDate)
                .in(AdGroupCost::getCampaignId, campaignIds);
        return costMapper.selectList(costWrapper).stream()
                .collect(Collectors.groupingBy(AdGroupCost::getCampaignId,
                        Collectors.summingDouble(AdGroupCost::getSumSuccessPrice)));
    }

    private Map<String, Long> campaignImp(Date bizDate, List<Integer> campaignIds) {
        LambdaQueryWrapper<AdGroupImpCount> impCountWrapper = Wrappers.<AdGroupImpCount>lambdaQuery()
                .eq(AdGroupImpCount::getCreateDate, bizDate)
                .in(AdGroupImpCount::getCampaignId, campaignIds);
        return impCountMapper.selectList(impCountWrapper).stream()
                .collect(Collectors.groupingBy(AdGroupImpCount::getCampaignId,
                        Collectors.summingLong(AdGroupImpCount::getValue)));
    }

    private Map<String, Long> campaignClick(Date bizDate, List<Integer> campaignIds) {
        LambdaQueryWrapper<AdGroupClick> clickWrapper = Wrappers.<AdGroupClick>lambdaQuery()
                .eq(AdGroupClick::getCreateDate, bizDate)
                .in(AdGroupClick::getCampaignId, campaignIds);
        return clickMapper.selectList(clickWrapper).stream()
                .collect(Collectors.groupingBy(AdGroupClick::getCampaignId,
                        Collectors.summingLong(AdGroupClick::getClickCount)));
    }

    private String convertToUsWestDate(Date date) {
        // 1. 将Date对象转换为LocalDate对象
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        // 2. 将本地日期转换为美西时区日期
        ZonedDateTime zdt = localDate.atStartOfDay(ZoneId.of("America/Los_Angeles"));
        // 3. 格式化日期字符串
        return zdt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
