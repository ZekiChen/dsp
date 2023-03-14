package com.tecdo.job.util;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.json.JSONUtil;

import com.alibaba.fastjson.JSONObject;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 企业微信群机器人操作工具类
 *
 * @author marc
 * @date 2022-08-15
 */
public class WeChatRobotUtils {

    /**
     * 发送文字消息
     * 发送参数请求格式
     * {
     * "msgtype":"text",
     * "text":{
     * "content":"@suki 测试"
     * },
     * "safe":0
     * }
     *
     * @param msg 需要发送的消息
     * @return 是否成功
     */
    public static Boolean sendTextMsg(String robotLink, String msg, Boolean isAtAll) throws Exception {
        //请求参数
        JSONObject text = new JSONObject();
        text.put("content", msg);
        if (BooleanUtil.isTrue(isAtAll)) {
            text.put("mentioned_list", Collections.singletonList("@all"));
        }
        JSONObject reqBody = new JSONObject();
        reqBody.put("msgtype", "text");
        reqBody.put("text", text);
        reqBody.put("safe", 0);
        //调用机器人
        String respMsg = callWeChatBot(robotLink, reqBody.toString());
        //是否请求成功
        if (respMsg == null || respMsg.isEmpty()) {
            return false;
        }
        return "0".equals(respMsg.substring(11, 12));
    }

    /**
     * 发送文字消息
     * 发送参数请求格式
     * {
     * "msgtype":"text",
     * "text":{
     * "content":"@suki 测试"
     * },
     * "safe":0
     * }
     *
     * @param msg 需要发送的消息
     * @return 是否成功
     */
    public static Boolean sendTextMsg(String robotLink, String msg) throws Exception {
        return sendTextMsg(robotLink, msg, Boolean.FALSE);
    }

    /**
     * 重试三次发送消息
     *
     * @param robotLink 机器人操作地址
     * @param retry     重试次数
     * @param msg       发送内容
     * @param isAtAll
     * @return true-成功,false-失败
     */
    public static Boolean retry3SendTextMsg(Integer retry, String robotLink, String msg, Boolean isAtAll) throws Exception {
        boolean flag = false;
        //重试次数默认一次
        if (retry == null || retry <= 0) {
            retry = 1;
        }
        while (!flag) {
            if (retry <= 0) {
                break;
            }
            flag = WeChatRobotUtils.sendTextMsg(robotLink, msg, isAtAll);
            retry--;
        }
        return flag;
    }

    /**
     * 调用群机器人
     *
     * @param reqBody 接口请求参数
     * @throws Exception 可能有IO异常
     */
    public static String callWeChatBot(String robotLink, String reqBody) throws Exception {
        // 构造RequestBody对象，用来携带要提交的数据；需要指定MediaType，用于描述请求/响应 body 的内容类型
        MediaType contentType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(contentType, reqBody);
        // 调用群机器人
        return okHttp(body, robotLink);
    }

    /**
     * 请求接口
     *
     * @param body 携带需要提交的数据
     * @param url  请求地址
     * @return 接口返回数据字符串
     */
    public static String okHttp(RequestBody body, String url) throws Exception {
        //构造和配置OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                //设置连接超时时间
                .connectTimeout(10, TimeUnit.SECONDS)
                //设置读取超时时间
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
        //构造Request对象
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                //响应消息不缓存
                .addHeader("cache-control", "no-cache")
                .build();
        //构建Call对象，通过Call对象的execute()方法提交异步请求
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 请求结果处理
        if (response != null && response.body() != null) {
            byte[] result = response.body().bytes();
            return new String(result);
        }
        return null;
    }

    /**
     * 发送附件消息
     * 发送参数请求格式
     * {
     * "msgtype":"file",
     * "file":{
     * "media_id": "3a8asd892asd8asd"
     * },
     * }
     *
     * @param robotUrl 机器人群hook连接
     * @param file     文件
     * @param fileName 展示的文件全名称
     * @return 是否成功
     */
    public static Boolean sendFileMsg(String robotUrl, File file, String fileName) throws Exception {
        String mediaId = uploadFile(robotUrl, file, fileName);
        JSONObject mediaFile = new JSONObject();
        mediaFile.put("media_id", mediaId);
        JSONObject reqBody = new JSONObject();
        reqBody.put("msgtype", "file");
        reqBody.put("file", mediaFile);
        String respMsg = callWeChatBot(robotUrl, reqBody.toString());
        if (respMsg == null || respMsg.isEmpty()) {
            return false;
        }
        return "0".equals(respMsg.substring(11, 12));
    }

    /**
     * 上传文件
     *
     * @param robotUrl url
     * @param file     附件
     * @return
     * @throws Exception
     */
    private static String uploadFile(String robotUrl, File file, String fileName) throws Exception {
        String key = robotUrl.substring(robotUrl.indexOf("key="));
        String mediaUrl = "https://qyapi.weixin.qq.com/cgi-bin/webhook/upload_media?type=file&" + key;
        MediaType contentType = MediaType.parse("application/form-data; boundary");
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("media", fileName, RequestBody.create(contentType, file))
                .build();

        cn.hutool.json.JSONObject respContent = JSONUtil.parseObj(okHttp(requestBody, mediaUrl));
        if (respContent.getInt("errcode") != 0) {
            throw new RuntimeException(respContent.getStr("errmsg"));
        }
        return respContent.getStr("media_id");
    }

}
