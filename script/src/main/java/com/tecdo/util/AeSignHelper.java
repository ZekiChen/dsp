package com.tecdo.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Zeki on 2023/4/4
 */
public class AeSignHelper {
    private static final String SIGN_API_NAME = "/general";
    private static final String SIGN_CHARSET_UTF8 = "UTF-8";
    private static final String SIGN_METHOD_SHA256 = "HmacSHA256";
    private static final int SIGN_OXFF = 0xFF;
    // put your channel token here
    private static final String SIGN_APP_SECRET = "4sxwprGJArHhUuRmu9rKyh8YXVgYLyv9KFjPpJyI3i1NkrGkUCLHQfY5ANUvKHUG";

    // example code to generate sign
    public static String getSign(String adid, String channel, Long timestamp, List<String> campaignIds) {
        try {
            campaignIds.sort(String::compareTo);
            return buildSign(adid, campaignIds, channel, timestamp);
        } catch (Exception ex) {
            // ignore
            return null;
        }
    }

    private static String buildSign(String adid, List<String> campaignIds, String channel, Long timestamp)
            throws IOException {
        return signApiRequest(adid, campaignIds, channel, timestamp, SIGN_APP_SECRET);
    }

    private static String signApiRequest(String adid, List<String> campaignIds, String channel, Long timestamp, String appSecret) throws IOException {
        Map<String, String> signParams = new HashMap<>();
        signParams.put("adid", adid);
        signParams.put("campaignIds", String.format("[%s]", String.join(",", campaignIds)));
        signParams.put("channel", channel);
        signParams.put("timestamp", String.valueOf(timestamp));

        String[] keys = signParams.keySet().toArray(new String[0]);
        Arrays.sort(keys);
        StringBuilder query = new StringBuilder();
        query.append(SIGN_API_NAME);
        for (String key : keys) {
            String value = signParams.get(key);
            query.append(key).append(value);
        }
        byte[] bytes = encryptHMACsha256(query.toString(), appSecret);
        return byte2hex(bytes);
    }

    private static byte[] encryptHMACsha256(String data, String secret) throws IOException {
        byte[] bytes;
        try {
            SecretKey secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), SIGN_METHOD_SHA256);
            Mac mac = Mac.getInstance(secretKey.getAlgorithm());
            mac.init(secretKey);
            bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException gse) {
            throw new IOException(gse.toString());
        }
        return bytes;
    }

    private static String byte2hex(byte[] bytes) {
        StringBuilder sign = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & SIGN_OXFF);
            if (hex.length() == 1) {
                sign.append("0");
            }
            sign.append(hex.toUpperCase());
        }
        return sign.toString();
    }
}
