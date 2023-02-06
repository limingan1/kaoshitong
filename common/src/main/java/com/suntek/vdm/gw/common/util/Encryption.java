package com.suntek.vdm.gw.common.util;

import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
@Slf4j
public class Encryption {

    /**
     * BASE64解密
     *
     * @throws Exception
     */
    public static String decryptBase64(String encoded) {
        try { //Base64 解密
            byte[] decoded = Base64.getDecoder().decode(encoded);
            String decodeStr = new String(decoded);
            return decodeStr;
        } catch (Exception e) {
            log.error("exception",e);
            return null;
        }
    }

    /**
     * BASE64加密
     */
    public static String encryptBase64(String str) {
        try {
            byte[] bytes = str.getBytes();
            String encoded = Base64.getEncoder().encodeToString(bytes);
            return encoded;
        } catch (Exception e) {
            log.error("exception",e);
            return null;
        }
    }


    /**
     * 利用java原生的类实现SHA256加密
     *
     * @param str 参数拼接的字符串
     * @return
     */
    public static String getSha256(String str) {
        MessageDigest messageDigest;
        String encodeStr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes("UTF-8"));
            encodeStr = byte2Hex(messageDigest.digest());
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodeStr;
    }

    /**
     * 将byte转为16进制
     *
     * @param bytes
     * @return
     */
    private static String byte2Hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        String temp = null;
        for (byte aByte : bytes) {
            temp = Integer.toHexString(aByte & 0xFF);
            if (temp.length() == 1) {
                // 1得到一位的进行补0操作
                sb.append("0");
            }
            sb.append(temp);
        }
        return sb.toString();
    }


    public static String smcSignature(String timestamp, String userName, String ticket, String token) {
        String signature = "timestamp=%s|username=%s|ticket=%s|token=%s";
        signature = String.format(signature, timestamp, userName, ticket, token);
        signature = Encryption.getSha256(signature);
        return signature;
    }


}
