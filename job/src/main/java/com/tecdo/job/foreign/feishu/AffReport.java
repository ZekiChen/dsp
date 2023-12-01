package com.tecdo.job.foreign.feishu;

import cn.hutool.core.map.MapUtil;
import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.OkHttps;
import com.tecdo.adm.api.delivery.dto.SpentDTO;
import com.tecdo.job.domain.vo.camScanner.FeishuPrependReport;
import com.tecdo.job.domain.vo.camScanner.FeishuSetUnitStyle;
import com.tecdo.job.domain.vo.camScanner.UnitStyle;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Elwin on 2023/11/29
 */
@Component
@RequiredArgsConstructor
public class AffReport {
    @Value("${feishu.aff.app-id}")
    private String appId;
    @Value("${feishu.aff.app-secret}")
    private String appSecret;
    @Value("${feishu.aff.get-token.url}")
    private String tenantTokenUrl;

    @Value("${feishu.aff.sheet-formatter.url}")
    private String sheetFormatterUrl;
    @Value("${feishu.aff.sheet-prepend.url}")
    private String sheetPrependUrl;

    private final String dateFormat = "yyyy/MM/dd";

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
     * 向飞书表格插入数据
     *
     * @param impCost 花费
     */
    public void postData(LocalDate today, String sheetId, String sheetToken,
                         SpentDTO impCost, String costRatio, String impRatio, String range) {
        // 处理占位符
        range = range.replace("?", sheetId);
        String sheetPrependUrl = this.sheetPrependUrl.replace("?", sheetToken);

        String tenantToken = getAccessToken();

        DecimalFormat df = new DecimalFormat("#.0000"); // 保留4位小数
        double finalCost = Double.parseDouble(df.format(impCost.getCost() * Double.parseDouble(costRatio)));
        Long finalImp = Math.round(impCost.getImp() * Double.parseDouble(impRatio));

        impCost.setCost(finalCost);
        impCost.setImp(finalImp);

        FeishuPrependReport request = new FeishuPrependReport(range, buildValues(daysFrom1899(today), impCost));
        Map<String, Object> paramMap = MapUtil.newHashMap();
        paramMap.put("valueRange", request);
        HttpResult result = OkHttps.sync(sheetPrependUrl)
                .bodyType(OkHttps.JSON)
                .addHeader("Authorization", "Bearer " + tenantToken)
                .addBodyPara(paramMap)
                .post();
        String msg = result.isSuccessful() ? "数据写入成功" : "数据写入失败";
        XxlJobHelper.log(msg);
    }

    /**
     * 由于飞书表格的特殊要求，为了填入日期类型的数据，需要填当前距离1899年12月30日的天数
     * @return 当前距离1899年12月30日的天数
     */
    public Long daysFrom1899(LocalDate today) {
        LocalDate currentDate = today.minusDays(1);
        LocalDate date1899 = LocalDate.of(1899, 12, 30);
        return ChronoUnit.DAYS.between(date1899, currentDate);
    }

    /**
     * 获取sheetPrependUrl中二维数组请求参数
     * @param date 日期字符串
     * @param spentDTO 曝光量+花费
     * @return List<List<Object>>对象
     */
    public List<List<Object>> buildValues(Long date, SpentDTO spentDTO) {
        List<List<Object>> list = new ArrayList<>();
        List<Object> subList = new ArrayList<>();

        subList.add(date);
        subList.add(spentDTO.getImp());
        subList.add(spentDTO.getCost());

        list.add(subList);

        return list;
    }

    /**
     * 把日期对应的单元格设置为dateFormat日期类型
     * @return true / false
     */
    public boolean unitFormatter(String sheetId, String sheetToken, String unitRange) {
        // 处理占位符
        unitRange = unitRange.replace("?", sheetId);
        String sheetFormatterUrl = this.sheetFormatterUrl.replace("?", sheetToken);

        String tenantToken = getAccessToken();

        FeishuSetUnitStyle request = new FeishuSetUnitStyle(unitRange, new UnitStyle(dateFormat));
        Map<String, Object> paramMap = MapUtil.newHashMap();
        paramMap.put("appendStyle", request);
        HttpResult result = OkHttps.sync(sheetFormatterUrl)
                .bodyType(OkHttps.JSON)
                .addHeader("Authorization", "Bearer " + tenantToken)
                .addBodyPara(paramMap)
                .put();
        System.out.println(result.getBody());
        String msg = result.isSuccessful() ? "单元格格式修改成功" : "单元格格式修改失败";
        XxlJobHelper.log(msg);
        return result.isSuccessful();
    }

    /**
     * 获取前一天时间
     * @return 前一天日期字符串
     */
    public String dateFormat(LocalDate today) {
        LocalDate currentDate = today.minusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return currentDate.format(formatter);
    }
}
