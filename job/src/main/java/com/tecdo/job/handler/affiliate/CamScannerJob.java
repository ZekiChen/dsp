package com.tecdo.job.handler.affiliate;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.map.MapUtil;
import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.OkHttps;
import com.tecdo.adm.api.delivery.dto.SpentDTO;
import com.tecdo.job.domain.vo.camScanner.FeishuPrependReport;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    @Value("${feishu.aff.cs.sheet-range}")
    private String range;

    @XxlJob("FeishuAff127Job")
    public void dspReport() {
        XxlJobHelper.log("获取doris库dsp_report表前一天数据，写入飞书文档");
        String tenantToken = getAccessToken();
        if (tenantToken.isEmpty()) {
            XxlJobHelper.handleFail("tenantToken获取失败");
            return;
        }

        List<List<Object>> values = new ArrayList<>();

        FeishuPrependReport data = new FeishuPrependReport();
        data.setValueRange(new FeishuPrependReport.ValueRange(range, values));
    }

    /**
     * token有过期时间，每次使用token前调用此接口刷新token
     * @return
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
     * @param date 时间字符串
     * @param spentDTO 曝光量+花费
     * @return List<List<Object>>对象
     */
    public List<List<Object>> buildValues(String date, SpentDTO spentDTO) {
        return new ArrayList<List<Object>>() {{
            add(new ArrayList<Object>() {{
                add(date);
                add(spentDTO.getImp());
                add(spentDTO.getCost());
            }});
        }};
    }

    /**
     *
     * @return 返回当前日期字符串，精确到日
     */
    public String dateFormat() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        return currentDate.format(formatter);
    }
}
