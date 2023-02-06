package com.suntek.vdm.gw.license.service.impl;

import com.huawei.vdmserver.common.dto.response.GlobalRestResponse;
import com.huawei.vdmserver.license.service.LicenseCallCService;
import com.suntek.vdm.gw.license.service.LicenseManagerService;
import com.suntek.vdm.gw.license.service.WindowLicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;



@Service
@ConditionalOnProperty(name = "cas.database.db_type",havingValue = "sqlserver")
public class WindowLicenseServiceImpl implements WindowLicenseService {

    @Autowired(required = false)
    private LicenseCallCService licenseCallCService;
    @Autowired
    private LicenseManagerService licenseManagerService;

    @Override
    public GlobalRestResponse<?> getEsnCode() {
        return licenseCallCService.getEsnCode();
    }

    @Override
    public GlobalRestResponse<?> importLicenseString(MultipartFile file, boolean isDual) {
        GlobalRestResponse<?> globalRestResponse = null;
        try {
            globalRestResponse = licenseCallCService.httpImportLicense(file, isDual);
            if (globalRestResponse.getCode() == 0) {
                licenseManagerService.change();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return globalRestResponse;
    }

    @Override
    public GlobalRestResponse<?> allocResource(String resourceName) {
        return licenseCallCService.allocResource(resourceName);
    }

    @Override
    public GlobalRestResponse<?> deallocResource(String resourceName) {
        return licenseCallCService.deallocResource(resourceName);
    }

    @Override
    public GlobalRestResponse<?> getAllResource() {
        return licenseCallCService.getAllResource();
    }

    @Override
    public GlobalRestResponse<?> getOneResource(String category) {
        return licenseCallCService.getOneResource(category);
    }

    @Override
    public GlobalRestResponse<?> getRemainingDay() {
        return licenseCallCService.getRemainingDay();
    }

    @Override
    public GlobalRestResponse<?> getExpiredDate() {
        return licenseCallCService.getExpiredDate();
    }

    @Override
    public GlobalRestResponse<?> getVdcLicenseStatus() {
        return licenseCallCService.getVdcLicenseStatus();
    }

    @Override
    public GlobalRestResponse<?> revokeLicense() {
        return licenseCallCService.licGeneralRevoke();
    }

    @Override
    public GlobalRestResponse<?> getRvkTicketAndRvkTime() {
        return licenseCallCService.getRvkTicketAndRvkTime();
    }
}
