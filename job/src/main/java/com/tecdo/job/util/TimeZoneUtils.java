package com.tecdo.job.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
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

    /**
     * 获取到第二天需要多少秒
     * @return 到第二天需要的秒数
     */
    public static Long getNowToNextDaySeconds() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return (cal.getTimeInMillis() - System.currentTimeMillis()) / 1000;
    }
}
