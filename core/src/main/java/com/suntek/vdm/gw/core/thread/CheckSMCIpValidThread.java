package com.suntek.vdm.gw.core.thread;

import com.suntek.vdm.gw.common.pojo.SMCDto;
import com.suntek.vdm.gw.common.util.CheckSMCIpAddress;
import com.suntek.vdm.gw.common.util.SystemConfiguration;
import com.suntek.vdm.gw.smc.service.impl.SmcHttpServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CheckSMCIpValidThread {
    @Autowired
    private CheckSmcIpTask checkSmcIpTask;


    public void startCheck(){
        checkSmcIpTask.startCheckIpValidService(new Runnable() {
            @Override
            public void run() {
                SMCDto smcDto = new SMCDto();
                smcDto.setAddress(SmcHttpServiceImpl.ip);
                smcDto.setDomain(SystemConfiguration.getSmcAddress());
                smcDto.setProtocol("https");
                log.info("=====START CheckIpValid:{}======",SystemConfiguration.getSmcAddress());
                int code = CheckSMCIpAddress.testIpAddress(smcDto);
                if(code != 401){//ip地址失效
                    //重新配置smc
                    log.info("=====SMC IP IS INVALID======");
                    SMCDto smcDto1 = CheckSMCIpAddress.checkSMCIpAddress(smcDto);
                    switch (smcDto1.getCode()){
                        case 401:
                            SmcHttpServiceImpl.ip = smcDto1.getAddress();
                            break;
                        default:
                            log.error("checkSmcAddress failed. code: {}", smcDto1.getCode());
                    }
                }
            }
        });
    }

    public void stopCheck(){
        checkSmcIpTask.stopCheckIpValidService();
    }

    public void restarrt(){
        stopCheck();
        startCheck();
    }
}
