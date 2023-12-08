package com.tecdo.job.handler.fraud.pixalate;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.ftp.Ftp;
import com.tecdo.job.foreign.pixalate.PixalateFtpBuilder;
import com.tecdo.job.service.CacheService;
import com.tecdo.starter.oss.OssTemplate;
import com.tecdo.starter.oss.domain.PacFile;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Date;

/**
 * 基于 FTP 协议拉取 IP 数据，并写入 OBS
 * <p>
 * Created by Zeki on 2023/12/5
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IpSinkToObsJob {

    private final PixalateFtpBuilder pixalateFtpBuilder;
    private final OssTemplate ossTemplate;
    private final CacheService cacheService;

    private final String IP_FILE_PREFIX = "/ipblocklistv2/GenericIPBlacklisting_";
    private final String CSV_SUFFIX = ".csv";

    @XxlJob("pixalate-ip-sink-to-obs")
    public void ipSinkToObsJob() {
        XxlJobHelper.log("Job start: IP source is pulled based on FTP and sink to OBS");

        Ftp ftp = pixalateFtpBuilder.build();
        XxlJobHelper.log("The FTP connection was established successfully");

        String today = DateUtil.format(new Date(), DatePattern.PURE_DATE_PATTERN);
        String yesterday = DateUtil.format(DateUtil.yesterday(), DatePattern.PURE_DATE_PATTERN);
        String todayFile = buildFtpFilePath(today);
        String yesterdayFile = buildFtpFilePath(yesterday);

        if (ftp.exist(todayFile)) {
            XxlJobHelper.log("Today's ip source is ready, file path: {}", todayFile);
            String modificationTime = getModificationTime(ftp, todayFile);
            if (cacheService.getPixalateCache().hasIpFileUpload(today, modificationTime)) {
                XxlJobHelper.log("Today's current ip source has sunk to obs, file path: {}", todayFile);
                return;
            }
            PacFile pacFile = downloadAndUpload(ftp, today, "ip_v1");
            XxlJobHelper.log("Today's ip source sink to obs successfully, file path: {}", pacFile.getName());
            cacheService.getPixalateCache().setIpFileUploadFinish(today, modificationTime);
        } else if (ftp.exist(yesterdayFile)) {
            XxlJobHelper.log("Yesterday's ip source is ready, file path: {}", yesterdayFile);
            String modificationTime = getModificationTime(ftp, yesterdayFile);
            if (cacheService.getPixalateCache().hasIpFileUpload(yesterday, modificationTime)) {
                XxlJobHelper.log("Yesterday's current ip source has sunk to obs, file path: {}", todayFile);
                return;
            }
            PacFile pacFile = downloadAndUpload(ftp, yesterday, "ip_v2");
            XxlJobHelper.log("Yesterday's ip source sink to obs successfully, file path: {}", pacFile.getName());
            cacheService.getPixalateCache().setIpFileUploadFinish(yesterday, modificationTime);
        } else {
            XxlJobHelper.log("None of yesterday's or today's ip sources");
        }

        IoUtil.close(ftp);
    }

    private PacFile downloadAndUpload(Ftp ftp, String day, String fileName) {
        String ossFileName = day + StrUtil.SLASH + fileName + CSV_SUFFIX;
        try {
            InputStream in = ftp.getClient().retrieveFileStream(buildFtpFilePath(day));
            return ossTemplate.uploadFile(ossFileName, in);
        } catch (IOException e) {
            XxlJobHelper.log("IP source retrieveFileStream error, oss file name: {}", ossFileName);
            throw new RuntimeException(e);
        }
    }

    private String buildFtpFilePath(String day) {
        return IP_FILE_PREFIX + day + CSV_SUFFIX;
    }

    private String getModificationTime(Ftp ftp, String file) {
        try {
            return ftp.getClient().getModificationTime(file);
        } catch (IOException e) {
            XxlJobHelper.log("Failed to obtain today's ip source file modification time, file path: {}", file);
            throw new RuntimeException(e);
        }
    }
}
