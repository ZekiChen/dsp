package com.tecdo.job.handler.condition;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.adm.api.delivery.mapper.TargetConditionMapper;
import com.tecdo.adm.api.doris.dto.AutoBundle;
import com.tecdo.adm.api.doris.mapper.ReportMapper;
import com.tecdo.adm.api.log.entity.BizLogApi;
import com.tecdo.adm.api.log.enums.BizTypeEnum;
import com.tecdo.adm.api.log.enums.OptTypeEnum;
import com.tecdo.adm.api.log.mapper.BizLogApiMapper;
import com.tecdo.job.util.ConditionHelper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private final BizLogApiMapper bizLogApiMapper;
    private final String TITLE = "Ad Group Auto Update Bundle";

    @Value("${pac.auto-bundle.start-day:9}")
    private Integer startDay;

    @Value("${pac.auto-bundle.end-day:3}")
    private Integer endDay;

    @XxlJob("autoBundleRefresh")
    public void autoBundleRefresh() {
        XxlJobHelper.log("获取bundle拉黑的定向条件");
        List<TargetCondition> conditionList = targetConditionMapper.blackConditionList(attributeList());
        if (conditionList == null || conditionList.isEmpty()) {
            XxlJobHelper.log("不存在自动拉黑定向条件，任务结束！");
            return;
        }

        Map<Integer, List<TargetCondition>> conditionMap = conditionList.stream()
                .collect(Collectors.groupingBy(TargetCondition::getAdGroupId));

        XxlJobHelper.log("获取参与校验的ad_group-bundle信息列表");
        List<AutoBundle> autoBundleInfoList = reportMapper
                .getAutoBundleInfoList(conditionMap.keySet(),
                        LocalDate.now().minusDays(startDay).toString(),
                        LocalDate.now().minusDays(endDay).toString());

        XxlJobHelper.log("根据定向条件生成新黑名单");
        Map<Integer, Set<String>> newBlackListMap = generateBlackListMap(autoBundleInfoList, conditionMap);

        XxlJobHelper.log("查询target_condition表中已存在的自动拉黑名单");
        Map<Integer, TargetCondition> oldBlackListMap = targetConditionMapper.selectList(Wrappers
                .<TargetCondition>lambdaQuery()
                .eq(TargetCondition::getAttribute, AUTO_BUNDLE)
                .in(TargetCondition::getAdGroupId, newBlackListMap.keySet())
        ).stream().collect(Collectors.toMap(TargetCondition::getAdGroupId, Function.identity()));

        // 遍历已经存在的黑名单：对 已经存在的黑名单 & 新获取的黑名单 求并集
        for (TargetCondition oldBlackCondition : oldBlackListMap.values()) {
            // “,”分隔String构建Set<String>对象
            Set<String> preBlackList = new HashSet<>();
            Collections.addAll(preBlackList, oldBlackCondition.getValue().split(","));

            Set<String> newBlackList = newBlackListMap.get(oldBlackCondition.getAdGroupId());

            // 1.差集运算求新增 2.记录新增日志
            newBlackList.removeAll(preBlackList);
            insertLog(newBlackList, oldBlackCondition.getAdGroupId());

            // 并集运算求结果
            preBlackList.addAll(newBlackList);

            newBlackListMap.put(oldBlackCondition.getAdGroupId(), preBlackList);
        }

        // 构造合并后的黑名单
        List<TargetCondition> updatedAutoBundleList = new ArrayList<>();
        for (Integer key : newBlackListMap.keySet()) {
            // 若之前不存在黑名单，则日志记录
            if (!oldBlackListMap.containsKey(key)) {
                insertLog(newBlackListMap.get(key), key);
            }
            TargetCondition condition = new TargetCondition();
            condition.setAdGroupId(key);
            condition.setAttribute(AUTO_BUNDLE.getDesc());
            condition.setOperation(EXCLUDE);
            condition.setValue(String.join(",", newBlackListMap.get(key)));
            updatedAutoBundleList.add(condition);
        }

        targetConditionMapper.updateAutoBundleList(updatedAutoBundleList);

    }

    /**
     * 记录自动拉黑的新增bundle
     * @param diff 新增的bundle集合
     * @param adGroupId 所在adgroup的id
     */
    public void insertLog(Set<String> diff, int adGroupId) {
        if (diff == null || diff.isEmpty()) return;
        BizLogApi bizLogApi = new BizLogApi();
        bizLogApi.setBizId(adGroupId);
        bizLogApi.setOptType(OptTypeEnum.INSERT.getType());
        bizLogApi.setBizType(BizTypeEnum.AD_GROUP.getType());
        bizLogApi.setTitle(TITLE);
        bizLogApi.setContent("Bundle: " + EXCLUDE + " " + String.join(",", diff));
        bizLogApi.setCreator("system");
        bizLogApiMapper.insert(bizLogApi);
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
            // 分母为0则不进行拉黑操作
            double ctr = autoBundle.getImpCount() == 0 ?
                    Double.MAX_VALUE : (double)autoBundle.getClickCount() / autoBundle.getImpCount();
            double roi = autoBundle.getBidPriceTotal() == 0 ?
                    Double.MAX_VALUE : (double)autoBundle.getAdEstimatedCommission() / autoBundle.getBidPriceTotal();

            valueMap.put(BUNDLE_BLACK_CLICK.getDesc(), autoBundle.getClickCount().toString());
            valueMap.put(BUNDLE_BLACK_IMP.getDesc(), autoBundle.getImpCount().toString());
            valueMap.put(BUNDLE_BLACK_CTR.getDesc(), Double.toString(ctr));
            valueMap.put(BUNDLE_BLACK_ROI.getDesc(), Double.toString(roi));

            // 找到对应adGroupId的定向条件列表
            List<TargetCondition> conditionList = conditionMap.get(autoBundle.getAdGroupId());
            if (conditionList == null || conditionList.isEmpty()) continue;

            // 存在一个不需要拉黑的条件，则不拉黑
            boolean bundleIsValid = false;
            for (TargetCondition condition : conditionList) {
                if (!ConditionHelper.compare(valueMap.get(condition.getAttribute()),
                        condition.getOperation(),
                        condition.getValue())) {
                    bundleIsValid = true;
                    break;
                }
            }

            // 不需要拉黑则检查下一个bundle
            if (bundleIsValid) continue;

            Set<String> blackList = blackListMap.getOrDefault(autoBundle.getAdGroupId(), new HashSet<>());
            blackList.add(autoBundle.getBundleId());
            blackListMap.put(autoBundle.getAdGroupId(), blackList);
        }

        return blackListMap;
    }

    /**
     * 返回自动拉黑需要用到的attribute
     * @return attributes
     */
    public List<String> attributeList() {
        return Arrays.asList(
                BUNDLE_BLACK_IMP.getDesc(),
                BUNDLE_BLACK_CLICK.getDesc(),
                BUNDLE_BLACK_CTR.getDesc(),
                BUNDLE_BLACK_ROI.getDesc()
        );
    }
}

