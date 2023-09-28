package com.tecdo.job.handler.budget;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.sql.StringEscape;
import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.OkHttps;
import com.tecdo.adm.api.delivery.entity.AdGroup;
import com.tecdo.adm.api.delivery.entity.Campaign;
import com.tecdo.adm.api.delivery.mapper.AdGroupMapper;
import com.tecdo.adm.api.delivery.mapper.CampaignMapper;
import com.tecdo.adm.api.doris.entity.AdGroupCost;
import com.tecdo.job.domain.vo.budget.BudgetWarn;
import com.tecdo.job.domain.vo.budget.ContentData;
import com.tecdo.job.domain.vo.budget.MsgContent;
import com.tecdo.job.mapper.ImpCostMapper;
import com.tecdo.job.util.JsonHelper;
import com.tecdo.job.util.TimeZoneUtils;
import com.tecdo.starter.redis.PacRedis;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Elwin on 2023/9/26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BudgetJob {
    @Value("${feishu.aff.app-id}")
    private String appId;
    @Value("${feishu.aff.app-secret}")
    private String appSecret;
    @Value("${feishu.aff.get-token.url}")
    private String tenantTokenUrl;
    @Value("${feishu.chat.warn.budget.url}")
    private String sendMsgUrl;
    @Value("${feishu.chat.warn.budget.receive-id}")
    private String receive_id;
    @Value("${feishu.chat.warn.budget.template-id}")
    private String template_id;

    private static final String WARNED_AD_GROUP_CACHE = "pac:dsp:budget:warned:ad_group";
    private static final String WARNED_CAMPAIGN_CACHE = "pac:dsp:budget:warned:campaign";

    private final ImpCostMapper impCostMapper;
    private final AdGroupMapper adGroupMapper;
    private final CampaignMapper campaignMapper;
    private final PacRedis pacRedis;
    private Map<String, Double> campaignImpCostMap;
    private Map<String, AdGroupCost> groupImpCostMap;
    private String tenantToken = "";
    private String msg_type = "interactive";

    @XxlJob("FeishuBudgetWarning")
    public void BudgetWarning() {
        XxlJobHelper.log("扫描新增超预算Campaign, Ad_Group集合");
        tenantToken = getAccessToken();
        if (StrUtil.isBlank(tenantToken)) {
            XxlJobHelper.handleFail("tenantToken获取失败");
            return;
        }
        List<AdGroupCost> impCosts = impCostMapper.listByGroup();

        // 获得ad group, campaign列表，并过滤掉已经被Redis记录的部分
        List<AdGroup> groupList = getOverBudgetGroups(impCosts).stream()
                .filter(adGroup -> !isWarned(false, adGroup.getId()))
                .collect(Collectors.toList());
        List<Campaign> campaignList = getOverBudgetCampaigns(impCosts).stream()
                .filter(campaign -> !isWarned(true, campaign.getId()))
                .collect(Collectors.toList());

        // 使用Redis记录新增对象
        setRecords(groupList, campaignList);
        // 用通过筛选的groupList, campaignList，构建vo对象
        List<BudgetWarn> budgetWarnList = buildBudgetWarn(groupList, campaignList);
        // 发送警告消息
        sentWarnings(budgetWarnList);
    }

    /**
     * 发送警告消息
     * @param budgetWarnList 警告列表
     */
    public void sentWarnings(List<BudgetWarn> budgetWarnList) {
        // 依次发送budgetWarnList
        for (BudgetWarn warn : budgetWarnList) {
            // 构建请求体request
            MsgContent content = new MsgContent("template", new ContentData(template_id, warn));
            // 序列化为json并去掉首尾单引号
            String escapedContent = JsonHelper.toJSONString(content)
                    .replaceAll("^'+|'+$", "");

            Map<String, Object> request = MapUtil.newHashMap();
            request.put("receive_id", receive_id);
            request.put("msg_type", msg_type);
            request.put("content", escapedContent);
            request.put("uuid", UUID.randomUUID().toString());

            // 发送请求
            HttpResult result = OkHttps.sync(sendMsgUrl.concat("?").concat("receive_id_type=chat_id"))
                    .bodyType(OkHttps.JSON)
                    .addHeader("Authorization", "Bearer " + tenantToken)
                    .addBodyPara(request)
                    .post();

            if (!result.isSuccessful()) {
                XxlJobHelper.handleFail("消息发送失败");
                return;
            }
        }
    }

    /**
     * 将未被记录的数据记录在Redis中
     * @param groupList 未被记录的ad_group
     * @param campaignList 未被记录的campaign
     */
    public void setRecords(List<AdGroup> groupList, List<Campaign> campaignList) {
        for (AdGroup adGroup : groupList) {
            String key = WARNED_AD_GROUP_CACHE.concat(":").concat(adGroup.getId().toString());
            pacRedis.setEx(key, Boolean.TRUE, TimeZoneUtils.getNowToNextDaySeconds());
        }
        for (Campaign campaign : campaignList) {
            String key = WARNED_CAMPAIGN_CACHE.concat(":").concat(campaign.getId().toString());
            pacRedis.setEx(key, Boolean.TRUE,  TimeZoneUtils.getNowToNextDaySeconds());
        }
    }

    /**
     * 构造vo对象 BudgetWarn
     *
     * @param groupList    超预算的ad group列表
     * @param campaignList 超预算的campaign列表
     * @return BudgetWarn对象列表
     */
    public List<BudgetWarn> buildBudgetWarn(List<AdGroup> groupList, List<Campaign> campaignList) {
        List<BudgetWarn> budgetWarnList = new ArrayList<>();
        for (AdGroup adGroup : groupList) {
            budgetWarnList.add(new BudgetWarn(TimeZoneUtils.dateInChina().toString(),
                    adGroup.getCampaignId(), adGroup.getId(),
                    groupImpCostMap.get(adGroup.getId().toString()).getSumSuccessPrice().toString(),
                    adGroup.getDailyBudget().toString(),
                    adGroup.getName(),
                    campaignMapper.selectById(adGroup.getCampaignId()).getName()));
        }
        for (Campaign campaign : campaignList) {
            budgetWarnList.add(new BudgetWarn(TimeZoneUtils.dateInChina().toString(),
                    campaign.getId(),
                    -1,
                    campaignImpCostMap.get(campaign.getId().toString()).toString(),
                    campaign.getDailyBudget().toString(),
                    "none",
                    campaign.getName()));
        }
        return budgetWarnList;
    }

    /**
     * 获取超预算的campaign列表
     *
     * @param impCosts 产生花费的campaign的花费情况
     * @return 超预算的ad_group列表
     */
    public List<Campaign> getOverBudgetCampaigns(List<AdGroupCost> impCosts) {
        // 以campaign为粒度计算花费
        campaignImpCostMap = impCosts.stream()
                .collect(Collectors.groupingBy(AdGroupCost::getCampaignId,
                        Collectors.summingDouble(AdGroupCost::getSumSuccessPrice)));

        // 在campaign中查找产生了花费的campaign数据
        List<Integer> ids = impCosts.stream()
                .map(AdGroupCost::getCampaignId)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        List<Campaign> campaigns = campaignMapper.selectList(Wrappers.<Campaign>lambdaQuery().in(Campaign::getId, ids));

        // 使用filter筛选出超预算的campaign对象列表
        List<Campaign> result = campaigns.stream()
                .filter(campaign -> {
                    double impCost = campaignImpCostMap.getOrDefault(campaign.getId().toString(), -1.0);
                    return impCost / 1000 >= campaign.getDailyBudget();
                })
                .collect(Collectors.toList());

        return result;
    }

    /**
     * 获取超预算的ad_group列表
     *
     * @param impCosts 产生花费的ad_group的花费情况
     * @return 超预算的ad_group列表
     */
    public List<AdGroup> getOverBudgetGroups(List<AdGroupCost> impCosts) {
        groupImpCostMap = impCosts.stream()
                .collect(Collectors.toMap(AdGroupCost::getAdGroupId, Function.identity()));

        // 在ad_group中查找产生了花费的ad_group数据
        List<Integer> ids = impCosts.stream()
                .map(AdGroupCost::getAdGroupId)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        List<AdGroup> adGroups = adGroupMapper.selectList(Wrappers.<AdGroup>lambdaQuery().in(AdGroup::getId, ids));

        // 使用filter筛选出超预算的ad group对象列表
        List<AdGroup> filteredAdGroups = adGroups.stream()
                .filter(adGroup -> {
                    AdGroupCost impCost = groupImpCostMap.getOrDefault(adGroup.getId().toString(), null);
                    return impCost != null && impCost.getSumSuccessPrice() / 1000 >= adGroup.getDailyBudget();
                })
                .collect(Collectors.toList());

        return filteredAdGroups;
    }

    /**
     * 查询目标ad_group/campaign是否被警告过
     *
     * @param isCampaign Campaign id为true，反之为false
     * @param id         id
     * @return 是否被警告过
     */
    public boolean isWarned(boolean isCampaign, int id) {
        String prefix = isCampaign ? WARNED_CAMPAIGN_CACHE : WARNED_AD_GROUP_CACHE;
        return pacRedis.exists(prefix.concat(":").concat(Integer.toString(id)));
    }

    /**
     * token有过期时间，每次使用token前调用此接口刷新token
     *
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
}
