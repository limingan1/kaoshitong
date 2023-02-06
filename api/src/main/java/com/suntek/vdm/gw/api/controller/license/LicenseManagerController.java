package com.suntek.vdm.gw.api.controller.license;


import com.alibaba.fastjson.JSON;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.core.annotation.InternInterface;
import com.suntek.vdm.gw.core.annotation.PassLicense;
import com.suntek.vdm.gw.license.pojo.LicenseStatus;
import com.suntek.vdm.gw.license.service.LicenseManagerApiService;
import com.suntek.vdm.gw.license.service.LicenseManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/license")
public class LicenseManagerController {

    @Autowired
    private LicenseManagerService licenseManagerService;

    @Autowired
    LicenseManagerApiService licenseManagerApiService;

    @GetMapping("/change")
    @PassLicense
    @InternInterface
    public ResponseEntity<String> change() {
        try {
            licenseManagerService.change();
            return new ResponseEntity<>(JSON.toJSONString("ok"), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @GetMapping("/getLicense")
    public ResponseEntity<String> getLicense() {
        try {
            LicenseStatus status = licenseManagerService.getLicense();
            return new ResponseEntity<>(JSON.toJSONString(status), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }
}
