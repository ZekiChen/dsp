package com.tecdo.job.service.cache;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.tecdo.common.constant.CacheConstant;
import com.tecdo.starter.redis.PacRedis;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Created by Zeki on 2023/12/6
 */
@Service
@RequiredArgsConstructor
public class PixalateCache {

    private final PacRedis pacRedis;
    private final static String HAS_IP_UPLOAD_CACHE = "ip-upload";
    private final static String HAS_DEVICE_ID_UPLOAD_CACHE = "deviceId-upload";

    private final static String HAS_IP_SYNC_CACHE = "ip-sync";
    private final static String HAS_DEVICE_ID_SYNC_CACHE = "deviceId-sync";

    private final static String FRAUD_IP_CACHE = "ip";
    private final static String FRAUD_DEVICE_ID_CACHE = "deviceId";

    @Value("${pac.pixalate.expire.ip-csv:24}")
    private long ipFileExpire;
    @Value("${pac.pixalate.expire.deviceId-csv:24}")
    private long deviceIdFileExpire;

    @Value("${pac.pixalate.expire.ip:48}")
    private long ipExpire;
    @Value("${pac.pixalate.expire.deviceId:48}")
    private long deviceIdExpire;

    /**
     * 根据 day-timestamp（YYYYMMDDHHMMSS）判断该 ip 文件是否已经上传过 obs
     */
    public boolean hasIpFileUpload(String day, String timestamp) {
        String key = CacheConstant.PIXALATE_CACHE
                .concat(StrUtil.COLON).concat(HAS_IP_UPLOAD_CACHE)
                .concat(StrUtil.COLON).concat(day)
                .concat(StrUtil.COLON).concat(timestamp);
        return pacRedis.exists(key);
    }

    /**
     * 将该 day-timestamp 的 ip 文件标记为已上传至 obs 完毕
     */
    public boolean setIpFileUploadFinish(String day, String timestamp) {
        String key = CacheConstant.PIXALATE_CACHE
                .concat(StrUtil.COLON).concat(HAS_IP_UPLOAD_CACHE)
                .concat(StrUtil.COLON).concat(day)
                .concat(StrUtil.COLON).concat(timestamp);
        return pacRedis.setIfAbsent(key, 1, ipFileExpire, TimeUnit.HOURS);
    }

    /**
     * 根据 day-timestamp（YYYYMMDDHHMMSS）判断该 设备id 文件是否已经上传过 obs
     */
    public boolean hasDeviceIdFileUpload(String day, String timestamp) {
        String key = CacheConstant.PIXALATE_CACHE
                .concat(StrUtil.COLON).concat(HAS_DEVICE_ID_UPLOAD_CACHE)
                .concat(StrUtil.COLON).concat(day)
                .concat(StrUtil.COLON).concat(timestamp);
        return pacRedis.exists(key);
    }

    /**
     * 将该 day-timestamp 的 设备id 文件标记为已上传至 obs 完毕
     */
    public boolean setDeviceIdFileUploadFinish(String day, String timestamp) {
        String key = CacheConstant.PIXALATE_CACHE
                .concat(StrUtil.COLON).concat(HAS_DEVICE_ID_UPLOAD_CACHE)
                .concat(StrUtil.COLON).concat(day)
                .concat(StrUtil.COLON).concat(timestamp);
        return pacRedis.setIfAbsent(key, 1, deviceIdFileExpire, TimeUnit.HOURS);
    }

    /**
     * 判断昨天指定版本的 ip 文件是否已更新至作弊池
     */
    public boolean hasIpFileSync(String version) {
        String yesterday = DateUtil.format(DateUtil.yesterday(), DatePattern.PURE_DATE_PATTERN);
        String key = CacheConstant.PIXALATE_CACHE
                .concat(StrUtil.COLON).concat(HAS_IP_SYNC_CACHE)
                .concat(StrUtil.COLON).concat(yesterday)
                .concat(StrUtil.COLON).concat(version);
        return pacRedis.exists(key);
    }

    /**
     * 判断昨天指定版本的 设备id 文件是否已更新至作弊池
     */
    public boolean hasDeviceIdFileSync(String version) {
        String yesterday = DateUtil.format(DateUtil.yesterday(), DatePattern.PURE_DATE_PATTERN);
        String key = CacheConstant.PIXALATE_CACHE
                .concat(StrUtil.COLON).concat(HAS_DEVICE_ID_SYNC_CACHE)
                .concat(StrUtil.COLON).concat(yesterday)
                .concat(StrUtil.COLON).concat(version);
        return pacRedis.exists(key);
    }

    /**
     * 将昨天指定版本的 ip 文件标识为已同步至作弊池
     */
    public void setIpFileSyncFinish(String version) {
        String yesterday = DateUtil.format(DateUtil.yesterday(), DatePattern.PURE_DATE_PATTERN);
        String key = CacheConstant.PIXALATE_CACHE
                .concat(StrUtil.COLON).concat(HAS_IP_SYNC_CACHE)
                .concat(StrUtil.COLON).concat(yesterday)
                .concat(StrUtil.COLON).concat(version);
        pacRedis.set(key, 1);
        pacRedis.expire(key, ipFileExpire * 3600);
    }

    /**
     * 将昨天指定版本的 设备id 文件标识为已同步至作弊池
     */
    public void setDeviceIdFileSyncFinish(String version) {
        String yesterday = DateUtil.format(DateUtil.yesterday(), DatePattern.PURE_DATE_PATTERN);
        String key = CacheConstant.PIXALATE_CACHE
                .concat(StrUtil.COLON).concat(HAS_DEVICE_ID_SYNC_CACHE)
                .concat(StrUtil.COLON).concat(yesterday)
                .concat(StrUtil.COLON).concat(version);
        pacRedis.set(key, 1);
        pacRedis.expire(key, ipFileExpire * 3600);
    }

    /**
     * 更新 ip 作弊池，并刷新过期时间
     */
    public void setFraudIp(String ip, String fraudType, String probability) {
        String key = CacheConstant.PIXALATE_CACHE
                .concat(StrUtil.COLON).concat(FRAUD_IP_CACHE)
                .concat(StrUtil.COLON).concat(ip);
        pacRedis.set(key, fraudType + StrUtil.COMMA + probability);
        pacRedis.expire(key, ipExpire * 3600);
    }

    /**
     * 更新 设备id 作弊池，并刷新过期时间
     */
    public void setFraudDeviceId(String deviceId, String fraudType, String probability) {
        String key = CacheConstant.PIXALATE_CACHE
                .concat(StrUtil.COLON).concat(FRAUD_DEVICE_ID_CACHE)
                .concat(StrUtil.COLON).concat(deviceId);
        pacRedis.set(key, fraudType + StrUtil.COMMA + probability);
        pacRedis.expire(key, deviceIdExpire * 3600);
    }
}
