package com.suntek.vdm.gw.common.util.security;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

/**
 * PBKDF2_SHA256加密验证算法
 * */
public class Pbkdf2Sha {
    private static final Logger logger = LoggerFactory.getLogger(Pbkdf2Sha.class);

    /**
     * 盐的长度
     */
    public static final int SALT_BYTE_SIZE = 16;

    /**
     * 生成密文的长度(例：64 * 4，密文长度为64)
     */
    public int HASH_BIT_SIZE;

    /**
     * 迭代次数(默认迭代次数为 10000)
     */
    public static final Integer DEFAULT_ITERATIONS = 100000;

    /**
     * 算法名称
     */
    private String algorithm = "PBKDF2&SHA256";

    public Pbkdf2Sha(String algorithm, int HASH_BIT_SIZE) {
        this.algorithm = algorithm;
        this.HASH_BIT_SIZE = HASH_BIT_SIZE;
    }

    /**
     * 获取密文
     * @param password   密码明文
     * @param salt       加盐
     * @param iterations 迭代次数
     * @return
     */
    public byte[] getEncodedHash(String password, byte[] salt, int iterations) {
        // Returns only the last part of whole encoded password
        SecretKeyFactory keyFactory = null;
        if ("PBKDF2&SHA256".equals(algorithm)) {
            try {
                keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            } catch (NoSuchAlgorithmException e) {
                logger.error("Could NOT retrieve PBKDF2WithHmacSHA256 algorithm", e);
            }
        }
        else {
            try {
                keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            } catch (NoSuchAlgorithmException e) {
                logger.error("Could NOT retrieve PBKDF2WithHmacSHA512 algorithm", e);
            }
        }
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, iterations, HASH_BIT_SIZE);
        SecretKey secret = null;
        try {
            secret = keyFactory.generateSecret(keySpec);
        } catch (InvalidKeySpecException e) {
            logger.error("Could NOT generate secret key", e);
        }
        //使用十六进制密文
        return secret.getEncoded();
    }
}
