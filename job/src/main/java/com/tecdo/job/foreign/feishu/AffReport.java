package com.tecdo.job.foreign.feishu;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import com.ejlchina.data.Mapper;
import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.OkHttps;
import com.tecdo.adm.api.delivery.dto.SpentDTO;
import com.tecdo.adm.api.doris.dto.AffWeekReport;
import com.tecdo.job.domain.entity.ReportAffGap;
import com.tecdo.job.domain.vo.affReport.InsertDimensionVO;
import com.tecdo.job.domain.vo.camScanner.FeishuValRange;
import com.tecdo.job.domain.vo.camScanner.FeishuSetUnitStyle;
import com.tecdo.job.domain.vo.camScanner.UnitStyle;
import com.tecdo.job.mapper.DspReportMapper;
import com.tecdo.job.mapper.ReportAffGapMapper;
import com.tecdo.job.util.TimeZoneUtils;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tecdo.job.constant.ReportConstant.*;

/**
 * Created by Elwin on 2023/11/29
 */
@Component
@RequiredArgsConstructor
public class AffReport {
    private final ReportAffGapMapper reportAffGapMapper;
    private String appId = FEISHU_APP_ID;
    private String appSecret = FEISHU_APP_SECRET;
    private String tenantTokenUrl = FEISHU_GET_TOKEN_URL;

    private String sheetFormatterUrl = FEISHU_SHEET_FORMATTER_URL;
    private String sheetPrependUrl = FEISHU_SHEET_PREPEND_URL;
    private final String dateFormat = "yyyy/MM/dd";
    private final String insertDimensionUrl = FEISHU_SHEET_INSERT_DIMENSION_URL;
    private final String valBatchUpdateUrl = FEISHU_SHEET_VAL_BATCH_UPDATE_URL;
    private final String getMetaInfoUrl = FEISHU_SHEET_GET_META_INFO_URL;

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
     * 向飞书表格插入数据
     * @param dspSpent dsp视角
     * @Param affSpent 渠道视角，未提供api则传入null
     */
    public void postData(LocalDate today, String sheetId, String sheetToken,
                         SpentDTO dspSpent, SpentDTO affSpent, String costRatio, String impRatio, String range) {
        // 处理占位符
        range = range.replace("?", sheetId);
        String sheetPrependUrl = this.sheetPrependUrl.replace("?", sheetToken);

        // 处理小数位和缩放比例
        DecimalFormat df = new DecimalFormat("#.0000"); // 保留4位小数
        double finalCost = Double.parseDouble(df.format(dspSpent.getCost() * Double.parseDouble(costRatio)));
        Long finalImp = Math.round(dspSpent.getImp() * Double.parseDouble(impRatio));
        dspSpent.setCost(finalCost);
        dspSpent.setImp(finalImp);

        // 发送网络请求
        FeishuValRange request = new FeishuValRange(range, buildSpentDTO(daysFrom1899(today), dspSpent, affSpent));
        Map<String, Object> paramMap = MapUtil.newHashMap();
        paramMap.put("valueRange", request);
        HttpResult result = OkHttps.sync(sheetPrependUrl)
                .bodyType(OkHttps.JSON)
                .addHeader("Authorization", "Bearer " + getAccessToken())
                .addBodyPara(paramMap)
                .post();
        String msg = result.isSuccessful() ? "数据写入成功" : "数据写入失败";
        XxlJobHelper.log(msg);
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
     * 获取文件的meta信息
     * @return meta info 响应体
     */
    public Mapper getMetaInfo(String sheetToken, String tenantToken) {
        String getMetaInfoUrl = this.getMetaInfoUrl.replace("?", sheetToken);
        HttpResult result = OkHttps.sync(getMetaInfoUrl)
                .bodyType(OkHttps.JSON)
                .addHeader("Authorization", "Bearer " + tenantToken)
                .get();
        return result.getBody().toMapper();
    }

    /* ------------------------------ Inner Util ------------------------------ */

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
     * 获取前一天时间
     * @return 前一天日期字符串
     */
    public String dateFormat(LocalDate today) {
        LocalDate currentDate = today.minusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return currentDate.format(formatter);
    }

    /**
     * 获取sheetPrependUrl中二维数组请求参数
     * @param date 日期字符串
     * @param dspSpent dsp视角的spent
     * @param affSpent 渠道视角的revenue
     * @return List<List<Object>>对象
     */
    public List<List<Object>> buildSpentDTO(Long date, SpentDTO dspSpent, SpentDTO affSpent) {
        List<List<Object>> list = new ArrayList<>();
        List<Object> subList = new ArrayList<>();
        Long dspImp = dspSpent.getImp();
        Double dspCost = dspSpent.getCost();

        subList.add(date);
        subList.add(dspImp);
        subList.add(dspCost);

        if (affSpent != null) {
            Long affImp = affSpent.getImp();
            Double affRevenue = affSpent.getCost();
            double impGap = (dspImp - affImp) / (double)affImp;
            double costGap = (dspCost - affRevenue) / affRevenue;

            subList.add(affImp);
            subList.add(NumberUtil.round(affRevenue, 4)); // 同步dspSpent保留4位小数
            subList.add(NumberUtil.formatPercent(impGap, 2));
            subList.add(NumberUtil.formatPercent(costGap, 2));
        }

        list.add(subList);

        return list;
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
     * 由于飞书表格的特殊要求，为了填入日期类型的数据，需要填当前距离1899年12月30日的天数
     * @return 当前距离1899年12月30日的天数
     */
    public Long daysFrom1899(LocalDate today) {
        LocalDate currentDate = today.minusDays(1);
        LocalDate date1899 = LocalDate.of(1899, 12, 30);
        return ChronoUnit.DAYS.between(date1899, currentDate);
    }

    /**
     * 插入空行，目的：继承前一行格式
     * @return 成功/失败
     */
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

    public void insertGapReport(Integer affId, String date, SpentDTO dspSpent, SpentDTO affSpent) {
        // TODO: examine either affSpent or dspSpent should be Denominator
        double impGap = ((dspSpent.getImp() - affSpent.getImp()) / (double)affSpent.getImp()) * 100;
        double costGap = ((dspSpent.getCost() - affSpent.getCost()) / affSpent.getCost()) * 100;
        ReportAffGap entity = new ReportAffGap();
        entity.setCreateDate(date);
        entity.setAffId(affId);
        entity.setAffImp(affSpent.getImp());
        entity.setDspImp(dspSpent.getImp());
        entity.setGapImp(impGap);
        entity.setAffCost(affSpent.getCost());
        entity.setDspCost(dspSpent.getCost());
        entity.setGapCost(costGap);
        reportAffGapMapper.insert(entity);
    }


}
