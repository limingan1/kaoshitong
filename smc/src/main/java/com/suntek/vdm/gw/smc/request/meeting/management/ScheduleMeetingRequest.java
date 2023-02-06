package com.suntek.vdm.gw.smc.request.meeting.management;

import com.alibaba.fastjson.JSON;
import com.suntek.vdm.gw.common.pojo.ParticipantReq;
import com.suntek.vdm.gw.smc.pojo.*;
import lombok.Data;

import java.util.List;

@Data
public class ScheduleMeetingRequest {
    /**
     * 会议基本信息
     */
    private ConferenceReq conference;

    /**
     * 多点会议请求（可选）
     */
    private MultiConferenceServiceReq multiConferenceService;

    /**
     * 流媒体会议请求（可选）
     */
    private StreamServiceReq streamService;

    /**
     * 签到服务区请求（可选）
     */
    private CheckInServiceReq checkInService;

    /**
     * 会场列表（可选）
     */
    private List<ParticipantReq> participants;

    /**
     * 与会人列表（可选）
     */
    private List<AttendeeReq> attendees;

    /**
     * 字幕服务请求（可选）
     */
    private SubtitleServiceReq subtitleService;

    /**
     * 会议预置参数（可选）
     */
    private ConferencePresetRsp confPresetParam;

    /**
     * 是不是预约会议
     * @return
     */
    public boolean  editConferenceFlag(){
      return   "EDIT_CONFERENCE".equals(conference.getConferenceTimeType());
    }

    public boolean  periodConferenceFlag(){
        return   "PERIOD_CONFERENCE".equals(conference.getConferenceTimeType());
    }

    /**
     * 是不是即时会议
     * @return
     */
    public boolean  instantConferenceFlag(){
        return   "INSTANT_CONFERENCE".equals(conference.getConferenceTimeType());
    }

    public ModifyMeetingRequest toModifyMeetingRequest(){
        return   JSON.parseObject(JSON.toJSONString(this),ModifyMeetingRequest.class);
    }

    public void delRecordParam(){
        setStreamService(null);
        if(multiConferenceService!= null){
            ConferenceCapabilityReq conferenceCapabilityReq = multiConferenceService.getConferenceCapabilitySetting();
            conferenceCapabilityReq.setEnableRecord(false);
            conferenceCapabilityReq.setEnableLiveBroadcast(false);
            conferenceCapabilityReq.setAutoRecord(false);
            conferenceCapabilityReq.setAudioRecord(false);
            conferenceCapabilityReq.setAmcRecord(false);
        }
    }

}
