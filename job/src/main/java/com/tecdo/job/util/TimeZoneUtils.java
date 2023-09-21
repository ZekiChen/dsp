package com.tecdo.job.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * Created by Elwin on 2023/9/20
 */
public class TimeZoneUtils {
    /**
     * 时区转换，获取东八区LocalDate时间
     * @return 东八区时间
     */
    public static LocalDate dateInChina() {
        // 获取服务器当前时间（UTC）
        Date currentDateUTC = new Date();

        // 创建一个ZoneId表示东八区
        ZoneId zoneId = ZoneId.of("Asia/Shanghai");

        // 使用ZoneId将UTC时间转换为东八区时间的LocalDate
        LocalDate currentDateChina = currentDateUTC.toInstant().atZone(zoneId).toLocalDate();

        return currentDateChina;
    }
}
