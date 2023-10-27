package com.tecdo.job.handler.condition;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.adm.api.delivery.mapper.TargetConditionMapper;
import com.tecdo.adm.api.doris.dto.AutoBundle;
import com.tecdo.adm.api.doris.mapper.ReportMapper;
import com.tecdo.job.util.ConditionHelper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

import static com.tecdo.adm.api.delivery.enums.ConditionEnum.*;
import static com.tecdo.common.constant.ConditionConstant.EXCLUDE;

/**
 * Created by Zeki on 2023/10/23
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AutoBundleJob {
    private final TargetConditionMapper targetConditionMapper;
    private final ReportMapper reportMapper;
    @XxlJob("autoBundleRefresh")
    public void autoBundleRefresh() {
        XxlJobHelper.log("获取bundle拉黑的定向条件");
        List<TargetCondition> conditionList = targetConditionMapper.blackConditionList(attributeList());
        if (conditionList == null || conditionList.isEmpty()) {
            XxlJobHelper.log("不存在自动拉黑定向条件，任务结束！");
            return;
        }

        Map<Integer, List<TargetCondition>> conditionMap = buildConditionMap(conditionList);

        // 确保每个存在拉黑定向条件的adGroup有attribute = “auto_bundle”的元组


        XxlJobHelper.log("获取参与校验的ad_group-bundle信息列表");
        List<AutoBundle> autoBundleInfoList = reportMapper
                .getAutoBundleInfoList(new ArrayList<>(conditionMap.keySet()),
                        LocalDate.now().minusDays(5).toString(),
                        LocalDate.now().minusDays(1).toString());

        XxlJobHelper.log("根据定向条件生成新黑名单");
        Map<Integer, Set<String>> blackListMap = generateBlackListMap(autoBundleInfoList, conditionMap);

        XxlJobHelper.log("查询target_condition表中已存在的自动拉黑名单");
        List<TargetCondition> autoBundleConditionList = targetConditionMapper.selectList(Wrappers
                .<TargetCondition>lambdaQuery()
                .eq(TargetCondition::getAttribute, AUTO_BUNDLE)
                .in(TargetCondition::getAdGroupId, blackListMap.keySet())
        );

        // 对数据库黑名单求并集
        for (TargetCondition autoBundleCondition : autoBundleConditionList) {
            // “,”分隔String构建Set<String>对象
            Set<String> preBlackList = new HashSet<>();
            Collections.addAll(preBlackList, autoBundleCondition.getValue().split(","));

            // 并集运算
            preBlackList.addAll(blackListMap.get(autoBundleCondition.getAdGroupId()));

            blackListMap.put(autoBundleCondition.getAdGroupId(), preBlackList);
        }

        // 构造合并后的黑名单
        List<TargetCondition> updatedAutoBundleList = new ArrayList<>();
        for (Integer key : blackListMap.keySet()) {
            TargetCondition condition = new TargetCondition();
            condition.setAdGroupId(key);
            condition.setAttribute(AUTO_BUNDLE.getDesc());
            condition.setOperation(EXCLUDE);
            condition.setValue(String.join(",", blackListMap.get(key)));
            updatedAutoBundleList.add(condition);
        }

        targetConditionMapper.updateAutoBundleList(updatedAutoBundleList);

    }

    /**
     * 根据bundle信息和定向条件bundle生成新黑名单
     * @param autoBundleInfoList adGroup-Bundle信息列表
     * @return 黑名单映射表
     */
    public Map<Integer, Set<String>> generateBlackListMap(List<AutoBundle> autoBundleInfoList,
                                                       Map<Integer, List<TargetCondition>> conditionMap) {
        Map<Integer, Set<String>> blackListMap = new HashMap<>();
        Map<String, String> valueMap = new HashMap<>();
        for (AutoBundle autoBundle : autoBundleInfoList) {
            double ctr = (double)autoBundle.getClickCount() / autoBundle.getImpCount();
            double roi = (double)autoBundle.getAdEstimatedCommission() / autoBundle.getBidPriceTotal();

            valueMap.put(BUNDLE_BLACK_CLICK.getDesc(), autoBundle.getClickCount().toString());
            valueMap.put(BUNDLE_BLACK_IMP.getDesc(), autoBundle.getImpCount().toString());
            valueMap.put(BUNDLE_BLACK_CTR.getDesc(), Double.toString(ctr));
            valueMap.put(BUNDLE_BLACK_ROI.getDesc(), Double.toString(roi));

            // 找到对应adGroupId的定向条件列表
            List<TargetCondition> conditionList = conditionMap.get(autoBundle.getAdGroupId());
            if (conditionList == null || conditionList.isEmpty()) continue;

            // 存在一个不满足的定向条件则拉黑
            boolean bundleIsValid = true;
            for (TargetCondition condition : conditionList) {
                if (ConditionHelper.compare(valueMap.get(condition.getAttribute()),
                        condition.getOperation(),
                        condition.getValue())) {
                    bundleIsValid = false;
                }
            }

            // 不需要拉黑则检查下一个bundle
            if (bundleIsValid) continue;

            Set<String> blackList = blackListMap.get(autoBundle.getAdGroupId());
            if (blackList != null) {
                blackList.add(autoBundle.getBundleId());
            }
            else {
                blackList = new HashSet<>();
                blackList.add(autoBundle.getBundleId());
            }
            blackListMap.put(autoBundle.getAdGroupId(), blackList);
        }

        return blackListMap;
    }

    /**
     * 构建每个adGroupId对应多个condition的Map
     * @param conditionList 条件列表
     * @return adGroup-Condition列表的映射
     */
    public Map<Integer, List<TargetCondition>> buildConditionMap(List<TargetCondition> conditionList) {
        Map<Integer, List<TargetCondition>> conditionMap = new HashMap<>();
        for (TargetCondition condition : conditionList) {
            int adGroupId = condition.getAdGroupId();
            List<TargetCondition> oldVal = conditionMap.getOrDefault(adGroupId, new ArrayList<>());
            oldVal.add(condition);
            conditionMap.put(adGroupId, oldVal);
        }
        return conditionMap;
    }

    /**
     * 返回自动拉黑需要用到的attribute
     * @return attributes
     */
    public List<String> attributeList() {
        return Arrays.asList(
                BUNDLE_BLACK_IMP.toString(),
                BUNDLE_BLACK_CLICK.toString(),
                BUNDLE_BLACK_CTR.toString(),
                BUNDLE_BLACK_ROI.toString()
        );
    }
}

