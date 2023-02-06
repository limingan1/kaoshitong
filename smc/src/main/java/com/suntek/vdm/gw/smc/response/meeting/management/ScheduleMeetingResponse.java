package com.suntek.vdm.gw.smc.response.meeting.management;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.suntek.vdm.gw.common.pojo.ParticipantReq;
import com.suntek.vdm.gw.smc.pojo.*;
import com.suntek.vdm.gw.smc.request.meeting.management.ModifyMeetingRequest;
import com.suntek.vdm.gw.smc.request.meeting.management.ScheduleMeetingRequest;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Data
public class ScheduleMeetingResponse {
    /**
     * 会议响应基本信息
     */
    private ConferenceRsp conference;

    /**
     * 多点会议响应
     */
    private MultiConferenceServiceRsp multiConferenceService;

    /**
     * 流媒体会议响应
     */
    private StreamServiceRsp streamService;

    /**
     * 签到服务响应
     */
    private CheckInServiceRsp checkInService;

    /**
     * 会场列表
     */
    private List<ParticipantRsp> participants;

    /**
     * 与会者列表
     */
    private List<AttendeeRsp> attendees;

    /**
     * 字幕服务响应
     */
    private SubtitleServiceRsp subtitleService;

    /**
     * 会议预置参数
     */
    private ConferencePresetRsp confPresetParam;


    public ScheduleMeetingRequest toScheduleMeetingRequest() {
        return JSON.parseObject(JSON.toJSONString(this), ScheduleMeetingRequest.class);
    }

    public ModifyMeetingRequest toModifyMeetingRequest() {
        ModifyMeetingRequest modifyMeetingRequest = new ModifyMeetingRequest();
        ConferenceReq conferenceReq = JSON.parseObject(JSON.toJSONString(conference), ConferenceReq.class);
        modifyMeetingRequest.setConference(conferenceReq);

        MultiConferenceServiceReq multiConferenceServiceReq = JSON.parseObject(JSON.toJSONString(multiConferenceService), MultiConferenceServiceReq.class);
        modifyMeetingRequest.setMultiConferenceService(multiConferenceServiceReq);

        StreamServiceReq streamServiceRsp = JSON.parseObject(JSON.toJSONString(streamService), StreamServiceReq.class);
        modifyMeetingRequest.setStreamService(streamServiceRsp);

        CheckInServiceReq checkInServiceRsp = JSON.parseObject(JSON.toJSONString(checkInService), CheckInServiceReq.class);
        modifyMeetingRequest.setCheckInService(checkInServiceRsp);

        List<ParticipantReq> participantReqs = new ArrayList<>();
        for(ParticipantRsp participantRsp: participants){
            ParticipantReq participantReq = JSON.parseObject(JSON.toJSONString(participantRsp),ParticipantReq.class);
            participantReqs.add(participantReq);
            TpParam tpParam = participantRsp.getTpParam();
            if(tpParam != null){
                String leftUri = tpParam.getLeftUri();
                String rightUri = tpParam.getRightUri();
                if(StringUtils.isEmpty(leftUri) && StringUtils.isEmpty(rightUri)){
                    continue;
                }
                participantReq.setUri(participantReq.getUri()+";"+leftUri+";"+rightUri);
            }
        }
        modifyMeetingRequest.setParticipants(participantReqs);

        List<AttendeeReq> attendeeReqs = JSON.parseObject(JSON.toJSONString(attendees), new TypeReference<List<AttendeeReq>>() {
        });
        modifyMeetingRequest.setAttendees(attendeeReqs);
        SubtitleServiceRsp subtitleServiceRsp = JSON.parseObject(JSON.toJSONString(subtitleService), SubtitleServiceRsp.class);

        if (streamServiceRsp != null) {
            SubtitleServiceReq subtitleServiceReq = new SubtitleServiceReq();
            subtitleServiceReq.setEnableSubtitle(subtitleServiceRsp.getEnableSubtitle());

            String srcLang = subtitleServiceRsp.getSrcLang();
            if ("CHINESE".equals(srcLang)) {
                subtitleServiceReq.setSrcLang(1);
            } else {
                subtitleServiceReq.setSrcLang(0);
            }
            modifyMeetingRequest.setSubtitleService(subtitleServiceReq);
        }

        ConferencePresetRsp conferencePresetRsp = JSON.parseObject(JSON.toJSONString(confPresetParam), ConferencePresetRsp.class);
        modifyMeetingRequest.setConfPresetParam(conferencePresetRsp);

        return modifyMeetingRequest;
    }

    /**
     * 是不是预约会议
     *
     * @return
     */
    public boolean editConferenceFlag() {
        return "EDIT_CONFERENCE".equals(conference.getConferenceTimeType());
    }


    public String getConferenceId() {
        return getConference().getId();
    }

    public String getSubject() {
        return getConference().getSubject();
    }

    public String getAccessCode() {
        return getMultiConferenceService().getAccessCode();
    }
}
