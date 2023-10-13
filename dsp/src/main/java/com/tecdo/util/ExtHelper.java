package com.tecdo.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.tecdo.domain.openrtb.request.Source;
import com.tecdo.enums.biz.PlacementTypeEnum;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 扩展位解析工具
 *
 * Created by Zeki on 2023/9/12
 */
public class ExtHelper {

    private final static String LOOKME_PLACEMENT_TYPE = "placementType";

    public static boolean isRewarded(Object ext) {
        try {
            LinkedHashMap<String, Object> extMap = (LinkedHashMap) ext;
            if (extMap != null && extMap.containsKey(LOOKME_PLACEMENT_TYPE)) {
                return PlacementTypeEnum.REWARDED.getDesc().equals(extMap.get(LOOKME_PLACEMENT_TYPE).toString());
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static String listSChain(Source source) {
        if (source != null) {
            try {
                LinkedHashMap<String, Object> extMap = (LinkedHashMap) source.getExt();
                if (extMap != null && extMap.containsKey("schain")) {
                    LinkedHashMap<String, Object> sChain = (LinkedHashMap<String, Object>) extMap.get("schain");
                    if (sChain != null && sChain.containsKey("nodes")) {
                        List<LinkedHashMap<String, Object>> nodes = (List<LinkedHashMap<String, Object>>) sChain.get("nodes");
                        if (CollUtil.isNotEmpty(nodes)) {
                            return nodes.stream()
                                    .filter(map -> map.containsKey("asi"))
                                    .map(map -> map.get("asi").toString())
                                    .collect(Collectors.joining(StrUtil.COMMA));
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
        return null;
    }
}
