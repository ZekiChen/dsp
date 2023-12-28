package com.tecdo.job.foreign.feishu;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.ejlchina.data.Array;
import com.ejlchina.data.Mapper;
import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.OkHttps;
import com.tecdo.adm.api.delivery.dto.SpentDTO;
import com.tecdo.adm.api.doris.dto.AffWeekReport;
import com.tecdo.job.domain.vo.affReport.InsertDimensionVO;
import com.tecdo.job.domain.vo.camScanner.FeishuValRange;
import com.tecdo.job.domain.vo.camScanner.FeishuSetUnitStyle;
import com.tecdo.job.domain.vo.camScanner.UnitStyle;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
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
    private final String insertDimensionUrl = "https://open.feishu.cn/open-apis/sheets/v2/spreadsheets/?/insert_dimension_range";
    private final String valBatchUpdateUrl = "https://open.feishu.cn/open-apis/sheets/v2/spreadsheets/?/values_batch_update";
    private final String getMetaInfoUrl = "https://open.feishu.cn/open-apis/sheets/v2/spreadsheets/?/metainfo";

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

    public void postWeekData(String sheetId, String sheetToken, List<AffWeekReport> data, String tenantToken) {
        if (CollUtil.isEmpty(data)) return;
        // 思路：插入一空行，继承前一行的格式，编辑前7天（7行）的数据
        // 插入空行
        if (!insertRow(tenantToken, sheetId, sheetToken)) {
            // 插入失败则退出
            XxlJobHelper.log("空行插入失败, sheetId: " + sheetId +
                    " sheetToekn: " + sheetToken + "\n");
            return;
        }
        XxlJobHelper.log("空行插入成功!");

        // 构造前7行数据
        List<FeishuValRange> valRanges = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            String range = sheetId + "!" + "A" + (i + 2) + ":" + "Y" + (i + 2);
            List<List<Object>> value = buildRecurList(data.get(i));
            FeishuValRange valRange = new FeishuValRange(range, value);
            valRanges.add(valRange);
        }

        // 修改前7行数据
        if (!valueBatchUpdate(tenantToken, sheetToken, valRanges)) {
            // 修改失败
            XxlJobHelper.log("数据填充失败, sheetId: " + sheetId +
                    " sheetToekn: " + sheetToken + "\n");
            return;
        }
        XxlJobHelper.log("数据填充成功!");

    }

    /**
     * 调用飞书多范围更新接口
     * @param tenantToken tenantToken
     * @param sheetToken sheetToken
     * @param valRanges valRanges
     * @return 成功/失败
     */
    public boolean valueBatchUpdate(String tenantToken, String sheetToken, List<FeishuValRange> valRanges) {
        String valBatchUpdateUrl = this.valBatchUpdateUrl.replace("?", sheetToken);
        Map<String, Object> paramMap = MapUtil.newHashMap();
        paramMap.put("valueRanges", valRanges);
        HttpResult result = OkHttps.sync(valBatchUpdateUrl)
                .bodyType(OkHttps.JSON)
                .addHeader("Authorization", "Bearer " + tenantToken)
                .addBodyPara(paramMap)
                .post();
        return result.isSuccessful();
    }

    public boolean insertRow(String tenantToken, String sheetId, String sheetToken) {
        String insertDimensionUrl = this.insertDimensionUrl.replace("?", sheetToken);
        InsertDimensionVO dimensionVO = new InsertDimensionVO();
        dimensionVO.setSheetId(sheetId);
        dimensionVO.setMajorDimension("ROWS");
        dimensionVO.setStartIndex(1);
        dimensionVO.setEndIndex(2);

        Map<String, Object> paramMap = MapUtil.newHashMap();
        paramMap.put("inheritStyle", "AFTER");
        paramMap.put("dimension", dimensionVO);

        HttpResult result = OkHttps.sync(insertDimensionUrl)
                .bodyType(OkHttps.JSON)
                .addHeader("Authorization", "Bearer " + tenantToken)
                .addBodyPara(paramMap)
                .post();
        return result.isSuccessful();
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

        FeishuValRange request = new FeishuValRange(range, buildSpentDTO(daysFrom1899(today), impCost));
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
     * 构造双层嵌套list = [subList], subList = [prop1, prop2, ..., propN]
     * @param yourObj value
     * @return 双层了list
     */
    public List<List<Object>> buildRecurList(Object yourObj) {
        List<List<Object>> list = new ArrayList<>();
        List<Object> subList = new ArrayList<>();

        // 获取类的所有字段
        Field[] fields = yourObj.getClass().getDeclaredFields();

        // 通过反射获取字段值并添加到List中
        for (Field field : fields) {
            try {
                // 设置字段可访问，即使是私有字段
                field.setAccessible(true);

                // 获取字段值并添加到List中
                Object value = field.get(yourObj);
                subList.add(value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        list.add(subList);
        return list;
    }

    /**
     * 获取sheetPrependUrl中二维数组请求参数
     * @param date 日期字符串
     * @param spentDTO 曝光量+花费
     * @return List<List<Object>>对象
     */
    public List<List<Object>> buildSpentDTO(Long date, SpentDTO spentDTO) {
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

    public Mapper getMetaInfo(String sheetToken, String tenantToken) {
        String getMetaInfoUrl = this.getMetaInfoUrl.replace("?", sheetToken);
        HttpResult result = OkHttps.sync(getMetaInfoUrl)
                .bodyType(OkHttps.JSON)
                .addHeader("Authorization", "Bearer " + tenantToken)
                .get();
        return result.getBody().toMapper();
    }
}
