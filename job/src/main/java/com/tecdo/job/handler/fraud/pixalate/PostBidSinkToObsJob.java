package com.tecdo.job.handler.fraud.pixalate;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.thread.ThreadUtil;
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
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
        int order = 0;
        long start = System.currentTimeMillis();

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        String requestUrl = buildUrl(date, date, order); // 构建请求
        String batchCsvUrl = reqCsvFromPixalate(requestUrl); // 发送请求，返回一个csv文件的url
        ThreadUtil.sleep(10, TimeUnit.SECONDS);  // 太快打开会返回404
        List<String> urls = readEveryCsvUrlFromBatchCsvUrl(start, batchCsvUrl);

        for (String url : urls) {
            if (isCsvEmpty(url)) break; // 若文件为空，说明当天的数据已经遍历完毕，跳出
            // 在线程池中读区csv写入obs
            final int curOrder = order;
            final String curCsvUrl = url;
            futures.add(CompletableFuture.runAsync(() ->
                    uploadToHuaWeiOBS(curCsvUrl, date, curOrder), threadPool.getExecutor()));
            order++;
        }

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).get(3600, TimeUnit.SECONDS);
        } catch (Exception e) {
            XxlJobHelper.log("Job execute error");
            throw new RuntimeException(e);
        }

        XxlJobHelper.log("Job end: pixalate postBid data sink to OBS successfully: {}s",
                (System.currentTimeMillis() - start) / 1000);
    }

    @NotNull
    private static List<String> readEveryCsvUrlFromBatchCsvUrl(long start, String batchCsvUrl) {
        List<String> urls = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new URL(batchCsvUrl).openStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.endsWith(".csv")) {
                    urls.add(line);
                }
            }
        } catch (MalformedURLException e) {
            XxlJobHelper.log("url open stream error, spent: {}s, batch csv url: {}", (System.currentTimeMillis() - start) / 1000, batchCsvUrl);
            throw new RuntimeException(e);
        } catch (IOException e) {
            XxlJobHelper.log("read batch csv file error, spent: {}s, batch csv url: {}", (System.currentTimeMillis() - start) / 1000, batchCsvUrl);
            throw new RuntimeException(e);
        }
        return urls;
    }

    /**
     * 构建url
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param start 起始索引
     * @return 请求url
     */
    public String buildUrl(String startDate, String endDate, Integer start) {
        String baseUrl = "https://api.pixalate.com/api/v2/analytics/reports/default?";
        String simpleSql = "advertiserId,campaignId,city,countryCode,day,deviceBrandName," +
                " deviceModelId,deviceMarketingName, fraudType, kv4,kv7,kv18,kv19,kv26, publisherId" +
                " WHERE day>='" + startDate + "' AND day<='" + endDate + "'";
//                " WHERE fraudType != '' AND day>='" + startDate + "' AND day<='" + endDate + "'";
        return UrlBuilder.of(baseUrl).addQuery("q", simpleSql)
                .addQuery("exportUri", true)
//                .addQuery("start", start * maxItemSize)
                .addQuery("isLargeResultSet", true)
                .toString();
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
