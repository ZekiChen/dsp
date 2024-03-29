package com.tecdo.adm.api.doris.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by Zeki on 2023/8/25
 */
@DS("doris-ads")
public interface RequestMapper {

    String countDevice(@Param("startDate") String startDate, @Param("endDate") String endDate,
                       @Param("affiliates") List<String> affiliates,
                       @Param("countries") List<String> countries,
                       @Param("inDeviceMakes") List<String> inDeviceMakes, @Param("exDeviceMakes") List<String> exDeviceMakes,
                       @Param("deviceOSs") List<String> deviceOSs,
                       @Param("inBundles") List<String> inBundles, @Param("exBundles") List<String> exBundles);

    String countDeviceWithGP(@Param("startDate") String startDate, @Param("endDate") String endDate,
                             @Param("affiliates") List<String> affiliates,
                             @Param("countries") List<String> countries,
                             @Param("inDeviceMakes") List<String> inDeviceMakes, @Param("exDeviceMakes") List<String> exDeviceMakes,
                             @Param("deviceOSs") List<String> deviceOSs,
                             @Param("categories") List<String> categories,
                             @Param("tags") List<String> tags,
                             @Param("inBundles") List<String> inBundles, @Param("exBundles") List<String> exBundles);
}
