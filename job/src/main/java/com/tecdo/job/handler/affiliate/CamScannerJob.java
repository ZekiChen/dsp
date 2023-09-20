package com.tecdo.job.handler.affiliate;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.map.MapUtil;
import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.OkHttps;
import com.tecdo.adm.api.delivery.dto.SpentDTO;
import com.tecdo.job.domain.vo.camScanner.FeishuPrependReport;
import com.tecdo.job.domain.vo.camScanner.FeishuSetUnitStyle;
import com.tecdo.job.domain.vo.camScanner.UnitStyle;
import com.tecdo.job.mapper.DspReportMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Elwin on 2023/9/18
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CamScannerJob {
    @Value("${feishu.aff.app-id}")
    private String appId;
    @Value("${feishu.aff.app-secret}")
    private String appSecret;
    @Value("${feishu.aff.get-token.url}")
    private String tenantTokenUrl;
    @Value("${feishu.aff.cs.sheet-prepend.url}")
    private String sheetPrependUrl;
    @Value("${feishu.aff.cs.sheet-range}")
    private String range;
    @Value("${feishu.aff.cs.sheet-formatter.url}")
    private String sheetFormatterUrl;
    @Value("${feishu.aff.cs.sheet-unit-range}")
    private String unitRange;
    private String dateFormat = "yyyy/MM/dd";
    private String tenantToken = "";

    private final DspReportMapper reportMapper;

    @XxlJob("FeishuAff127Job")
    public void dspReport() {
        XxlJobHelper.log("获取doris库dsp_report表前一天数据，写入飞书文档");
        tenantToken = getAccessToken();
        if (tenantToken.isEmpty()) {
            XxlJobHelper.handleFail("tenantToken获取失败");
            return;
        }
        String msg = postData(reportMapper.getImpCostForCamScanner(dateFormat())) ? "数据写入成功" : "数据写入失败";
        XxlJobHelper.log(msg);
        msg = unitFormatter() ? "单元格格式修改成功" : "单元格格式修改失败";
        XxlJobHelper.log(msg);
    }

    /**
     * token有过期时间，每次使用token前调用此接口刷新token
     * @return 返回应用的tenant token
     */
    public String getAccessToken() {
        Map<String, Object> paramMap = MapUtil.newHashMap();
        paramMap.put("app_id", appId);
        paramMap.put("app_secret", appSecret);
        HttpResult result = OkHttps.sync(tenantTokenUrl).bodyType(OkHttps.JSON).addBodyPara(paramMap).post();
        String tenantToken;
        if (result.isSuccessful()) {
            tenantToken = result.getBody().toMapper().getString("tenant_access_token");
            return tenantToken;
        }
        return "";
    }

    /**
     *
     * @param date 日期字符串
     * @param spentDTO 曝光量+花费
     * @return List<List<Object>>对象
     */
    public List<List<Object>> buildValues(Long date, SpentDTO spentDTO) {
        return new ArrayList<List<Object>>() {{
            add(new ArrayList<Object>() {{
                add(date);
                add(spentDTO.getImp());
                add(spentDTO.getCost());
            }});
        }};
    }

    /**
     * @return 返回前一天日期字符串，精确到日
     */
    public String dateFormat() {
        LocalDate currentDate = LocalDate.now().minusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        return currentDate.format(formatter).replace("/", "-");
    }

    /**
     * 由于飞书表格的特殊要求，为了填入日期类型的数据，需要填当前距离1899年12月30日的天数
     * @return 当前距离1899年12月30日的天数
     */
    public Long daysFrom1899() {
        LocalDate currentDate = LocalDate.now().minusDays(1);
        LocalDate date1899 = LocalDate.of(1899, 12, 30);
        return ChronoUnit.DAYS.between(date1899, currentDate);
    }

    /**
     * 向飞书表格插入数据
     * @param impCost 花费
     * @return true / false
     */
    public boolean postData(SpentDTO impCost) {
        FeishuPrependReport request = new FeishuPrependReport(range, buildValues(daysFrom1899(), impCost));
        Map<String, Object> paramMap = MapUtil.newHashMap();
        paramMap.put("valueRange", request);
        HttpResult result = OkHttps.sync(sheetPrependUrl)
                .bodyType(OkHttps.JSON)
                .addHeader("Authorization", "Bearer " + tenantToken)
                .addBodyPara(paramMap)
                .post();
        return result.isSuccessful();
    }

    /**
     * 把日期对应的单元格设置为dateFormat日期类型
     * @return true / false
     */
    public boolean unitFormatter() {
        FeishuSetUnitStyle request = new FeishuSetUnitStyle(unitRange, new UnitStyle(dateFormat));
        Map<String, Object> paramMap = MapUtil.newHashMap();
        paramMap.put("appendStyle", request);
        HttpResult result = OkHttps.sync(sheetFormatterUrl)
                .bodyType(OkHttps.JSON)
                .addHeader("Authorization", "Bearer " + tenantToken)
                .addBodyPara(paramMap)
                .put();
        System.out.println(result.getBody());
        return result.isSuccessful();
    }
}
