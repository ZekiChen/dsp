package com.tecdo.job.handler.adv;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lazada.lazop.api.LazopClient;
import com.lazada.lazop.api.LazopRequest;
import com.lazada.lazop.api.LazopResponse;
import com.lazada.lazop.util.ApiException;
import com.tecdo.adm.api.delivery.dto.ReportEventDTO;
import com.tecdo.adm.api.delivery.entity.Adv;
import com.tecdo.adm.api.delivery.entity.RtaInfo;
import com.tecdo.adm.api.delivery.enums.AdvTypeEnum;
import com.tecdo.adm.api.delivery.mapper.AdvMapper;
import com.tecdo.adm.api.delivery.mapper.CampaignMapper;
import com.tecdo.adm.api.delivery.mapper.RtaInfoMapper;
import com.tecdo.adm.api.doris.mapper.ReportMapper;
import com.tecdo.common.util.WeChatRobotUtils;
import com.tecdo.job.domain.entity.ReportAdvGap;
import com.tecdo.job.domain.vo.lazada.LazadaReportVO;
import com.tecdo.job.domain.vo.lazada.LazadaResponse;
import com.tecdo.job.service.mvc.IReportAdvGapService;
import com.tecdo.job.util.StringConfigUtil;
import com.tecdo.starter.mp.entity.IdEntity;
import com.tecdo.starter.mp.enums.BaseStatusEnum;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.tecdo.job.util.MonitorGroupHelper.MONITOR_GROUP;
import static com.tecdo.job.util.MonitorGroupHelper.logError;

/**
 * Created by Zeki on 2023/5/29
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LazadaJob {

    @Value("${foreign.lazada.report.url}")
    private String lazadaReportUrl;
    @Value("${foreign.lazada.report.api}")
    private String lazadaReportApi;
    @Value("${pac.dsp.adv.gap}")
    private Double gap;

    private final ReportMapper reportMapper;
    private final IReportAdvGapService reportAdvGapService;
    private final RtaInfoMapper rtaInfoMapper;
    private final CampaignMapper campaignMapper;
    private final AdvMapper advMapper;

    @XxlJob("lazadaGap")
    public void lazadaGap() {
        XxlJobHelper.log("重跑过去三天数据，落库lazada和报表gap，超过阈值则告警");
        String yesterday = DateUtil.yesterday().toDateStr();
        String startDay = DateUtil.offsetDay(DateUtil.yesterday(), -2).toDateStr();

        List<RtaInfo> rtaInfos = rtaInfoMapper.selectList(Wrappers.query());
        LambdaQueryWrapper<Adv> wrapper = Wrappers.<Adv>lambdaQuery()
                .eq(Adv::getType, AdvTypeEnum.LAZADA_RTA.getType())
                .eq(Adv::getStatus, BaseStatusEnum.ACTIVE.getType());
        List<Integer> advIds = advMapper.selectList(wrapper).stream().map(IdEntity::getId).collect(Collectors.toList());
        if (CollUtil.isEmpty(advIds)) {
            XxlJobHelper.log("load db and then: advIds is empty");
            return;
        }
        List<Integer> campaignIds = campaignMapper.listIdByAdvIds(advIds);

        for (RtaInfo rtaInfo : rtaInfos) {
            LazopClient client = new LazopClient(lazadaReportUrl, rtaInfo.getAppKey(), rtaInfo.getAppSecret());
            LazopRequest request = new LazopRequest();
            request.setApiName(lazadaReportApi);
            request.addApiParameter("member_id", rtaInfo.getAdvMemId().toString());
            request.addApiParameter("report_type", "DATA_STATISTICS");
            request.addApiParameter("page", "1");
            request.addApiParameter("page_size", "10");
            request.addApiParameter("start_date", startDay);
            request.addApiParameter("end_date", yesterday);
            request.addApiParameter("campaign_type", "Retargeting");
            request.addApiParameter("sort_column", "date");
            request.addApiParameter("sort_type", "desc");
            request.addApiParameter("group_by", "date");
            try {
                LazopResponse response = client.execute(request);
                if (response == null || StrUtil.isEmpty(response.getBody())) {
                    XxlJobHelper.log("call lazada report api error, response is empty");
                    continue;
                }
                if (!"0".equals(response.getCode())) {
                    XxlJobHelper.log("call lazada report api fail, code: " + response.getCode());
                    continue;
                }
                LazadaResponse<LazadaReportVO> resp = JSON.parseObject(response.getBody(), new TypeReference<LazadaResponse<LazadaReportVO>>() {});
                if (resp.getData() == null || CollUtil.isEmpty(resp.getData().getData())) {
                    XxlJobHelper.log("LazadaPage is null or data is empty");
                    continue;
                }
                // 一个国家的近三天数据
                List<LazadaReportVO> reportVOs = resp.getData().getData();
                String country = StringConfigUtil.getCountryCode3(reportVOs.get(0).getCountry());
                List<ReportAdvGap> entities = new ArrayList<>();
                String msg = "报表与广告主gap差异监控\n"
                        + "广告主：Lazada\n"
                        + "国家：" + country + "\n";
                boolean send = false;
                for (LazadaReportVO reportVO : reportVOs) {
                    String date = formatStandard(reportVO.getDate());
                    Long advEvent1 = reportVO.getEvent1();
                    Long advEvent2 = reportVO.getEvent2();
                    Long advEvent3 = reportVO.getEvent3();
                    if (advEvent1 == null || advEvent2 == null || advEvent3 == null) {
                        XxlJobHelper.log("event for lazada exist null, date: " + date + ", country: " + country);
                        return;
                    }
                    ReportEventDTO eventDTO = reportMapper.getRepostEventForLazada(date, country, campaignIds);
                    if (eventDTO == null) {
                        XxlJobHelper.log("get report eventDTO for lazada is null, date: " + date + ", country: " + country);
                        continue;
                    }
                    long dspEvent1 = eventDTO.getEvent1() != null ? eventDTO.getEvent1() : 0L;
                    long dspEvent2 = eventDTO.getEvent2() != null ? eventDTO.getEvent2() : 0L;
                    long dspEvent3 = eventDTO.getEvent3() != null ? eventDTO.getEvent3() : 0L;
                    if (dspEvent1 == 0L || dspEvent2 == 0L || dspEvent3 == 0L) {
                        XxlJobHelper.log("get report event for lazada has 0, date: " + date + ", country: " + country);
                        continue;
                    }
                    double event1Gap = Math.abs((double) (dspEvent1 - advEvent1) / dspEvent1) * 100;
                    double event2Gap = Math.abs((double) (dspEvent2 - advEvent2) / dspEvent2) * 100;
                    double event3Gap = Math.abs((double) (dspEvent3 - advEvent3) / dspEvent3) * 100;
                    // 群消息通知时，保留2位小数
                    String event1GapStr = NumberUtil.round(event1Gap, 2).toString();
                    String event2GapStr = NumberUtil.round(event2Gap, 2).toString();
                    String event3GapStr = NumberUtil.round(event3Gap, 2).toString();
                    if (event1Gap > gap || event2Gap > gap || event3Gap > gap) {
                        send = true;
                        msg = msg
                                + "日期：" + date + "\n"
                                + "广告主event1：" + advEvent1 + "\n"
                                + "报表event1：" + dspEvent1 + "\n"
                                + "event1 gap：" + event1GapStr + "%\n"
                                + "广告主event2：" + advEvent2 + "\n"
                                + "报表event2：" + dspEvent2 + "\n"
                                + "event2 gap：" + event2GapStr + "%\n"
                                + "广告主event3：" + advEvent3 + "\n"
                                + "报表event3：" + dspEvent3 + "\n"
                                + "event3 gap：" + event3GapStr + "%\n\n";
                    }
                    ReportAdvGap entity = buildDspAdvGap(1, date, country,
                            advEvent1, dspEvent1, event1GapStr,
                            advEvent2, dspEvent2, event2GapStr,
                            advEvent3, dspEvent3, event3GapStr);
                    entities.add(entity);
                }
                try {
                    if (send) {
                        msg = msg.substring(0, msg.length() - 2);
                        WeChatRobotUtils.sendTextMsg(MONITOR_GROUP, msg);
                    }
                } catch (Exception e) {
                    logError("lazadaJob: send text msg error: " + e.getMessage(), true);
                    return;
                }
                updateLastPeriod(entities);
            } catch (ApiException e) {
                logError("call lazada report api error: " + e.getMessage());
                return;
            }
        }
    }

    private void updateLastPeriod(List<ReportAdvGap> entities) {
        ReportAdvGap entity = entities.get(0);
        List<String> dates = entities.stream().map(ReportAdvGap::getCreateDate).collect(Collectors.toList());
        LambdaUpdateWrapper<ReportAdvGap> wrapper = Wrappers.<ReportAdvGap>lambdaUpdate()
                .eq(ReportAdvGap::getAdvId, entity.getAdvId())
                .in(ReportAdvGap::getCreateDate, dates)
                .eq(ReportAdvGap::getCountry, entity.getCountry());
        reportAdvGapService.remove(wrapper);
        reportAdvGapService.saveBatch(entities);
    }

    private static ReportAdvGap buildDspAdvGap(Integer advId, String createDate, String country,
                                               Long advEvent1, Long dspEvent1, String event1GapStr,
                                               Long advEvent2, Long dspEvent2, String event2GapStr,
                                               Long advEvent3, Long dspEvent3, String event3GapStr) {
        ReportAdvGap entity = new ReportAdvGap();
        entity.setCreateDate(createDate);
        entity.setAdvId(advId);
        entity.setCountry(country);
        entity.setAdvEvent1(advEvent1);
        entity.setDspEvent1(dspEvent1);
        entity.setEvent1Gap(Double.valueOf(event1GapStr));
        entity.setAdvEvent2(advEvent2);
        entity.setDspEvent2(dspEvent2);
        entity.setEvent2Gap(Double.valueOf(event2GapStr));
        entity.setAdvEvent3(advEvent3);
        entity.setDspEvent3(dspEvent3);
        entity.setEvent3Gap(Double.valueOf(event3GapStr));
        return entity;
    }

    private String formatStandard(String input) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd");
            Date inputDate = inputFormat.parse(input);
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
            return outputFormat.format(inputDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
