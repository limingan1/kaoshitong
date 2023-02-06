package com.suntek.vdm.gw.common.util.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Cipher;
import java.io.*;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.JDBCType;
import java.util.Arrays;

@Slf4j
public class EncryptionMachine {
    //key长度
    public static final int KEY_LENGHT = 32;

    //IV长度
    public static final int IV_LENGHT = 16;

    // component2
    public static final byte[] component2 = {-67, -91, 43, -118, -15, -98, 117, -3, 100, 56, -60, 15, -31, -20, -11,
            103, -25, 20, -123, -80, 69, 25, -94, -45, 23, 43, -13, -114, -21, -10, 47, 10, 24, -102, 38, -43, -107,
            -31, 13, -67, -69, 33, 107, -8, 85, -65, 99, -36, -52, 3, -30, -56, 15, 11, 69, -43, -53, 106, -4, -77,
            0, -113, -78, -75, -22, 26, 86, 37, -56, -6, 4, 0, 78, 118, 5, 96, -58, -16, 103, -78, 66, 37, -10, 102,
            85, -86, 107, -103, 62, 28, -36, -25, 83, -39, 75, -47, -80, -110, 22, -93, 13, 59, 123, -95, 23, -101,
            102, -32, 107, 41, -45, 123, 19, -120, -68, 107, -84, 113, 43, -12, 28, -37, 20, -80, -107, 101, -125, -42};

    // key大小
    public static final int mapSize = 5830;

    public static final int mapDateSize = 6346;


    // 根密钥向量
    public static byte[] fileNameKey = null;

    // 根密钥向量
    public static byte[] fileNameIv = null;

    // 根密钥
    public static String fileName;
    // 工作密文文件
    public static String workFileName;
    //证书工作密文文件
    public static String certWorkFileName;
    //HA工作密文文件
    public static String haFileName = null;
    public static String casFileName = null;

    // 工作明文向量
    public static byte[] workFileNameIv = null;

    // 工作明文密钥
    public static byte[] workFileNameKey = null;

    public static byte[] privateWorkFileNameIv = null;
    public static byte[] privateWorkFileNameKey = null;

    // 工作明文向量
    public static byte[] certWorkFileNameIv = null;

    // 工作明文密钥
    public static byte[] certWorkFileNameKey = null;

    // 工作明文向量
    public static byte[] haFileNameIv = null;

    // 工作明文密钥
    public static byte[] haFileNameKey = null;


    private static String enType = "512";

    //redis密码文件路径
    public static String redisFileName;



    /**
     * 加密文件路径
     */
    private static String securityFilepath;

    public static String getEnType() {
        return enType;
    }

    public static void init(String path, String dbType) {
        try {
            securityFilepath=path;
            redisFileName = securityFilepath + "/redis";
            fileName = securityFilepath + "/vdc.bmp";
            workFileName = securityFilepath + "/vdm-server";
            certWorkFileName = securityFilepath + "/vdc-cert";
            haFileName = securityFilepath + "/vdc-ha";
            casFileName = securityFilepath + "/cas";

            // 根密钥
            fileNameKey = genSKey(fileName, "512");
            createworkKey(fileNameKey, fileNameIv, workFileName);
            // 创建证书工作密钥和向量
            getworkKey(certWorkFileName, fileNameKey, fileNameIv);
            // 创建工作密钥和向量
            getworkKey(workFileName, fileNameKey, fileNameIv);
            // 创建工作密钥和向量
            if(haFileName != null){
                getworkKey(haFileName, fileNameKey, fileNameIv);
            }

        } catch (Exception e) {
            log.error("exception",e);
        }
        if("sqlserver".equals(dbType)){
            String workKeyCiphertext = readFile(casFileName);
            if(workKeyCiphertext == null){
                //需要创建
                try {
                    createworkKey(workFileNameKey,workFileNameIv, casFileName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                try {
                    getworkKey(casFileName, workFileNameKey, workFileNameIv);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取工作密钥和向量
     *
     * @param fileName 文件路径
     * @param rootKey  根密钥
     * @param rootiv   根向量
     * @throws Exception
     */
    public static void getworkKey(String fileName, byte[] rootKey, byte[] rootiv) throws Exception {
        String workKeyCiphertext = readFile(fileName);
        if (workKeyCiphertext == null || "".equals(workKeyCiphertext)) {
            throw new Exception(" workKey read error");
        }
        String workKey = decrypt(workKeyCiphertext, rootKey, rootiv);
        byte[] bytes = hexToBytes(workKey);
        // 前32位为工作明文密钥， 后16位为工作明文向量
        getWorkKeyAndIv(bytes, fileName);
    }


    /**
     * 加密，默认使用工作明文密钥
     *
     * @param sSrc   明文
     * @param enType 加密方式
     * @param iv     加密所需向量
     * @return 密文
     * @throws Exception
     */
    public static String encrypt(String sSrc, String enType, byte[] iv) throws Exception {
        if (StringUtils.isEmpty(enType)) {
            enType = getEnType();
        }
        byte[] bs_iv;
        byte[] bs_key;
        if ("256".equals(enType)) {
            bs_key = Arrays.copyOfRange(workFileNameKey, 0, 24);
            ;
        } else {
            bs_key = workFileNameKey;
        }
        if (iv == null || iv.length == 0) {
            byte[][] keyAndIV = createKeyAndIv(bs_key);
            bs_key = keyAndIV[0];
            bs_iv = keyAndIV[1];
        } else {
            bs_iv = iv;
        }
        return AesCBC.getInstance().encrypt(sSrc, "utf-8", bs_key, bs_iv);
    }

    /**
     * 指定key加密
     *
     * @param sSrc   明文
     * @param bs_key key
     * @param iv     向量
     * @return 密文
     * @throws Exception
     */
    public static String encrypt(String sSrc, byte[] bs_key, byte[] iv) throws Exception {
        byte[] bs_iv;
        if (iv == null || iv.length == 0) {
            byte[][] keyAndIV = createKeyAndIv(bs_key);
            bs_key = keyAndIV[0];
            bs_iv = keyAndIV[1];
        } else {
            bs_iv = iv;
        }
        return AesCBC.getInstance().encrypt(sSrc, "utf-8", bs_key, bs_iv);
    }

    /**
     * 指定key解密
     *
     * @param sSrc   密文
     * @param bs_key key
     * @param iv     向量
     * @return 明文
     * @throws Exception
     */
    public static String decrypt(String sSrc, byte[] bs_key, byte[] iv) throws Exception {
        byte[] bs_iv;
        if (iv == null || iv.length == 0) {
            byte[][] keyAndIV = createKeyAndIv(bs_key);
            bs_key = keyAndIV[0];
            bs_iv = keyAndIV[1];
        } else {
            bs_iv = iv;
        }
        return AesCBC.getInstance().decrypt(sSrc, "utf-8", bs_key, bs_iv);
    }

    /**
     * 解密，默认使用工作明文密钥
     *
     * @param sSrc   密文
     * @param enType 解密方式
     * @param iv     解密所需向量
     * @return 明文
     * @throws Exception
     */
    public static String decrypt(String sSrc, String enType, byte[] iv) throws Exception {
        if (StringUtils.isEmpty(enType)) {
            enType = getEnType();
        }
        byte[] bs_iv;
        byte[] bs_key;
        if ("256".equals(enType)) {
            bs_key = Arrays.copyOfRange(workFileNameKey, 0, 24);
            ;
        } else {
            bs_key = workFileNameKey;
        }
        if (iv == null || iv.length == 0) {
            byte[][] keyAndIV = createKeyAndIv(bs_key);
            bs_key = keyAndIV[0];
            bs_iv = keyAndIV[1];
        } else {
            bs_iv = iv;
        }
        return AesCBC.getInstance().decrypt(sSrc, "utf-8", bs_key, bs_iv);
    }


    /**
     * 生成密钥
     *
     * @param fileName      对应文件路径
     * @param encryptMethod 密钥加密方式
     * @return
     * @throws Exception
     */
    public static byte[] genSKey(String fileName, String encryptMethod) throws Exception {
        File file = new File(fileName);
        if (!file.exists() || file.length() < mapSize) {
            throw new Exception(fileName + " not exists");
        }
        byte[] skey;
        int COMPONENT_SIZE = 128;
        byte[] version = new byte[4];
        byte[] component1 = new byte[COMPONENT_SIZE];
        byte[] component3 = new byte[COMPONENT_SIZE];
        byte[] salt = new byte[COMPONENT_SIZE];
        byte[] ivs = new byte[COMPONENT_SIZE];
        boolean tag = true;
        try {
            if (file.length() != 0) {
                if (file.length() >= mapDateSize) {
                    InputStream in = new FileInputStream(file);
                    in.skip(mapSize);
                    in.read(version);
                    in.read(component1);
                    in.read(component3);
                    in.read(salt);
                    in.read(ivs);
                    tag = false;
                }
            }
            if (tag) {
                version = new byte[]{0, 0, 0, 1};
                byte[] bytes = new byte[mapDateSize - mapSize - 4];
                SecureRandom.getInstanceStrong().nextBytes(bytes);
                OutputStream os = new BufferedOutputStream(new FileOutputStream(file, true));
                os.write(version);
                os.write(bytes);
                os.flush();
                os.close();
                component1 = Arrays.copyOfRange(bytes, 0, COMPONENT_SIZE);
                component3 = Arrays.copyOfRange(bytes, COMPONENT_SIZE, COMPONENT_SIZE * 2);
                salt = Arrays.copyOfRange(bytes, COMPONENT_SIZE * 2, COMPONENT_SIZE * 3);
                ivs = Arrays.copyOfRange(bytes, COMPONENT_SIZE * 3, COMPONENT_SIZE * 4);
            }
        } catch (Exception e) {
            log.error("exception",e);
        }
        if (EncryptionMachine.fileName.equals(fileName)) {
            fileNameIv = new byte[IV_LENGHT];
            for (int i = 0; i < 16; i++) {
                fileNameIv[i] = ivs[i];
            }
        }
        ByteBuffer component = ByteBuffer.allocate(COMPONENT_SIZE);
        byte[] array = component.array();
        for (int i = 0; i < COMPONENT_SIZE; i++) {
            array[i] = (byte) (component1[i] ^ component2[i]);
            array[i] = (byte) (array[i] ^ component3[i]);
        }
        String s_component = bytesToHex(array);
        Pbkdf2Sha pbkdf2Sha;
        if ("256".equals(encryptMethod)) {
            pbkdf2Sha = new Pbkdf2Sha("PBKDF2&SHA256", 256);
        } else {
            pbkdf2Sha = new Pbkdf2Sha("PBKDF2&SHA512", 256);
        }
        skey = pbkdf2Sha.getEncodedHash(s_component, salt, Pbkdf2Sha.DEFAULT_ITERATIONS);
        return skey;
    }

    /**
     * byte数组 转 16进制字符串
     *
     * @param bytes byte数组
     * @return 16进制字符串
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() < 2) {
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * 16进制字符串 转 byte数组
     *
     * @param s 16进制字符串
     * @return byte数组
     */
    public static byte[] hexToBytes(String s) {
        byte[] bytes = new byte[s.length() / 2];
        for (int i = 0; i < s.length(); i += 2) {
            bytes[i / 2] = (byte) Integer.parseInt(s.substring(i, i + 2), 16);
        }
        return bytes;
    }

    /**
     * MD5加密16位
     *
     * @param str 明文
     * @return
     */
    public static byte[] MD5_16(String str) {
        byte b[] = new byte[16];
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            b = md.digest();
        } catch (NoSuchAlgorithmException e) {
            log.error("Could NOT encrypt MD5");
        }
        return b;
    }

    public static byte[][] createKeyAndIv(byte[] bs_key) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        Cipher aesCBC = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[][] keyAndIV = EVP_BytesToKey(KEY_LENGHT, aesCBC.getBlockSize(), md5, null, bs_key, 1);
        return keyAndIV;
    }

    public static byte[][] EVP_BytesToKey(int key_len, int iv_len, MessageDigest md, byte[] salt, byte[] data, int count) {
        byte[][] both = new byte[2][];
        byte[] key = new byte[key_len];
        int key_ix = 0;
        byte[] iv = new byte[iv_len];
        int iv_ix = 0;
        both[0] = key;
        both[1] = iv;
        byte[] md_buf = null;
        int nkey = key_len;
        int niv = iv_len;
        int i = 0;
        if (data == null) {
            return both;
        }
        int addmd = 0;
        for (; ; ) {
            md.reset();
            if (addmd++ > 0) {
                md.update(md_buf);
            }
            md.update(data);
            if (null != salt) {
                md.update(salt, 0, 8);
            }
            md_buf = md.digest();
            for (i = 1; i < count; i++) {
                md.reset();
                md.update(md_buf);
                md_buf = md.digest();
            }
            i = 0;
            if (nkey > 0) {
                for (; ; ) {
                    if (nkey == 0) {
                        break;
                    }
                    if (i == md_buf.length) {
                        break;
                    }
                    key[key_ix++] = md_buf[i];
                    nkey--;
                    i++;
                }
            }
            if (niv > 0 && i != md_buf.length) {
                for (; ; ) {
                    if (niv == 0) {
                        break;
                    }
                    if (i == md_buf.length) {
                        break;
                    }
                    iv[iv_ix++] = md_buf[i];
                    niv--;
                    i++;
                }
            }
            if (nkey == 0 && niv == 0) {
                break;
            }
        }
        for (i = 0; i < md_buf.length; i++) {
            md_buf[i] = 0;
        }
        return both;
    }


    /**
     * 获取对应工作明文密钥和向量
     *
     * @param bytes    包含key和iv
     * @param fileName 工作文件
     */
    private static void getWorkKeyAndIv(byte[] bytes, String fileName) {
        if (workFileName.equals(fileName)) {
            workFileNameKey = Arrays.copyOfRange(bytes, 0, 32);
            workFileNameIv = Arrays.copyOfRange(bytes, 32, 48);
        }
        if (certWorkFileName.equals(fileName)) {
            certWorkFileNameKey = Arrays.copyOfRange(bytes, 0, 32);
            certWorkFileNameIv = Arrays.copyOfRange(bytes, 32, 48);
        }
        if (haFileName.equals(fileName)) {
            haFileNameKey = Arrays.copyOfRange(bytes, 0, 32);
            haFileNameIv = Arrays.copyOfRange(bytes, 32, 48);
        }
        if(fileName.equals(casFileName)){
            privateWorkFileNameIv = workFileNameIv;
            privateWorkFileNameKey = workFileNameKey;
            workFileNameKey = Arrays.copyOfRange(bytes, 0, 32);
            workFileNameIv = Arrays.copyOfRange(bytes, 32, 48);
        }
    }


    /**
     * 获取工作密文文件内容
     *
     * @param fileName 文件名
     * @return
     */
    public static String readFile(String fileName) {
        File file = new File(fileName);
        FileInputStream fi = null;
        BufferedReader br = null;
        String workKeyCiphertext = null;
        try {
            if (file.exists()) {
                fi = new FileInputStream(file);
                br = new BufferedReader(new InputStreamReader(fi));
                workKeyCiphertext = br.readLine();
            }
        } catch (Exception e) {
            log.error("exception",e);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (fi != null) {
                    fi.close();
                }
            } catch (IOException e) {
            }
        }
        return workKeyCiphertext;
    }

    /**
     * @param rootKey  根密钥
     * @param rootIv   根向量
     * @param fileName 工作文件路径
     * @throws Exception
     */
    public static void createworkKey(byte[] rootKey, byte[] rootIv, String fileName) throws Exception {
        // 判断工作密文文件是否存在
        File file = new File(fileName);
        // 不存在
        if (!file.exists()) {
            // 随机生成工作明文，加密配置文件，并使用根密钥加密存储
            byte[] bytes = new byte[48];
            SecureRandom.getInstanceStrong().nextBytes(bytes);
            // 前32位为工作明文密钥， 后16位为工作明文向量
            byte[] cpRootKey = rootKey.clone();
            byte[] cpRootIv = rootIv.clone();

            getWorkKeyAndIv(bytes, fileName);
            // 工作明文密钥
            byte[] workKeyToBytes = Arrays.copyOfRange(bytes, 0, 32);
            // 工作明文向量
            byte[] workIvs = Arrays.copyOfRange(bytes, 32, 48);
            // 工作明文密文 ，前32位位工作明文， 后 16位为向量
            String workKey = bytesToHex(workKeyToBytes) + bytesToHex(workIvs);
            String workKeyCiphertext = encrypt(workKey, cpRootKey, cpRootIv);
            // 将生成的工作明文密文写入文件
            createKeyFile(fileName, workKeyCiphertext);
        }
    }

    /**
     * 生成根密钥 或 工作密文写入
     *
     * @param fileName 文件名
     * @param pwd      工作密文需传入
     */
    public static byte[] createKeyFile(String fileName, String pwd) throws Exception {
        File file = new File(fileName);
        FileOutputStream fo = null;
        BufferedWriter bw = null;
        byte[] key = null;
        try {
            if (pwd == null || "".equals(pwd)) {
                // 生成根密钥文件 ,且返回根密钥key
                key = genSKey(fileName, "512");
            }
            // 写入工作密文
            else {
                // 判断文件是否存在
                if (!file.exists()) {
                    file.createNewFile();
                }
                fo = new FileOutputStream(file, false);
                bw = new BufferedWriter(new OutputStreamWriter(fo));
                bw.write(pwd);
                bw.flush();
            }
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
                if (fo != null) {
                    fo.close();
                }
            } catch (IOException e) {
            }
        }
        return key;
    }
}
