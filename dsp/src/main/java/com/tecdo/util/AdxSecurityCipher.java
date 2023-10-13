package com.tecdo.util;

import cn.hutool.core.codec.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

/**
 * vivo提供的AES加解密工具类
 */
public class AdxSecurityCipher {
    private static final String CIPHER_MODE = "AES/CBC/PKCS5Padding";
    private static byte[] ivBytes = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
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

    public AdxSecurityCipher() {
    }

    public static String encryptString(String input, byte[] keyBytes) {
        try {
            byte[] content = input.getBytes("UTF-8");
            byte[] result = encrypt(content, keyBytes);
            return Base64.encode(result);
        } catch (Exception var4) {
            throw new RuntimeException(var4);
        }
    }

    public static String decryptString(String input, byte[] keyBytes) {
        try {
            byte[] content = Base64.decode(input);
            byte[] result = decrypt(content, keyBytes);
            return new String(result, "utf-8");
        } catch (Exception var4) {
            throw new RuntimeException(var4);
        }
    }

    public static byte[] encrypt(byte[] content, byte[] keyBytes) throws Exception {
        return docrypt(content, keyBytes, 1);
    }

    public static byte[] decrypt(byte[] content, byte[] keyBytes) throws Exception {
        return docrypt(content, keyBytes, 2);
    }

    public static byte[] docrypt(byte[] content, byte[] keyBytes, int mode) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec iv = new IvParameterSpec(ivBytes);
        ((Cipher)cipherThreadLocal.get()).init(mode, keySpec, iv);
        byte[] result = ((Cipher)cipherThreadLocal.get()).doFinal(content);
        return result;
    }

    public static String generateKey(int aesLen) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(aesLen, new SecureRandom());
        return Base64.encode(kgen.generateKey().getEncoded());
    }
}