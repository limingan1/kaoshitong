package com.suntek.vdm.gw.api.controller.conf;

import com.alibaba.fastjson.JSON;
import com.suntek.vdm.gw.common.pojo.request.DualHostDto;
import com.suntek.vdm.gw.core.annotation.PassToken;
import com.suntek.vdm.gw.conf.dual.DualService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/conf-portal/dual")
@Slf4j
public class DualController {
    @Autowired
    private DualService dualService;

    @PassToken
    @PostMapping("/hostname")
    public ResponseEntity<String> subscribe(@RequestBody DualHostDto dualHostDto) {

        DualHostDto dualHostResp = dualService.dualHostReady(dualHostDto);
        String key = null;
        if(dualHostResp.getKey() != null){
            key = dualHostResp.getKey();
            dualHostResp.setKey(null);
            MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
            headers.add("casuuid", key);
            return new ResponseEntity<>(JSON.toJSONString(dualHostResp),headers, HttpStatus.OK);
        }
        return new ResponseEntity<>(JSON.toJSONString(dualHostResp), HttpStatus.OK);
    }

}
