package com.tecdo.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;
import com.tecdo.common.constant.Constant;
import com.tecdo.constant.RequestKey;
import com.tecdo.exception.ServiceException;

import java.nio.charset.StandardCharsets;

/**
 * 通知请求的签名处理
 * <p>
 * Created by Zeki on 2023/2/16
 */
public class SignHelper {

    private static String SALT = "pacdsp2023";

    /**
     * 对 target 进行摘要处理
     *
     * @param source 源数据
     * @param assist 辅助数据（增加安全性）
     * @return 16位摘要指纹
     */
    public static String digest(String source, String assist) {
        if (StrUtil.hasBlank(source, assist)) {
            throw new ServiceException("target/assist is blank!");
        }
        return new MD5((source + assist + SALT).getBytes(StandardCharsets.UTF_8)).digestHex16(source);
    }

    /**
     * 原始URL 拼接 "&sign=xxx"
     */
    public static String urlAddSign(String url, String sign) {
        return url.concat(Constant.AND_MARK).concat(RequestKey.SIGN).concat(Constant.EQUAL_MARK).concat(sign);
    }
}

