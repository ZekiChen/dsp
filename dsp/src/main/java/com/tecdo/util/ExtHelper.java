package com.tecdo.util;

import com.tecdo.domain.openrtb.request.Source;
import com.tecdo.enums.biz.PlacementTypeEnum;

import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 扩展位解析工具
 * Created by Zeki on 2023/9/12
 */
public class ExtHelper {

  private final static String LOOKME_PLACEMENT_TYPE = "placementType";

  public static boolean isRewarded(Object ext) {
    try {
      LinkedHashMap<String, Object> extMap = (LinkedHashMap) ext;
      if (extMap != null && extMap.containsKey(LOOKME_PLACEMENT_TYPE)) {
        return PlacementTypeEnum.REWARDED.getDesc()
                                         .equals(extMap.get(LOOKME_PLACEMENT_TYPE).toString());
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
          LinkedHashMap<String, Object> sChain =
            (LinkedHashMap<String, Object>) extMap.get("schain");
          if (sChain != null && sChain.containsKey("nodes")) {
            List<LinkedHashMap<String, Object>> nodes =
              (List<LinkedHashMap<String, Object>>) sChain.get("nodes");
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

  public static String listSChainForPixalate(Source source) {
    if (source != null) {
      try {
        LinkedHashMap<String, Object> extMap = (LinkedHashMap) source.getExt();
        if (extMap != null && extMap.containsKey("schain")) {
          LinkedHashMap<String, Object> sChain =
            (LinkedHashMap<String, Object>) extMap.get("schain");
          if (sChain != null && sChain.containsKey("nodes")) {
            List<LinkedHashMap<String, Object>> nodes =
              (List<LinkedHashMap<String, Object>>) sChain.get("nodes");
            if (CollUtil.isNotEmpty(nodes)) {
              StringBuilder sb = new StringBuilder();
              sb.append(sChain.get("ver")).append(",").append(sChain.get("complete"));
              nodes.stream().filter(map -> map.containsKey("asi")).forEach(i -> {
                sb.append("!")
                  .append(encode(i.getOrDefault("asi","")))
                  .append(",")
                  .append(encode(i.getOrDefault("sid","")))
                  .append(",")
                  .append(encode(i.getOrDefault("hp","")))
                  .append(",")
                  .append(encode(i.getOrDefault("rid","")))
                  .append(",")
                  .append(encode(i.getOrDefault("name","")))
                  .append(",")
                  .append(encode(i.getOrDefault("domain","")));
              });
              return sb.toString();
            }
          }
        }
      } catch (Exception e) {
      }
    }
    return "";
  }


  public static String encode(Object content) {
    if (content == null) {
      return "";
    }
    try {
      return URLEncoder.encode(content.toString(), "utf-8");
    } catch (Exception e) {
      return "";
    }
  }
}
