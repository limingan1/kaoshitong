package com.suntek.vdm.gw.license.service.impl;

import com.alibaba.fastjson.JSON;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.service.LicenseManageService;
import com.suntek.vdm.gw.common.util.CommonHelper;
import com.suntek.vdm.gw.core.entity.NodeData;
import com.suntek.vdm.gw.common.enums.CoreApiUrl;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.core.service.NodeDataService;
import com.suntek.vdm.gw.core.service.RemoteGwService;
import com.suntek.vdm.gw.license.pojo.LicenseBaseResponse;
import com.suntek.vdm.gw.license.pojo.LicenseStatus;
import com.suntek.vdm.gw.license.service.LicenseManagerApiService;
import com.suntek.vdm.gw.license.service.LicenseManagerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

@Service
@Slf4j
public class LicenseManagerServiceImpl implements LicenseManagerService {
    @Autowired
    private LicenseManagerApiService licenseManagerApiService;
    @Autowired
    private NodeDataService nodeDataService;
    @Autowired
    private RemoteGwService remoteGwService;
    @Autowired
    private LicenseManageService licenseManageService;
    //资源类别 需要和协商名称目前没定
    private static String category = "cas";//cas
    private static String categoryForWeLink = "welink";
    private final static String[] categories = {category, categoryForWeLink};


    public void getLocalLicense() throws MyHttpException{
        for (String category : categories) {
            boolean isWelink = category.equals("welink");
            boolean hasLicense = false;
            LicenseBaseResponse<String> licenseBaseResponse = licenseManagerApiService.getOneResource(category);
            log.info("get license status: {}", licenseBaseResponse);
            if (licenseBaseResponse.getCode() == 0) {
                String value = licenseBaseResponse.getDataValue();
                if (!StringUtils.isEmpty(value)) {
                    //value 案例 total:1,used:0
                    String[] values = value.split(",");
                    int total = Integer.parseInt(values[0].split(":")[1]);
                    hasLicense = total == 1;
//                        int used = Integer.parseInt(values[1].split(":")[1]);
                }
            } else {
                if (licenseBaseResponse.getCode() == 10056) {
                    throw new MyHttpException(409, "LICENSE_NOT_INIT");
                }
            }
            if (isWelink) {
                licenseManageService.saveWelinkLicense(hasLicense);
            }else{
                licenseManageService.saveCascadeLicense(hasLicense);
            }
        }
    }

    @Override
    public void change() throws MyHttpException{
        //重新获取本级license
        getLocalLicense();
        getPreLicense();//获取上级license
        noticeNextNode();
    }

    private void getPreLicense() throws MyHttpException {
        NodeData top = nodeDataService.getTop();
        if (top != null) {
            //向上节点获取
            GwId gwId = top.toGwId();
            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            String body = "";
            try{
                body = remoteGwService.toByGwId(gwId).get(CoreApiUrl.GET_LICENSE.value(), headers).getBody();
            }catch (Exception e){
                e.printStackTrace();
            }

            LicenseStatus preLicenseStatus = JSON.parseObject(body, LicenseStatus.class);
            if (preLicenseStatus != null) {
                this.saveLicenseStatus(preLicenseStatus);
            }
        }
    }

    private void saveLicenseStatus(LicenseStatus licenseStatus) {
        licenseManageService.saveCascadeLicense(licenseStatus.getHasLicense() || licenseManageService.getCascadeLicense());
        licenseManageService.saveWelinkLicense(licenseStatus.getHasLicenseForWeLink() || licenseManageService.getWelinkLicense());
    }

    @Override
    public LicenseStatus getLicense() throws MyHttpException{
        return new LicenseStatus(licenseManageService.getCascadeLicense(), licenseManageService.getWelinkLicense());
    }

    private void noticeNextNode() throws MyHttpException{
        //获取本级的所有下级
        List<NodeData> low = nodeDataService.getLow();
        for (NodeData nodeData : low) {
            GwId gwId = nodeData.toGwId();
            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            remoteGwService.toByGwId(gwId).get(CoreApiUrl.CHANGE_LICENSE.value(), headers);
        }
    }

    ThreadLocal<Integer> retryTime = new ThreadLocal<>();
    @Override
    @Async
    public void initLicense() throws MyHttpException{
        log.info("start init license");
        try {
            if (retryTime.get() == null) {
                retryTime.set(0);
            }
            //获得当前节点license，获取不到则向上节点获取
            //先获取本级license
            getLocalLicense();
            getPreLicense();//向上节点获取
            noticeNextNode();
            retryTime.remove();
        }catch (MyHttpException e){
            Integer currentTime = retryTime.get();
            currentTime++;
            retryTime.set(currentTime);
            if (currentTime > 36) {
                e.printStackTrace();
                log.error("initLicense error,retry count:{}", retryTime.get());
                retryTime.remove();
                return;
            }
            CommonHelper.sleep(5000);
            log.info("retry initLicense,retry count:{}", retryTime.get());
            initLicense();
        }
    }
}
