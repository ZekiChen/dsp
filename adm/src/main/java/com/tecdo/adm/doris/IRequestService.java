package com.tecdo.adm.doris;

import java.util.List;

/**
 * Created by Zeki on 2023/4/5
 */
public interface IRequestService {

    String countDevice(String startDate, String endDate,
                       List<String> affiliates,
                       List<String> countries,
                       List<String> inDeviceMakes, List<String> exDeviceMakes,
                       List<String> deviceOSs,
                       List<String> inBundles, List<String> exBundles);

    String countDeviceWithGP(String startDate, String endDate,
                             List<String> affiliates,
                             List<String> countries,
                             List<String> inDeviceMakes, List<String> exDeviceMakes,
                             List<String> deviceOSs,
                             List<String> categories,
                             List<String> tags,
                             List<String> inBundles, List<String> exBundles);
}
