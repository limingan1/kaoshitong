package com.suntek.vdm.gw.api.listener;

import com.alibaba.fastjson.JSONObject;
import com.suntek.vdm.gw.common.constant.Constants;
import com.suntek.vdm.gw.common.pojo.CoreConfig;
import com.suntek.vdm.gw.common.pojo.WarningReportDto;
import com.suntek.vdm.gw.common.pojo.WarningType;
import com.suntek.vdm.gw.common.service.HttpService;
import com.suntek.vdm.gw.common.service.WarningReportService;
import com.suntek.vdm.gw.common.util.dual.EnpUtil;
import com.suntek.vdm.gw.conf.service.OtherService;
import com.suntek.vdm.gw.conf.service.internal.InternalLinkService;
import com.suntek.vdm.gw.core.cache.CommonCache;
import com.suntek.vdm.gw.core.entity.NodeData;
import com.suntek.vdm.gw.conf.dual.DualService;
import com.suntek.vdm.gw.core.service.NodeDataService;
import com.suntek.vdm.gw.core.service.StartService;
import com.suntek.vdm.gw.license.service.LicenseManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

@Component
@Slf4j
public class ApplicationRunnerImpl implements ApplicationRunner {
    @Value("${cas.database.dual_computer_enable}")
    private Boolean dual_computer_enable;

    @Autowired
    private StartService startService;
    @Autowired
    private InternalLinkService internalLinkService;
    @Autowired
    private LicenseManagerService licenseManagerService;
    @Value("${version}")
    private String version;
    @Autowired
    private NodeDataService nodeDataService;
    @Autowired
    private WarningReportService warningReportService;
    @Autowired
    @Qualifier("httpServiceImpl")
    private HttpService httpService;
    @Autowired
    private DualService dualService;
    @Autowired
    private EnpUtil enpUtil;
    @Autowired
    private OtherService otherService;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("{}START GW{}\n" +
                "   __ _           \n" +
                "  / _` | __ __ __ \n" +
                "  \\__, | \\ V  V / \n" +
                "  |___/   \\_/\\_/    \n" +
                "             {} ", CoreConfig.splitLine, CoreConfig.splitLine, version);

        log.info("{}startup complete by version:{}{}", CoreConfig.splitLine, version, CoreConfig.splitLine);
        try {
            log.info("{}check database{}", CoreConfig.splitLine, CoreConfig.splitLine);
            if (!startService.checkDataSource()) {
                return;
            }
            log.info("{}init database{}", CoreConfig.splitLine, CoreConfig.splitLine);
            startService.initDatabase();

            log.info("{}init smc config{}", CoreConfig.splitLine, CoreConfig.splitLine);
            startService.initSmcConfig();

            enpUtil.enp();
            if(dual_computer_enable != null && dual_computer_enable){
                log.info("{}init dual{}", CoreConfig.splitLine, CoreConfig.splitLine);
                dualService.start();
            }

            log.info("{}init node{}", CoreConfig.splitLine, CoreConfig.splitLine);
            startService.loadNodeByDatabase();

            log.info("{}init license{}", CoreConfig.splitLine, CoreConfig.splitLine);
            licenseManagerService.initLicense();
            log.info("{}init internal data{}", CoreConfig.splitLine, CoreConfig.splitLine);
            internalLinkService.start();

            checkLocalNodeConfig();
            otherService.getOrganizations(null);
        } catch (Exception e) {
            log.error("startup exception error:", e);
        }finally {
            //设置系统加载完成
            log.info("{}set load status{}",CoreConfig.splitLine, CoreConfig.splitLine);
            CommonCache.LOAD_STATUS = true;
        }
    }

    public void checkLocalNodeConfig(){
        String url = Constants.localAddress + ":" + Constants.port + Constants.prefix + "/smcconfig";
        try {
            String body = httpService.get(url, null, new LinkedMultiValueMap<>()).getBody();
            log.info("query start cascade server:{}", body);
            JSONObject res = JSONObject.parseObject(body);
            if (res.getIntValue("code") == 200) {
                JSONObject data = res.getJSONObject("data");
                if (data != null) {
                    boolean startCasServer = data.getBooleanValue("link");
                    if (startCasServer) {
                        NodeData nodeData = nodeDataService.getLocal();
                        if (nodeData == null) {
                            //发送告警
                            WarningReportDto warningReportDto = new WarningReportDto();
                            warningReportDto.setOmcSource("B");
                            warningReportDto.setOmcLevel(0);
                            warningReportDto.setOmcType(4);
                            warningReportDto.setOmcName("10000005;本级节点没有配置");
                            warningReportDto.setWarningType(WarningType.LOCAL_NODE_NOT_CONFIG);
                            warningReportService.addWarningReport(warningReportDto);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("checkLocalNodeConfig error:{}", e.getMessage());
        }
    }
}
