package com.suntek.vdm.gw.common.util.security;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.UnsupportedEncodingException;

/**
 * MD5加密，32位
 *
 * @author yfb
 *
 */
@Slf4j
public class Md5 {


    private static final String CHARSET_UTF8 = "UTF-8";

    public static String sign(String text) {
        return sign(text, CHARSET_UTF8);
    }

    /**
     * 签名字符串
     *
     * @param text 需要签名的字符串
     * @param charset 编码格式
     * @return
     */
    public static String sign(String text, String charset) {
        try {
            return DigestUtils.md5Hex(getBytes(text, charset));
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(),e);
        }
        return null;
    }

    /**
     * 获取字节数组
     *
     * @param content
     * @param charset
     * @return
     * @throws UnsupportedEncodingException
     */
    private static byte[] getBytes(String content, String charset) throws UnsupportedEncodingException {
        if (charset == null || "".equals(charset)) {
            return content.getBytes();
        }
        return content.getBytes(charset);
    }
}
