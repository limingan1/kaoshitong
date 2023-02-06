package com.suntek.vdm.gw.api.controller.report;

import com.alibaba.fastjson.JSONObject;
import com.suntek.vdm.gw.common.pojo.WarningType;
import com.suntek.vdm.gw.common.service.WarningReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/warningreport")
public class WarningReportController {
    @Autowired
    private WarningReportService warningReportService;

    @DeleteMapping("/delete")
    public ResponseEntity<String> remote(@RequestBody JSONObject jsonObject) {
        WarningType warningType = jsonObject.getObject("warningType", WarningType.class);
        warningReportService.deleteWarningReport(warningType);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
