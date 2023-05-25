package com.tecdo.adm.foreign.ae.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.tecdo.adm.api.delivery.dto.AdGroupDTO;
import com.tecdo.adm.api.delivery.dto.CampaignDTO;
import com.tecdo.adm.api.delivery.entity.AdGroup;
import com.tecdo.adm.api.delivery.entity.Campaign;
import com.tecdo.adm.api.delivery.entity.CampaignRtaInfo;
import com.tecdo.adm.api.delivery.enums.AdvEnum;
import com.tecdo.adm.api.doris.entity.Report;
import com.tecdo.adm.api.doris.mapper.ReportMapper;
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
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Zeki on 2023/4/3
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AeReportServiceImpl implements IAeReportService {

    private final ReportMapper reportMapper;

    @Override
    public AeDataVO<AeReportVO> listAdvCampaignDailyReport(AeDailyCostVO vo) {
        AeDataVO<AeReportVO> aeDataVO = new AeDataVO<>();
        String advCampaignIds = String.join(StrUtil.COMMA, vo.getAdvCampaignIds());
        List<CampaignRtaInfo> campaignRtaInfos = CampaignCache.listCampaignRta(vo.getChannel(), advCampaignIds);
        if (CollUtil.isEmpty(campaignRtaInfos)) {
            return aeDataVO;
        }
        Set<Integer> campaignIds = campaignRtaInfos.stream()
                .map(CampaignRtaInfo::getCampaignId).collect(Collectors.toSet());
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
            campaignIds = campaignDTOs.stream().map(IdEntity::getId).collect(Collectors.toSet());

            List<String> dateHours = getUsWestHour(vo.getBizDate());
            List<Report> adsDis = reportMapper.getAeDailyReportInUsWest(dateHours, campaignIds);
            Map<Integer, Report> adsDiMap = adsDis.stream().collect(Collectors.toMap(Report::getCampaignId, Function.identity()));

            List<AeReportVO> aeReports = new ArrayList<>();
            for (CampaignDTO dto : campaignDTOs) {
                Report adsDi = adsDiMap.getOrDefault(dto.getId(), new Report());
                Double cost = adsDi.getImpSuccessPriceTotal();
                Long imps = adsDi.getImpCount();
                Long clicks = adsDi.getClickCount();

                AeReportVO aeReport = AeReportVO.builder()
                        .campaignId(dto.getCampaignRtaInfo().getAdvCampaignId())
                        .cost(cost)
                        .impressions(imps)
                        .clicks(clicks)
                        .cpm(imps != null && imps != 0L ? (cost / imps * 1000) : null)
                        .cpc(clicks != null && clicks != 0L ? (cost / clicks) : null)
                        .ctr(clicks != null && imps != null && imps != 0L ? (clicks.doubleValue() / imps.doubleValue()) : null)
                        .build();
                aeReports.add(aeReport);
            }
            aeDataVO.setDataList(aeReports);
        }
        return aeDataVO;
    }

    @NonNull
    private static List<String> getUsWestHour(Date bizDate) {
        String curDay = DateUtil.format(bizDate, "yyyy-MM-dd");
        String curDayTomorrow = DateUtil.offsetDay(bizDate, 1).toDateStr();
        int curMonth = DateUtil.month(new Date()) + 1;
        List<String> dateHours = new ArrayList<>();
        // 4-10月夏令时美国西部时间为UTC-8，冬令时为UTC-7
        int offset = (curMonth >= 4 && curMonth <= 10) ? 8 : 7;
        for (int i = offset; i < offset + 24; i++) {
            int hour = i % 24;
            String date = (hour < offset) ? curDayTomorrow : curDay;
            String hourStr = String.format("%02d", hour);
            dateHours.add(date + "_" + hourStr);
        }
        return dateHours;
    }
}
