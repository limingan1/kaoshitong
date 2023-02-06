package com.suntek.vdm.gw.api.controller.conf;

import com.suntek.vdm.gw.conf.service.ForwardService;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@Slf4j
public class ForwardController {

    @Autowired
    private ForwardService forwardService;
    @Autowired
    private HttpServletResponse response;


    @RequestMapping("conf-portal/**")
    public ResponseEntity<String> forwardAll() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        try {
            ResponseEntity<String> res = forwardService.forwardToSmc(request);
            return new ResponseEntity<>(res.getBody(), res.getStatusCode());
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

}
