package com.tecdo.ab.util;

import com.tecdo.ab.AbTestConfigKey;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.entity.AbTestConfig;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AbTestConfigHelper {


  public static boolean execute(List<AbTestConfig> configList,
                                BidRequest bidRequest,
                                Integer affiliateId) {
    if (CollectionUtils.isEmpty(configList)) {
      return false;
    }
    for (AbTestConfig abTestConfig : configList) {
      if (StringUtils.isAnyEmpty(abTestConfig.getAttribute(),
                                 abTestConfig.getOperation(),
                                 abTestConfig.getValue())) {
        return false;
      }
      String source;
      switch (abTestConfig.getAttribute()) {
        case AbTestConfigKey.AFFILIATE:
          source = String.valueOf(affiliateId);
          break;
        case AbTestConfigKey.HOUR:
          source = String.valueOf(DateUtil.thisHour(true));
          break;
        case AbTestConfigKey.BUNDLE:
          source = bidRequest.getApp().getBundle();
          break;
        case AbTestConfigKey.DEVICE_COUNTRY:
          source = bidRequest.getDevice().getGeo().getCountry();
          break;
        case AbTestConfigKey.DEVICE_OS:
          source = bidRequest.getDevice().getOs();
          break;
        case AbTestConfigKey.DEVICE_OSV:
          source = bidRequest.getDevice().getOsv();
          break;
        case AbTestConfigKey.DEVICE_MAKE:
          source = bidRequest.getDevice().getMake();
          break;
        case AbTestConfigKey.DEVICE_LANGUAGE:
          source = bidRequest.getDevice().getLanguage();
          break;
        default:
          return false;
      }
      if (!ConditionHelper.compare(source, abTestConfig.getOperation(), abTestConfig.getValue())) {
        return false;
      }
    }
    return true;
  }

}
