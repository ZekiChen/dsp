package com.tecdo.util;

import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import cn.hutool.core.codec.Base64;

/**
 * 对发送给pac的点击追踪链接进行加密
 */
public class ClickUrlSecurityCipher {
    private static final String CIPHER_MODE = "AES/CBC/PKCS5Padding";
    private static ThreadLocal<Cipher> cipherThreadLocal = new ThreadLocal<Cipher>() {
        protected Cipher initialValue() {
            try {
                Cipher cipher = Cipher.getInstance(CIPHER_MODE);
                return cipher;
            } catch (Exception var2) {
                throw new RuntimeException(var2);
            }
        }
    };

    public ClickUrlSecurityCipher() {
    }

    public static String encryptString(String input, String key, String iv) {
        return encryptString(input,
                             key.getBytes(StandardCharsets.UTF_8),
                             iv.getBytes(StandardCharsets.UTF_8));
    }

    public static String encryptString(String input, byte[] keyBytes, byte[] ivBytes) {
        try {
            byte[] content = input.getBytes(StandardCharsets.UTF_8);
            byte[] result = encrypt(content, keyBytes, ivBytes);
            return Base64.encode(result);
        } catch (Exception var4) {
            throw new RuntimeException(var4);
        }
    }

    public static String decryptString(String input, String key, String iv) {
        return decryptString(input,
                             key.getBytes(StandardCharsets.UTF_8),
                             iv.getBytes(StandardCharsets.UTF_8));
    }

    public static String decryptString(String input, byte[] keyBytes, byte[] ivBytes) {
        try {
            byte[] content = Base64.decode(input);
            byte[] result = decrypt(content, keyBytes, ivBytes);
            return new String(result, StandardCharsets.UTF_8);
        } catch (Exception var4) {
            throw new RuntimeException(var4);
        }
    }

    private static byte[] encrypt(byte[] content, byte[] keyBytes, byte[] ivBytes) throws Exception {
        return docrypt(content, keyBytes, ivBytes, 1);
    }

    private static byte[] decrypt(byte[] content, byte[] keyBytes, byte[] ivBytes) throws Exception {
        return docrypt(content, keyBytes, ivBytes, 2);
    }

    private static byte[] docrypt(byte[] content, byte[] keyBytes, byte[] ivBytes, int mode) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec iv = new IvParameterSpec(ivBytes);
        ((Cipher)cipherThreadLocal.get()).init(mode, keySpec, iv);
        byte[] result = ((Cipher)cipherThreadLocal.get()).doFinal(content);
        return result;
    }

}