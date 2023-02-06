package com.suntek.vdm.gw.api.controller.conf;

import com.alibaba.fastjson.JSON;
import com.suntek.vdm.gw.common.pojo.request.GetSiteRegiesterStatusReq;
import com.suntek.vdm.gw.common.pojo.request.meeting.GetConditionsMeetingRequest;
import com.suntek.vdm.gw.common.pojo.response.GetConditionsMeetingResponse;
import com.suntek.vdm.gw.common.pojo.response.room.GetSiteRegiesterStatusResp;
import com.suntek.vdm.gw.conf.api.request.ScheduleMeetingRequestEx;
import com.suntek.vdm.gw.conf.service.MeetingManagerService;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.smc.pojo.McuInfo;
import com.suntek.vdm.gw.smc.pojo.McuParam;
import com.suntek.vdm.gw.common.pojo.request.ScheduleConfBrief;
import com.suntek.vdm.gw.smc.request.meeting.management.*;
import com.suntek.vdm.gw.smc.response.meeting.management.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conf-portal")
public class MeetingManagerController {
    @Autowired
    private MeetingManagerService meetingManagerService;

    @PostMapping("/conferences")
    public ResponseEntity<String> scheduleConferences(@RequestBody ScheduleMeetingRequestEx scheduleMeetingRequest,
                                                      @RequestHeader("Token") String token) {
        try {
            ScheduleMeetingResponse response = meetingManagerService.scheduleConferences(scheduleMeetingRequest, token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.CREATED);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PostMapping("/conferences/conditions")
    public ResponseEntity<String> queryConferences(@RequestBody(required=false) GetConditionsMeetingRequest getConditionsMeetingRequest,
                                                   @RequestHeader("Token") String token,
                                                   @RequestParam(value = "page", required = false) Integer page,
                                                   @RequestParam(value = "size", required = false) Integer size) {
        try {
            GetConditionsMeetingResponse response = meetingManagerService.getConditions(getConditionsMeetingRequest, token, page, size);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PutMapping("/conferences/{conferenceId}")
    public ResponseEntity<String> modify(@RequestBody ModifyMeetingRequest request,
                                         @RequestHeader("Token") String token,
                                         @PathVariable("conferenceId") String conferenceId) {
        try {
            ModifyMeetingResponse response = meetingManagerService.modify(conferenceId, request, token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @GetMapping("/conferences/{conferenceId}")
    public ResponseEntity<String> getOne(@RequestHeader("Token") String token,
                                         @PathVariable("conferenceId") String conferenceId,
                                         @RequestParam(value = "confCasId", required = false) String confCasId) {
        try {
            GetOneMeetingResponse response = meetingManagerService.getOne(conferenceId, token, confCasId);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @GetMapping("/conferences/{conferenceId}/token")
    public ResponseEntity<String> getToken(@RequestHeader("Token") String token, @PathVariable ("conferenceId") String conferenceId) {
        try {
            String response = meetingManagerService.getToken (conferenceId, token);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }
    @GetMapping("/conferences/{conferenceId}/mcus")
    public ResponseEntity<String> getMcus(@RequestHeader("Token") String token,
                                          @PathVariable("conferenceId") String conferenceId) {
        try {
            List<McuInfo> response = meetingManagerService.getMcus(conferenceId, token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @GetMapping("/conferences/important")
    public ResponseEntity<String> getImportant(@RequestHeader("Token") String token,
                                               @RequestParam(value = "page", required = false) Integer page,
                                               @RequestParam(value = "size", required = false) Integer size) {
        try {
            List<ScheduleConfBrief> response = meetingManagerService.getImportant(page, size, token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PostMapping("/conferences/count")
    public ResponseEntity<String> getCount(@RequestBody GetCountMeetingRequest request,
                                            @RequestHeader("Token") String token
                                           ) {
        try {
            int response = meetingManagerService.getCount(request, token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @DeleteMapping("/conferences/{conferenceId}")
    public ResponseEntity<String> cancel(@RequestHeader("Token") String token,
                                         @PathVariable("conferenceId") String conferenceId) {
        try {
            meetingManagerService.cancel(conferenceId, token);
            return new ResponseEntity<>( HttpStatus.NO_CONTENT);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PostMapping("/conferences/{conferenceId}/template")
    public ResponseEntity<String> toTemplate(@RequestHeader("Token") String token,
                                             @PathVariable("conferenceId") String conferenceId) {
        try {
            MeetingToTemplateResponse response = meetingManagerService.toTemplate(conferenceId, token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PostMapping("/mail/conference/{conferenceId}")
    public ResponseEntity<String> sendMail(@RequestBody SendMeetingMailRequest request,
                                            @RequestHeader("Token") String token,
                                             @PathVariable("conferenceId") String conferenceId) {
        try {
            meetingManagerService.sendMail(conferenceId, request, token);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @GetMapping("/mail/conference/{conferenceId}")
    public ResponseEntity<String> getTimeZones(@RequestParam(value = "lang", required = false) String lang,
                                               @RequestHeader("Token") String token) {
        try {
            GetMeetingTimeZonesResponse response = meetingManagerService.getTimeZones(lang, token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PutMapping("/conferences/period/{conferenceId}")
    public ResponseEntity<String> modifyPeriod(@RequestBody ModifyPeriodMeetingRequest request,
                                               @RequestHeader("Token") String token,
                                               @PathVariable("conferenceId") String conferenceId) {
        try {
            ModifyPeriodMeetingResponse response = meetingManagerService.modifyPeriod(conferenceId, request, token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @GetMapping("/conferences/period/{conferenceId}")
    public ResponseEntity<String> getPeriodIds(@RequestHeader("Token") String token,
                                               @PathVariable("conferenceId") String conferenceId) {
        try {
            GetPeriodMeetingIdsResponse response = meetingManagerService.getPeriodIds(conferenceId, token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @DeleteMapping("/conferences/period/{conferenceId}")
    public ResponseEntity<String> delPeriod(@RequestHeader("Token") String token,
                                               @PathVariable("conferenceId") String conferenceId) {
        try {
            meetingManagerService.delPeriod(conferenceId, token);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @GetMapping("/conferences/{id}/participants")
    public ResponseEntity<String> getParticipants(@RequestHeader("Token") String token,
                                                  @PathVariable("id") String conferenceId,
                                                  @RequestParam(value = "page", required = false) Integer page,
                                                  @RequestParam(value = "size", required = false) Integer size,
                                                  @RequestParam(value = "name", required = false) String name) {
        try {
            GetMeetingParticipantsResponse response = meetingManagerService.getParticipants(conferenceId, page, size, name, token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PostMapping("/conferences/participants/calendar")
    public ResponseEntity<String> getParticipantsCalendar(@RequestBody GetParticipantsCalendarRequest request,
                                                          @RequestHeader("Token") String token) {
        try {
            List<GetParticipantsCalendarListResponse> response = meetingManagerService.getParticipantsCalendar(request, token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @GetMapping("/conferences/{conferenceId}/participants/{participantId}/multipic")
    public ResponseEntity<String> getMultipicMode(@PathVariable("conferenceId") String conferenceId,
                                                  @PathVariable("participantId") String participantId,
                                                  @RequestHeader("Token") String token) {
        try {
            McuParam response = meetingManagerService.getMultipicMode(conferenceId, participantId, token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PostMapping("/conferences/sso/ticket")
    public ResponseEntity<String> getSsoTicket(@RequestBody GetSsoTicketRequest request,
                                                  @RequestHeader("Token") String token) {
        try {
            GetSsoTicketResponse response = meetingManagerService.getSsoTicket(request, token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PostMapping("/conferences/sso/ticket/auth")
    public ResponseEntity<String> ssoTicketAuth(@RequestBody SsoTicketAuthRequest request,
                                               @RequestHeader("Token") String token) {
        try {
            SsoTicketAuthResponse response = meetingManagerService.ssoTicketAuth(request, token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @GetMapping("/conferences/record/{conferenceId}{guestPassword}")
    public ResponseEntity<String> getRecordAddress(@PathVariable("conferenceId") String conferenceId,
                                                   @PathVariable("guestPassword") String guestPassword,
                                                   @RequestHeader("Token") String token) {
        try {
            GetRecordAddressResponse response = meetingManagerService.getRecordAddress(conferenceId, guestPassword, token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @GetMapping("/conferences/external/recordAddress/{confId}")
    public ResponseEntity<String> getExternalRecordAddress(@PathVariable("confId") String conferenceId,
                                                           @RequestHeader("Token") String token) {
        try {
            GetExternalRecordAddressResponse response = meetingManagerService.getExternalRecordAddress(conferenceId, token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PostMapping("/conferences/sso/token/ticket")
    public ResponseEntity<String> getSsoTokenTicket(@RequestBody GetSsoTokenTicketRequest request,
                                                    @RequestHeader("Token") String token) {
        try {
            GetSsoTokenTicketResponse response = meetingManagerService.getSsoTokenTicket(request, token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    /**
     * 查询会场忙闲 200
     *
     * @param getSiteRegiesterStatusReq
     * @return
     */
    @PostMapping("/conferences/register/status/conditions")
    public ResponseEntity<String> queryVenuesStatus(@RequestHeader("Token") String token,
                                                    @RequestBody GetSiteRegiesterStatusReq getSiteRegiesterStatusReq) {
        try {
            List<GetSiteRegiesterStatusResp> response = meetingManagerService.getStieRegister(getSiteRegiesterStatusReq, token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }

    }
}
