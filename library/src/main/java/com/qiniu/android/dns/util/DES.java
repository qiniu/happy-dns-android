package com.qiniu.android.dns.util;

import java.nio.charset.Charset;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class DES {
    /**
     * Convert data to encrypted hex string
     * @param data data to encrypt
     * @param key encrypt key
     * @return hex string
     */
    public static String encrypt(String data, String key) {
        try {
            Cipher c = Cipher.getInstance("DES/ECB/PKCS5Padding");
            c.init(Cipher.ENCRYPT_MODE, SecretKeyFactory.getInstance("DES").generateSecret(new DESKeySpec(key.getBytes())));
            return Hex.encodeHexString(c.doFinal(data.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Convert encrypted hex string to UTF-8 string
     * @param data data to decrypt
     * @param key decrypt key
     * @return UTF-8 string
     */
    public static String decrypt(String data, String key) {
        try {
            Cipher c = Cipher.getInstance("DES/ECB/PKCS5Padding");
            c.init(Cipher.DECRYPT_MODE, SecretKeyFactory.getInstance("DES").generateSecret(new DESKeySpec(key.getBytes())));
            return new String(c.doFinal(Hex.decodeHex(data.toCharArray())), Charset.defaultCharset());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
