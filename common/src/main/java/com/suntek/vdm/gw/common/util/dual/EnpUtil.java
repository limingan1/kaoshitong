package com.suntek.vdm.gw.common.util.dual;


import com.suntek.vdm.gw.common.util.security.EncryptionMachine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

@Service
@Slf4j
public class EnpUtil {
    @Value("${cas.security.file_path}")
    private String securityFilePath;

    public void enp(){
        Properties prop = new Properties();
        try{
            //读取属性文件a.properties
            String PATH = securityFilePath + "/config/config.properties";
            InputStream in = new BufferedInputStream(new FileInputStream(PATH));
            prop.load(in);     ///加载属性列表
            in.close();
            System.out.println("read enp success");
            String oldIv = prop.getProperty("iv");
            if(oldIv != null){
                return;
            }

            ///保存属性到b.properties文件
            FileOutputStream oFile = new FileOutputStream(PATH, false);//true表示追加打开
            String iv = UUID.randomUUID().toString();
            String enp = EncryptionMachine.encrypt(prop.getProperty("enp").trim() ,EncryptionMachine.certWorkFileNameKey,EncryptionMachine.MD5_16(iv));
            prop.setProperty("enp",enp);
            prop.setProperty("iv",iv);
            prop.store(oFile, "");
            oFile.close();
            log.info("write enp success");
        }
        catch(Exception e){
            log.error("error message: {}", e.getMessage());
            log.error("error Stack: {}", e.getStackTrace());
        }
    }
}
