package com.suntek.vdm.gw.conf;

import com.suntek.vdm.gw.common.util.CommonHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages="com.suntek.vdm.gw")
@EnableScheduling
@EnableAsync
@EnableCaching
@Slf4j
public class ConfApplication {
    public static void main(String[] args) {
        new ConfApplication().Standby();
        SpringApplication.run(ConfApplication.class, args);
    }

    public void Standby() {
        try{
            String dir = System.getProperty("user.dir");
            boolean b = CommonHelper.isStandbyCurrent(dir);
            // 备机时， 不启动服务， 服务相当于挂起
            while (b) {
                try {
                    log.info("===============Standby : Service pending==============");
                    synchronized (this) {
                        wait();
                    }
                } catch (Exception e) {
                    log.error("=============Standby : Service pending error============");
                }
            }
        }catch (Exception e){
           log.warn("user.dir not found");
        }
    }
}