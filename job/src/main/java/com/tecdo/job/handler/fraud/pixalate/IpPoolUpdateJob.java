package com.tecdo.job.handler.fraud.pixalate;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.ftp.Ftp;
import com.tecdo.core.launch.thread.ThreadPool;
import com.tecdo.job.service.CacheService;
import com.tecdo.starter.oss.OssTemplate;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 拉取 OBS 中的 IP 数据更新至 Redis 作弊池
 * <p>
 * Created by Zeki on 2023/12/6
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IpPoolUpdateJob {

    private final OssTemplate ossTemplate;
    private final CacheService cacheService;
    private final ThreadPool threadPool;

    private final String OSS_PREFIX = "pixalate";
    private final String CSV_SUFFIX = ".csv";

    @XxlJob("pixalate-ip-pool-update")
    public void ipPoolUpdate() {
        XxlJobHelper.log("Job start: Update the ip file from OBS to Redis");

        String fileV2 = buildOssFilePath("ip_v2");
        String fileV1 = buildOssFilePath("ip_v1");
        if (ossTemplate.existFile(fileV2)) {
            XxlJobHelper.log("Yesterday's ip file exists, oss path: {}", fileV2);
            if (cacheService.getPixalateCache().hasIpFileSync("v2")) {
                XxlJobHelper.log("Yesterday's ip file has sync, oss path: {}", fileV2);
                return;
            }
            syncToRedis(fileV2);
            cacheService.getPixalateCache().setIpFileSyncFinish("v2");
        } else if (ossTemplate.existFile(fileV1)) {
            XxlJobHelper.log("Yesterday's ip file exists, oss path: {}", fileV1);
            if (cacheService.getPixalateCache().hasIpFileSync("v1")) {
                XxlJobHelper.log("Yesterday's ip file has sync, oss path: {}", fileV1);
                return;
            }
            syncToRedis(fileV1);
            cacheService.getPixalateCache().setIpFileSyncFinish("v1");
        } else {
            XxlJobHelper.log("Yesterday's ip file not exists");
        }
    }

    private String buildOssFilePath(String fileName) {
        String yesterday = DateUtil.format(DateUtil.yesterday(), DatePattern.PURE_DATE_PATTERN);
        return OSS_PREFIX + StrUtil.SLASH + yesterday + StrUtil.SLASH + fileName + CSV_SUFFIX;
    }

    private void syncToRedis(String file) {
        int limit = 10_0000;
        List<String> list = new ArrayList<>(limit);

        try (BufferedReader br = ossTemplate.download(file)) {
            XxlJobHelper.log("Yesterday's ip file is being loaded and sync to redis, oss path: {}", file);
            String line;
            while ((line = br.readLine()) != null) {
                if (list.size() == limit) {
                    doSyncToRedis(list);
                    list.clear();
                }
                list.add(line);
            }
            doSyncToRedis(list);
            XxlJobHelper.log("Yesterday's ip file sync to redis successfully, oss path: {}", file);
        } catch (IOException e) {
            XxlJobHelper.log("Failed to sync yesterday's ip file, oss path: {}", file);
            throw new RuntimeException(e);
        }
    }

    private void doSyncToRedis(List<String> list) {
        List<CompletableFuture<Void>> futures = list.stream()
                .filter(line -> line.split(StrUtil.COMMA).length == 3)
                .map(line -> CompletableFuture.runAsync(() -> {
                    String[] arr = line.split(StrUtil.COMMA);
                    String ip = arr[0];
                    String fraudType = arr[1];
                    String probability = arr[2];
                    cacheService.getPixalateCache().setFraudIp(ip, fraudType, probability);
                }, threadPool.getExecutor()))
                .collect(Collectors.toList());

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).get(600, TimeUnit.SECONDS);
        } catch (Exception e) {
            XxlJobHelper.log("sync to redis error");
            throw new RuntimeException(e);
        }
    }
}
