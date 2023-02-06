package com.suntek.vdm.gw.api.controller.conf;


import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.suntek.vdm.gw.conf.api.request.AddTemplatesRequestEx;
import com.suntek.vdm.gw.conf.api.request.ModifyTemplatesRequestEx;
import com.suntek.vdm.gw.conf.api.response.AddTemplatesResponseEx;
import com.suntek.vdm.gw.conf.api.response.GetTemplatesResponseEx;
import com.suntek.vdm.gw.conf.api.response.ModifyTemplatesResponseEx;
import com.suntek.vdm.gw.conf.service.MeetingTemplatesService;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.smc.request.meeting.templates.GetTemplatesListRequest;
import com.suntek.vdm.gw.smc.request.meeting.templates.ScheduleByTemplatesRequest;
import com.suntek.vdm.gw.smc.response.meeting.templates.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/conf-portal")
public class MeetingTemplatesController {

    @Autowired
    private MeetingTemplatesService meetingTemplatesService;
    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/conferences/templates")
    public ResponseEntity<String> addTemplates(@RequestBody AddTemplatesRequestEx request,
                                               @RequestHeader("Token") String token) {
        try {
            AddTemplatesResponseEx response = meetingTemplatesService.addTemplates(request, token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.CREATED);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PutMapping("/conferences/templates/{templateId}")
    public ResponseEntity<String> modifyTemplates(@RequestBody ModifyTemplatesRequestEx request,
                                                  @PathVariable("templateId") String templateId,
                                                  @RequestHeader("Token") String token) {
        try {
            ModifyTemplatesResponseEx response = meetingTemplatesService.modifyTemplates(templateId, request, token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.NO_CONTENT);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }


    @GetMapping("/conferences/templates/{templateId}")
    public ResponseEntity<String> getTemplates(@PathVariable("templateId") String templateId,
                                               @RequestHeader("Token") String token) {
        try {
            GetTemplatesResponseEx response = meetingTemplatesService.getTemplates(templateId, token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }


    @PostMapping("/conferences/templates/conditions")
    public ResponseEntity<String> getTemplatesList(@RequestBody GetTemplatesListRequest request,
                                                   @RequestParam("page") Integer page,
                                                   @RequestParam("size") Integer size,
                                                   @RequestParam(value="sort",required = false) String sort,
                                                   @RequestHeader("Token") String token) {
        try {
            GetTemplatesLisResponse response = meetingTemplatesService.getTemplatesList(page, size, sort, request, token);
            String res = objectMapper.writeValueAsString(response);
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/conferences/templates/{templateId}/participants?page={page}&size={size}&sort={sort}&name={name}")
    public ResponseEntity<String> getTemplatesParticipants(
            @PathVariable("templateId") String templateId,
            @PathVariable("page") Integer page,
            @PathVariable("size") Integer size,
            @PathVariable("sort") String sort,
            @PathVariable("name") String name,
            @RequestHeader("Token") String token) {
        try {
            GetTemplatesParticipantsResponse response = meetingTemplatesService.getTemplatesParticipants(templateId, page, size, sort, name, token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PostMapping("/conferences/templates/{templateId}/conference")
    public ResponseEntity<String> scheduleByTemplates(@RequestBody ScheduleByTemplatesRequest request,
                                                      @PathVariable("templateId") String templateId,
                                                      @RequestHeader("Token") String token) {
        try {
            ScheduleByTemplatesResponse response = meetingTemplatesService.scheduleByTemplates(templateId, request, token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }


    @DeleteMapping("/conferences/templates/{templateId}")
    public ResponseEntity<String> delTemplates(@PathVariable("templateId") String templateId,
                                               @RequestHeader("Token") String token) {
        try {
            meetingTemplatesService.delTemplates(templateId, token);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }


    @GetMapping("/conferences/templates/{templateId}/attendees?page={page}&size={size}")
    public ResponseEntity<String> getAttendeesById(@RequestBody ScheduleByTemplatesRequest request,
                                                   @PathVariable("templateId") String templateId,
                                                   @PathVariable("page") Integer page,
                                                   @PathVariable("size") Integer size,
                                                   @RequestHeader("Token") String token) {
        try {
            GetAttendeesByIdResponse response = meetingTemplatesService.getAttendeesById(templateId, page, size, token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PostMapping("/conferences/{conferenceId}/templates")
    public ResponseEntity<String> getAttendeesById(@PathVariable("conferenceId") String conferenceId,
                                                   @RequestHeader("Token") String token) {
        try {
            ConferencesToTemplateResponse response = meetingTemplatesService.conferencesToTemplate(conferenceId, token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

}
