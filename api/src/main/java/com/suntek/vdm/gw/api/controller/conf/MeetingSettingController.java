package com.suntek.vdm.gw.api.controller.conf;

import com.alibaba.fastjson.JSON;
import com.suntek.vdm.gw.conf.service.MeetingSettingService;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.smc.request.meeting.setting.ModifyMeetingSettingsRequest;
import com.suntek.vdm.gw.smc.response.meeting.setting.GetMeetingSettingsResponse;
import com.suntek.vdm.gw.smc.response.meeting.setting.ModifyMeetingSettingsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/conf-portal")
public class MeetingSettingController {

    @Autowired
    private MeetingSettingService meetingSettingService;


    @GetMapping("/conferences/settings")
    public ResponseEntity<String> getMeetingSettings(@RequestHeader("Token") String token) {
        try {
            GetMeetingSettingsResponse response = meetingSettingService.getMeetingSettings(token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PatchMapping("/conferences/settings")
    public ResponseEntity<String> modifyMeetingSettings(@RequestBody ModifyMeetingSettingsRequest request,
                                                        @RequestHeader("Token") String token) {
        try {
            ModifyMeetingSettingsResponse response = meetingSettingService.modifyMeetingSettings(request,token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }
}
