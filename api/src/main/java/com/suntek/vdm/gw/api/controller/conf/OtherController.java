package com.suntek.vdm.gw.api.controller.conf;


import com.alibaba.fastjson.JSON;
import com.huawei.vdmserver.common.dto.LicenseRequestDto;
import com.suntek.vdm.gw.common.pojo.request.FindAreaTreeConfigReq;
import com.suntek.vdm.gw.common.pojo.response.room.AreasResp;
import com.suntek.vdm.gw.conf.service.OtherService;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/conf-portal")
public class OtherController {
    @Autowired
    private OtherService otherService;

    @GetMapping("/tickets")
    public ResponseEntity<String> scheduleConferences(@RequestParam  String username,
                                                      @RequestHeader("Token") String token) {
        try {
            String response = otherService.getMeetingTickets(username,token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PostMapping("/conferences/license")
    public ResponseEntity<String> license(@RequestBody LicenseRequestDto licenseRequestDto,
                                          @RequestHeader("Token") String token) {
        try {
            String response = otherService.getLicense(licenseRequestDto,token);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @GetMapping("/configs/search/findareatreeconfig")
    public ResponseEntity<String> findareatreeconfig(@RequestHeader("Token") String token,
                                                     String configName) {
        try {
            FindAreaTreeConfigReq response = otherService.findareatreeconfig(configName, token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @GetMapping("/addressbook/areas")
    public ResponseEntity<String> areas(@RequestHeader("Token") String token){
        try {
            List<AreasResp> response = otherService.areas(token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

}
