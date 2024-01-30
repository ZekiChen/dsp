package com.tecdo.job.handler.fraud.pixalate;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.util.StrUtil;
import com.ejlchina.okhttps.HTTP;
import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.fastjson.FastjsonMsgConvertor;
import com.tecdo.core.launch.thread.ThreadPool;
import com.tecdo.starter.oss.OssTemplate;
import com.tecdo.starter.oss.domain.PacFile;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

/**
 * Created by Elwin on 2024/1/25
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostBidSinkToObsJob {
    @Autowired
    @Resource(name = "hwobsbkTemplate")
    private OssTemplate hwobsbkTemplate;

    private final ThreadPool threadPool;

    @XxlJob("pixalate-post-bid-sink-to-obs")
    public void postBidSinkToObs() {
        XxlJobHelper.log("Job start: pixalate postBid data is being pulled from api and sink to OBS");
        String date = DateUtil.yesterday().toDateStr();
        int maxLoopTime = 20; // 避免无限循环的情况出现
        int order = 0;

        while (order < maxLoopTime) {
            String urlStr = buildUrl(date, date, order); // 构建请求
            String csvUrl = reqCsvFromPixalate(urlStr); // csv文件url
            if (isCsvEmpty(csvUrl)) break; // 若文件为空，说明当天的数据已经遍历完毕，跳出

            /**
             * test
             */
/*            String csvUrl = "https://dashboardcdn.pixalate.com/www/exported/tdo/2024-01-29/372rmf6vivsbhla16u1m5oucmc/Report_for_2024-01-28.csv";
            order = 100;*/


            // 在线程池中读区csv写入obs
            final int curOrder = order;
            final String curCsvUrl = csvUrl;
            threadPool.execute(() -> {
                uploadToHuaWeiOBS(curCsvUrl, date, curOrder);
            });
            order++;
        }
    }

    /**
     * 构建url
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param start 起始索引
     * @return 请求url
     */
    public String buildUrl(String startDate, String endDate, Integer start) {
        int maxItemSize = 1000000; // 单个文件最大item数量
        String baseUrl = "https://api.pixalate.com/api/v2/analytics/reports/default?";
        String simpleSql = "advertiserId,campaignId,city,countryCode,day,deviceBrandName," +
                " deviceModelId,deviceMarketingName, fraudType, kv4,kv7,kv18,kv19,kv26, publisherId" +
                " WHERE day>='" + startDate + "' AND day<='" + endDate + "'";
        String url = UrlBuilder.of(baseUrl).addQuery("q", simpleSql)
                .addQuery("exportUri", true)
                .addQuery("start", start * maxItemSize)
                .toString();
        return url;
    }

    private void uploadToHuaWeiOBS(String url, String date, Integer order) {
        if (StrUtil.isEmpty(url) || !url.endsWith(".csv")) {
            return;
        }
        String objectKey = null;
        long start = System.currentTimeMillis();

        XxlJobHelper.log("upload file to OBS: " + url);
        try {
            URL urlObj = new URL(url);
            try (InputStream inputStream = urlObj.openStream()) {
                objectKey = generateObjkey(url, date, order);
                XxlJobHelper.log("open stream to upload: {}ms, object key: {}", System.currentTimeMillis() - start, objectKey);

                PacFile pacFile = hwobsbkTemplate.uploadFile(objectKey, inputStream);
                XxlJobHelper.log("successfully pushed to obs: {}s, object key: {}", (System.currentTimeMillis() - start) / 1000, objectKey);
            }
        } catch (IOException e) {
            log.error("上传错误, spent: {}s, object key: {}", (System.currentTimeMillis() - start) / 1000, objectKey, e);
            throw new RuntimeException("上传错误");
        }

    }

    /**
     * 根据日期&url生成objKey
     * @param url url
     * @param date url对应的日期
     * @param order 第几份文件
     * @return objKey
     */
    private String generateObjkey(String url, String date, Integer order) {
        String[] domainList = url.split("/");
        String fileName = domainList[domainList.length - 1];
        String id = domainList[domainList.length - 2];

        String[] dateList = date.split("-");

        String objKey = "year=" + dateList[0] +
                "/month=" + dateList[1] +
                "/day=" + dateList[2] +
                "/" +
                "brainx-flume-odl-postbid-" + id + "-" + "part" + order + ".csv";
        return objKey;
    }

    /**
     * 解析csv文件链接，判空
     * @param csvUrl csv链接
     * @return 是否为空
     */
    private boolean isCsvEmpty(String csvUrl) {
        try {
            URL url = new URL(csvUrl);
            URLConnection connection = url.openConnection();
            int fileSize = connection.getContentLength();
            return fileSize <= 0;
        }
        catch (Exception e) {
            throw new RuntimeException("csv文件解析错误");
        }
    }

    private String reqCsvFromPixalate(String url) {
        HTTP http = HTTP.builder()
                .addMsgConvertor(new FastjsonMsgConvertor())
                .config((OkHttpClient.Builder builder) -> {
                    // 读取超时时间(8min)
                    builder.readTimeout(8 * 60, TimeUnit.SECONDS);
                })
                .build();
        HttpResult result = http.sync(url)
                .addHeader("x-api-key", "3oGBNYdp70qAhrkKNze8zMlgt6AIlA8T")
                .get();

        return result.getBody().toBean(String.class);
    }
}
