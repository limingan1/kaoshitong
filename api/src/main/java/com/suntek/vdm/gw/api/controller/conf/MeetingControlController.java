package com.suntek.vdm.gw.api.controller.conf;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.suntek.vdm.gw.common.api.request.DurationMeetingRequestEx;
import com.suntek.vdm.gw.common.api.request.MeetingControlRequest;
import com.suntek.vdm.gw.common.api.request.MeetingControlRequestEx;
import com.suntek.vdm.gw.common.api.request.ParticipantsControlRequestEx;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.GetParticipantsDetailInfoResponse;
import com.suntek.vdm.gw.common.pojo.MergeConferenceReq;
import com.suntek.vdm.gw.common.pojo.ParticipantReq;
import com.suntek.vdm.gw.common.pojo.request.GetParticipantsRequest;
import com.suntek.vdm.gw.common.pojo.response.meeting.GetMeetingDetailResponse;
import com.suntek.vdm.gw.common.pojo.response.meeting.GetParticipantsResponse;
import com.suntek.vdm.gw.conf.api.request.*;
import com.suntek.vdm.gw.conf.pojo.WatchInfo;
import com.suntek.vdm.gw.conf.service.MeetingControlService;
import com.suntek.vdm.gw.core.customexception.BaseStateException;
import com.suntek.vdm.gw.smc.pojo.*;
import com.suntek.vdm.gw.smc.request.meeting.control.*;
import com.suntek.vdm.gw.smc.response.meeting.control.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conf-portal")
public class MeetingControlController {
    @Autowired
    private MeetingControlService meetingControlService;

    @DeleteMapping("/online/conferences/{conferenceId}")
    public ResponseEntity<String> delMeeting(@PathVariable String conferenceId,
                                             @RequestHeader("Token") String token,
                                             @RequestParam(value = "confCasId", required = false) String confCasId,
                                             @RequestParam(value = "keepByCasState", required = false) Boolean keepByCasState) {
        try {
            if ("null".equals(confCasId) || confCasId == null) {
                confCasId = null;
            }
            if (keepByCasState == null) {
                keepByCasState = false;
            }
            meetingControlService.delMeeting(conferenceId, token, confCasId, keepByCasState);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }


    @PatchMapping("/online/conferences/{conferenceId}/status")
    public ResponseEntity<String> meetingControl(@RequestBody MeetingControlRequestEx meetingControlRequestEx,
                                                 @RequestHeader("Token") String token,
                                                 @PathVariable("conferenceId") String conferenceId) {
        try {
            meetingControlService.meetingControl(meetingControlRequestEx, conferenceId, token);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PatchMapping("/online/conferences/{conferenceId}/status/direct")
    public ResponseEntity<String> meetingControlDirect(@RequestBody MeetingControlRequestEx meetingControlRequestEx,
                                                       @RequestHeader("Token") String token,
                                                       @PathVariable("conferenceId") String conferenceId) {
        try {
            meetingControlService.meetingControlDirect(meetingControlRequestEx, conferenceId, token);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PatchMapping("/online/conferences/{confCasId}/status/top/{lowConferenceId}")
    public ResponseEntity<String> meetingControlTop(@RequestBody MeetingControlRequest meetingControlRequest,
                                                    @RequestHeader("Token") String token,
                                                    @PathVariable("confCasId") String confCasId,
                                                    @PathVariable("lowConferenceId") String lowConferenceId) {
        try {
            meetingControlService.meetingControlTop(meetingControlRequest, confCasId, lowConferenceId, token);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }


    @PatchMapping("/online/conferences/{conferenceId}/participants/{participantId}/status")
    public ResponseEntity<String> participantsControl(@RequestBody ParticipantsControlRequestEx participantsControlRequestEx,
                                                      @RequestHeader("Token") String token,
                                                      @PathVariable("conferenceId") String conferenceId,
                                                      @PathVariable("participantId") String participantId) {
        try {
            WatchInfo watchInfo = meetingControlService.participantsControl(participantsControlRequestEx, conferenceId, participantId, token);
            if (watchInfo != null) {
                return new ResponseEntity<>(JSON.toJSONString(watchInfo), HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PatchMapping("/online/conferences/{conferenceId}/participants/{participantId}/status/pull")
    public ResponseEntity<String> pullSource(@RequestBody PullSourceRequest request,
                                             @RequestHeader("Token") String token,
                                             @PathVariable("conferenceId") String conferenceId,
                                             @PathVariable("participantId") String participantId
    ) {
        try {
            WatchInfo watchInfo = meetingControlService.pullSource(conferenceId, participantId, request, token);
            return new ResponseEntity<>(JSON.toJSONString(watchInfo), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        } catch (BaseStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/online/conferences/{conferenceId}/participants/status")
    public ResponseEntity<String> participantsControl(@RequestBody List<ParticipantsControlRequestEx> participantsControlRequestExs,
                                                      @RequestHeader("Token") String token,
                                                      @PathVariable("conferenceId") String conferenceId) {
        try {
            meetingControlService.participantsControl(participantsControlRequestExs, conferenceId, token);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PostMapping("/online/conferences/{conferenceId}/textTips")
    public ResponseEntity<String> setTextTips(@RequestBody SetTextTipsRequestEx setTextTipsRequestEx,
                                              @RequestHeader("Token") String token,
                                              @PathVariable("conferenceId") String conferenceId) {
        try {
            meetingControlService.setTextTips(setTextTipsRequestEx, conferenceId, token);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PostMapping("/online/conferences/{conferenceId}/participants/{participantId}textTips")
    public ResponseEntity<String> setTextTips(@RequestBody SetTextTipsRequestEx setTextTipsRequestEx,
                                              @RequestHeader("Token") String token,
                                              @PathVariable("participantId") String participantId,
                                              @PathVariable("conferenceId") String conferenceId) {
        try {
            meetingControlService.setTextTips(setTextTipsRequestEx, conferenceId, participantId, token);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PutMapping("/online/conferences/{conferenceId}/duration")
    public ResponseEntity<String> duration(@RequestBody DurationMeetingRequestEx durationMeetingRequestEx,
                                           @RequestHeader("Token") String token,
                                           @PathVariable("conferenceId") String conferenceId) {
        try {
            meetingControlService.duration(durationMeetingRequestEx, conferenceId, token);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PostMapping("/online/conferences/{conferenceId}/participants")
    public ResponseEntity<String> addParticipants(@RequestBody List<ParticipantReq> participantReqs,
                                                  @RequestHeader("Token") String token,
                                                  @PathVariable("conferenceId") String conferenceId,
                                                  @RequestParam(value = "confCasId", required = false) String confCasId,
                                                  @RequestParam(value = "createSign", required = false) Boolean createSign //是否需要创建会议
                                                    ) {
        try {
            meetingControlService.addParticipants(participantReqs, conferenceId, token, confCasId, createSign != null && createSign);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PostMapping("/online/conferences/{conferenceId}/merge")
    public ResponseEntity<String> mergeConference(@PathVariable String conferenceId,
                                                  @RequestBody MergeConferenceReq req,
                                                  @RequestHeader("Token") String token,
                                                  @RequestParam(required = false, value = "confCasId") String confCasId
    ) {
        try {
            meetingControlService.mergeConference(confCasId,conferenceId, req, token);

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @DeleteMapping("/online/conferences/{conferenceId}/participants")
    public ResponseEntity<String> delParticipants(@RequestBody List<String> delParticipantIds,
                                                  @RequestHeader("Token") String token,
                                                  @PathVariable("conferenceId") String conferenceId,
                                                  @RequestParam(value = "confCasId", required = false) String confCasId) {
        try {
            meetingControlService.delParticipants(delParticipantIds, conferenceId, token, confCasId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @GetMapping("/online/conferences/{conferenceId}/detail")
    public ResponseEntity<String> getMeetingDetail(@RequestHeader("Token") String token,
                                                   @RequestHeader(value = "isQueryMultiPicInfo", required = false) String isQueryMultiPicInfo,
                                                   @PathVariable("conferenceId") String conferenceId,
                                                   @RequestParam(value = "confCasId", required = false) String confCasId) {
        try {
            if (confCasId != null && confCasId.contains("@@")) {
                confCasId = confCasId.split("@@")[0];
            }
            GetMeetingDetailResponse response = meetingControlService.getMeetingDetail(conferenceId, token, confCasId, isQueryMultiPicInfo);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        } catch (BaseStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/online/conferences/{conferenceId}/participants/conditions")
    public ResponseEntity<String> getParticipants(@RequestBody GetParticipantsRequest getParticipantsRequest,
                                                  @RequestHeader("Token") String token,
                                                  @RequestHeader(value = "towall", required = false) String towall,
                                                  @PathVariable("conferenceId") String conferenceId,
                                                  @RequestParam(value = "page", required = false) Integer page,
                                                  @RequestParam(value = "size", required = false) Integer size,
                                                  @RequestParam(value = "confCasId", required = false) String confCasId) {
        try {
            if("null".equals(confCasId)){
                confCasId = null;
            }
            if (confCasId != null && confCasId.contains("@@")) {
                confCasId = confCasId.split("@@")[0];
            }
            GetParticipantsResponse response = meetingControlService.getParticipants(getParticipantsRequest, conferenceId, token, confCasId, page, size, towall);
            return new ResponseEntity<>(JSON.toJSONString(response, SerializerFeature.DisableCircularReferenceDetect), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PostMapping("/online/conferences/{conferenceId}/participants/{participantId}/camera")
    public ResponseEntity<String> cameraControl(@RequestBody CameraControlRequest cameraControlRequest,
                                                @RequestHeader("Token") String token,
                                                @PathVariable("conferenceId") String conferenceId,
                                                @PathVariable("participantId") String participantId,
                                                @RequestParam(value = "confCasId", required = false) String confCasId) {
        try {
            meetingControlService.cameraControl(cameraControlRequest, conferenceId, participantId, token, confCasId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PostMapping("/online/conferences/{conferenceId}/participants/{participantId}/fellow")
    public ResponseEntity<String> participantsFellow(@RequestBody ParticipantsFellowRequest participantsFellowRequest,
                                                     @RequestHeader("Token") String token,
                                                     @PathVariable("conferenceId") String conferenceId,
                                                     @PathVariable("participantId") String participantId,
                                                     @RequestParam(value = "confCasId", required = false) String confCasId) {
        try {
            meetingControlService.participantsFellow(participantsFellowRequest, conferenceId, participantId, token, confCasId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PostMapping("/online/conferences/{conferenceId}/chat/mic")
    public ResponseEntity<String> chatMic(@RequestBody ChatMicRequest chatMicRequest,
                                          @RequestHeader("Token") String token,
                                          @PathVariable("conferenceId") String conferenceId) {
        try {
            meetingControlService.chatMic(conferenceId, chatMicRequest, token);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PostMapping("/online/conferences/{conferenceId}/chat/speaker")
    public ResponseEntity<String> chatSpeaker(@RequestBody ChatSpeakerRequest chatSpeakerRequest,
                                              @RequestHeader("Token") String token,
                                              @PathVariable("conferenceId") String conferenceId) {
        try {
            meetingControlService.chatSpeaker(conferenceId, chatSpeakerRequest, token);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PostMapping("/online/conferences/{conferenceId}/attendees")
    public ResponseEntity<String> addAttendees(@RequestBody List<AttendeeReq> addAttendeesRequest,
                                               @RequestHeader("Token") String token,
                                               @PathVariable("conferenceId") String conferenceId,
                                               @RequestParam(value = "confCasId", required = false) String confCasId) {
        try {
            meetingControlService.addAttendees(conferenceId, addAttendeesRequest, token, confCasId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PostMapping("/online/conferences/{conferenceId}/participants/briefs")
    public ResponseEntity<String> getParticipantsBriefs(@RequestBody List<String> request,
                                                        @RequestHeader("Token") String token,
                                                        @PathVariable("conferenceId") String conferenceId) {
        try {
            meetingControlService.getParticipantsBriefs(conferenceId, request, token);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @GetMapping("/online/conferences/{conferenceId}/participants/{participantId}/detailInfo")
    public ResponseEntity<String> getParticipantsDetailInfo(@PathVariable("participantId") String participantId,
                                                            @RequestHeader("Token") String token,
                                                            @PathVariable("conferenceId") String conferenceId) {
        try {
            GetParticipantsDetailInfoResponse response = meetingControlService.getParticipantsDetailInfo(conferenceId, participantId, token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @GetMapping("/online/conferences/{conferenceId}/participants/{participantId}/capability")
    public ResponseEntity<String> getParticipantsCapability(@PathVariable("participantId") String participantId,
                                                            @RequestHeader("Token") String token,
                                                            @PathVariable("conferenceId") String conferenceId) {
        try {
            GetParticipantsCapabilityResponse response = meetingControlService.getParticipantsCapability(conferenceId, participantId, token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PutMapping("/online/conferences/{conferenceId}/participants/order")
    public ResponseEntity<String> setCommonlyUsedParticipants(@RequestBody SetCommonlyUsedParticipantsRequest request,
                                                              @RequestHeader("Token") String token,
                                                              @PathVariable("conferenceId") String conferenceId) {
        try {
            meetingControlService.setCommonlyUsedParticipants(conferenceId, request, token);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PostMapping("/online/conferences/{conferenceId}/participants/order")
    public ResponseEntity<String> getCommonlyUsedParticipants(@RequestBody GetCommonlyUsedParticipantsRequest request,
                                                              @RequestHeader("Token") String token,
                                                              @PathVariable("conferenceId") String conferenceId) {
        try {
            GetCommonlyUsedParticipantsResponse response = meetingControlService.getCommonlyUsedParticipants(conferenceId, request, token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PostMapping("/online/conferences/{conferenceId}/participants/groups/{groupId}")
    public ResponseEntity<String> subscribeParticipantsStatus(@RequestBody SubscribeParticipantsStatusRequest request,
                                                              @RequestHeader("Token") String token,
                                                              @PathVariable("groupId") String groupId,
                                                              @PathVariable("conferenceId") String conferenceId) {
        try {
            meetingControlService.subscribeParticipantsStatus(conferenceId, groupId, request, token);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @DeleteMapping("/online/conferences/{conferenceId}/participants/groups/{groupId}")
    public ResponseEntity<String> unSubscribeParticipantsStatus(@RequestHeader("Token") String token,
                                                                @PathVariable("groupId") String groupId,
                                                                @PathVariable("conferenceId") String conferenceId) {
        try {
            meetingControlService.unSubscribeParticipantsStatus(conferenceId, groupId, token);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PostMapping("/online/conferences/{conferenceId}/participants/groups/{groupId}/realTimeInfo")
    public ResponseEntity<String> subscribeParticipantsStatusRealTime(@RequestBody List<String> request,
                                                                      @RequestHeader("Token") String token,
                                                                      @PathVariable("groupId") String groupId,
                                                                      @PathVariable("conferenceId") String conferenceId) {
        try {
            meetingControlService.subscribeParticipantsStatusRealTime(conferenceId, groupId, request, token);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @DeleteMapping("/online/conferences/{conferenceId}/participants/groups/{groupId}/realTimeInfo")
    public ResponseEntity<String> unSubscribeParticipantsStatusRealTime(@RequestHeader("Token") String token,
                                                                        @PathVariable("groupId") String groupId,
                                                                        @PathVariable("conferenceId") String conferenceId) {
        try {
            meetingControlService.unSubscribeParticipantsStatusRealTime(conferenceId, groupId, token);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PostMapping("/online/conferences/{conferenceId}/participants/{participantId}/remind")
    public ResponseEntity<String> setRemind(@RequestHeader("Token") String token,
                                            @PathVariable("participantId") String participantId,
                                            @PathVariable("conferenceId") String conferenceId) {
        try {
            meetingControlService.setRemind(conferenceId, participantId, token);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PostMapping("/online/conferences/{conferenceId}/participants/chairmanPoll")
    public ResponseEntity<String> setChairmanPoll(@RequestBody SetChairmanPollRequest request,
                                                  @RequestHeader("Token") String token,
                                                  @PathVariable("conferenceId") String conferenceId) {
        try {
            meetingControlService.setChairmanPoll(conferenceId, request, token);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @GetMapping("/online/conferences/{conferenceId}/participants/chairmanPoll")
    public ResponseEntity<String> getChairmanPoll(@RequestHeader("Token") String token,
                                                  @PathVariable("conferenceId") String conferenceId) {
        try {
            GetChairmanPollResponse response = meetingControlService.getChairmanPoll(conferenceId, token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PostMapping("/online/conferences/{conferenceId}/broadcastPoll")
    public ResponseEntity<String> setBroadcastPoll(@RequestBody SetBroadcastPollRequest request,
                                                   @RequestHeader("Token") String token,
                                                   @PathVariable("conferenceId") String conferenceId) {
        try {
            meetingControlService.setBroadcastPoll(conferenceId, request, token);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @GetMapping("/online/conferences/{conferenceId}/broadcastPoll")
    public ResponseEntity<String> getBroadcastPoll(@RequestHeader("Token") String token,
                                                   @PathVariable("conferenceId") String conferenceId) {
        try {
            GetBroadcastPollResponse response = meetingControlService.getBroadcastPoll(conferenceId, token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PostMapping("/online/conferences/{conferenceId}/multiPicPoll")
    public ResponseEntity<String> setMultiPicPoll(@RequestBody SetMultiPicPollRequest request,
                                                  @RequestHeader("Token") String token,
                                                  @PathVariable("conferenceId") String conferenceId) {
        try {
            meetingControlService.setMultiPicPoll(conferenceId, request, token);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @GetMapping("/online/conferences/{conferenceId}/multiPicPoll")
    public ResponseEntity<String> getMultiPicPoll(@RequestHeader("Token") String token,
                                                  @PathVariable("conferenceId") String conferenceId) {
        try {
            GetMultiPicPollResponse response = meetingControlService.getMultiPicPoll(conferenceId, token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }


    @PostMapping("/online/conferences/{conferenceId}/participants/{participantId}/parameter")
    public ResponseEntity<String> setParticipantsParameter(@RequestBody SetParticipantsParameterRequest request,
                                                           @RequestHeader("Token") String token,
                                                           @PathVariable("participantId") String participantId,
                                                           @PathVariable("conferenceId") String conferenceId) {
        try {
            meetingControlService.setParticipantsParameter(conferenceId, participantId, request, token);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PostMapping("/online/conferences/{conferenceId}/migrate")
    public ResponseEntity<String> migrate(@RequestBody MigrateRequest request,
                                          @RequestHeader("Token") String token,
                                          @PathVariable("conferenceId") String conferenceId) {
        try {
            meetingControlService.migrate(conferenceId, request, token);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PostMapping("/online/conferences/{conferenceId}/participants/videoSource")
    public ResponseEntity<String> getVideoSource(@RequestBody List<String> request,
                                                 @RequestHeader("Token") String token,
                                                 @PathVariable("conferenceId") String conferenceId) {
        try {
            List<VideoSrcInfo> response = meetingControlService.getVideoSource(conferenceId, request, token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PostMapping("/online/conferences/{conferenceId}/rseStream")
    public ResponseEntity<String> rseStream(@RequestBody RseStreamRequest request,
                                            @RequestHeader("Token") String token,
                                            @PathVariable("conferenceId") String conferenceId) {
        try {
            meetingControlService.rseStream(conferenceId, request, token);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }


    @PostMapping("/online/conferences/{conferenceId}/participants/batchTextTips")
    public ResponseEntity<String> batchTextTips(@RequestBody BatchTextTipsRequest request,
                                                @RequestHeader("Token") String token,
                                                @PathVariable("conferenceId") String conferenceId) {
        try {
            meetingControlService.batchTextTips(conferenceId, request, token);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PatchMapping("/online/conferences/{conferenceId}/participants/groups/{groupId}")
    public ResponseEntity<String> updateSubscribeParticipantsStatus(@RequestBody UpdateSubscribeParticipantsStatusRequest request,
                                                                    @RequestHeader("Token") String token,
                                                                    @PathVariable("groupId") String groupId,
                                                                    @PathVariable("conferenceId") String conferenceId) {
        try {
            meetingControlService.updateSubscribeParticipantsStatus(conferenceId, groupId, request, token);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PostMapping("/online/conferences/{conferenceId}/participants/ai/caption")
    public ResponseEntity<String> pushAiCaption(@RequestBody String request,
                                                @RequestHeader("Token") String token,
                                                @PathVariable("conferenceId") String conferenceId) {
        try {
            meetingControlService.pushAiCaption(conferenceId, request, token);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @GetMapping("/online/conferences/{conferenceId}/presetParam")
    public ResponseEntity<String> getPresetParam(@RequestHeader("Token") String token,
                                                 @PathVariable("conferenceId") String conferenceId) {
        try {
            GetPresetParamResponse response = meetingControlService.getPresetParam(conferenceId, token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PutMapping("/conferences/quickHangup")
    public ResponseEntity<String> quickHangup(@RequestHeader("Token") String token,
                                              @RequestBody String uri) {
        try {
            meetingControlService.quickHangup(uri, token);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PostMapping("/conferences/callInfo")
    public ResponseEntity<String> callInfo(@RequestHeader("Token") String token,
                                           @RequestBody String uri) {
        try {
            CallInfoRsp response = meetingControlService.callInfo(uri, token);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PutMapping("/online/conferences/{conferenceId}/participants/param")
    public ResponseEntity<String> changeSiteName(@RequestHeader("Token") String token,
                                           @PathVariable String conferenceId,
                                           @RequestBody ParticipantUpdateDto participantUpdateDto
                                           ) {
        try {
            meetingControlService.changeSiteName(conferenceId, participantUpdateDto, token);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }
}
