package com.suntek.vdm.gw.smc.adaptService.Impl;

import com.alibaba.fastjson.JSON;
import com.huawei.vdmserver.common.dto.*;
import com.huawei.vdmserver.common.dto.requestDto.*;
import com.huawei.vdmserver.common.vo.ConfVO;
import com.huawei.vdmserver.smc.core.service.*;
import com.suntek.vdm.gw.common.api.request.MeetingControlRequest;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.ParticipantReq;
import com.suntek.vdm.gw.common.pojo.request.GetParticipantsRequest;
import com.suntek.vdm.gw.common.pojo.request.meeting.DurationMeetingRequest;
import com.suntek.vdm.gw.common.pojo.request.meeting.ParticipantsControlRequest;
import com.suntek.vdm.gw.smc.adaptService.AdaptMeetingControlService;
import com.suntek.vdm.gw.smc.adaptService.util.AdaptHttpStateUtil;
import com.suntek.vdm.gw.smc.pojo.AttendeeReq;
import com.suntek.vdm.gw.smc.request.meeting.control.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdaptMeetingControlServiceImpl implements AdaptMeetingControlService {
    @Autowired
    @Qualifier("SmcConferenceScheduledService2.0")
    SmcConferenceScheduledService smcConferenceScheduledService;

    @Autowired
    @Qualifier("SmcMeetingControlService2.0")
    SmcMeetingControlService smcMeetingControlService;

    @Autowired
    @Qualifier("SmcMeetingBannerService2.0")
    SmcMeetingBannerService smcMeetingBannerService;

    @Autowired
    @Qualifier("SmcVenueControlService2.0")
    SmcVenueControlService smcVenueControlService;

    @Autowired
    @Qualifier("SmcBathVenueControlService2.0")
    SmcBathVenueControlService smcBathVenueControlService;

    @Override
    public String getMeetingDetail(String conferenceId, String token, String isQueryMultiPicInfo) throws MyHttpException {
        ConfVO confVO = new ConfVO();
        confVO.setToken(token);
        confVO.setConferenceId(conferenceId);
        QueryConferenceStatusReq queryConferenceStatusReq = new QueryConferenceStatusReq();
        if("true".equals(isQueryMultiPicInfo)){
            queryConferenceStatusReq.setIsQueryMultiPicInfo(true);
        }
        confVO.setData(queryConferenceStatusReq);
        ResponseEntityEx<?> object = smcConferenceScheduledService.queryConferencesStatus(confVO);
        return AdaptHttpStateUtil.dealAdaptHttpStatus(object);
    }

    @Override
    public void setTextTips(String conferenceId, String participantId, SetTextTipsRequest request, String token) throws MyHttpException {


    }

    @Override
    public void meetingControl(String conferenceId, MeetingControlRequest request, String token) throws MyHttpException {
        MeetingControl meetingControl = JSON.parseObject(JSON.toJSONString(request), MeetingControl.class);
        ErrorInfo errorInfo = smcMeetingControlService.conferenceControl(conferenceId, meetingControl, token);
        if(errorInfo != null){
            throw new MyHttpException(errorInfo.getHttpCode(), JSON.toJSONString(errorInfo));
        }
    }

    @Override
    public void chatMic(String conferenceId, ChatMicRequest request, String token) throws MyHttpException {

    }

    @Override
    public void chatSpeaker(String conferenceId, ChatSpeakerRequest request, String token) throws MyHttpException {

    }

    @Override
    public void setTextTips(String conferenceId, SetTextTipsRequest request, String token) throws MyHttpException {
        MeetingBanner meetingBanner = JSON.parseObject(JSON.toJSONString(request), MeetingBanner.class);
        ErrorInfo errorInfo = smcMeetingBannerService.setMeetingBanner(conferenceId, meetingBanner, token);
        if(errorInfo != null){
            throw new MyHttpException(errorInfo.getHttpCode(), JSON.toJSONString(errorInfo));
        }

    }

    @Override
    public String duration(String conferenceId, DurationMeetingRequest request, String token) throws MyHttpException {
        ProlongScheduledConfReq prolongScheduledConfReq = JSON.parseObject(JSON.toJSONString(request), ProlongScheduledConfReq.class);
        ConfVO confVO = new ConfVO();
        confVO.setData(prolongScheduledConfReq);
        confVO.setConferenceId(conferenceId);
        confVO.setToken(token);
        ResponseEntityEx<?> object = smcConferenceScheduledService.prolongScheduledConf(confVO);
        return AdaptHttpStateUtil.dealAdaptHttpStatus(object);
    }

    @Override
    public String delMeeting(String conferenceId, String token) throws MyHttpException {
        ConfVO confVO = new ConfVO();
        confVO.setConferenceId(conferenceId);
        confVO.setToken(token);
        ResponseEntityEx<?> object = smcConferenceScheduledService.delScheduledConf(confVO);
        return AdaptHttpStateUtil.dealAdaptHttpStatus(object);
    }

    @Override
    public String addParticipants(String conferenceId, List<ParticipantReq> request, String token) throws MyHttpException {
        List<com.huawei.vdmserver.common.dto.ParticipantReq> list = JSON.parseArray(JSON.toJSONString(request), com.huawei.vdmserver.common.dto.ParticipantReq.class);
        ConfVO confVO = new ConfVO();
        confVO.setToken(token);
        confVO.setConferenceId(conferenceId);
        confVO.setData(list);
        ResponseEntityEx<?> object = smcConferenceScheduledService.addSitesInScheduleConf(confVO);
        return AdaptHttpStateUtil.dealAdaptHttpStatus(object);
    }

    @Override
    public void addAttendees(String conferenceId, List<AttendeeReq> request, String token) throws MyHttpException {

    }

    @Override
    public String getParticipants(String conferenceId, int page, int size, GetParticipantsRequest request, String token) throws MyHttpException {
        QueryConfSitesStatusReq queryConfSitesStatusReq = JSON.parseObject(JSON.toJSONString(request), QueryConfSitesStatusReq.class);
        ConfVO confVO = new ConfVO();
        confVO.setToken(token);
        confVO.setSize(size);
        confVO.setPage(page);
        confVO.setConferenceId(conferenceId);
        confVO.setData(queryConfSitesStatusReq);
        ResponseEntityEx<?> object = smcConferenceScheduledService.queryConfSitesStatus(confVO);
        return AdaptHttpStateUtil.dealAdaptHttpStatus(object);
    }

    @Override
    public String getParticipantsBriefs(String conferenceId, List<String> request, String token) throws MyHttpException {
        return null;
    }

    @Override
    public String getParticipantsDetailInfo(String conferenceId, String participantId, String token) throws MyHttpException {
        ConfVO confVO = new ConfVO();
        confVO.setToken(token);
        confVO.setConferenceId(conferenceId);
        confVO.setData(participantId);
        ResponseEntityEx<?> object = smcConferenceScheduledService.queryConfSiteDetailInfo(confVO);
        return AdaptHttpStateUtil.dealAdaptHttpStatus(object);
    }

    @Override
    public String getParticipantsCapability(String conferenceId, String participantId, String token) throws MyHttpException {
        return null;
    }

    @Override
    public String participantsControl(String conferenceId, String participantId, ParticipantsControlRequest request, String token) throws MyHttpException {
        VenueControl venueControl = JSON.parseObject(JSON.toJSONString(request), VenueControl.class);
        ErrorInfo errorInfo = smcVenueControlService.venueControl(conferenceId,participantId, venueControl, token);
        if(errorInfo != null){
            throw new MyHttpException(errorInfo.getHttpCode(), JSON.toJSONString(errorInfo));
        }
        return null;
    }

    @Override
    public void participantsControl(String conferenceId, List<ParticipantsControlRequest> request, String token) throws MyHttpException {
        List<VenueControl> venueControlList = JSON.parseArray(JSON.toJSONString(request), VenueControl.class);
        ErrorInfo errorInfo = smcBathVenueControlService.bathVenueControl(conferenceId, venueControlList, token);
        if(errorInfo != null){
            throw new MyHttpException(errorInfo.getHttpCode(), JSON.toJSONString(errorInfo));
        }
    }

    @Override
    public void participantsFellow(String conferenceId, String participantId, ParticipantsFellowRequest request, String token) throws MyHttpException {
        SetSourceFellowReq setSourceFellowReq = JSON.parseObject(JSON.toJSONString(request), SetSourceFellowReq.class);
        ConfVO confVO = new ConfVO();
        confVO.setToken(token);
        confVO.setConferenceId(conferenceId);
        setSourceFellowReq.setConfereceId(conferenceId);
        setSourceFellowReq.setParticipantId(participantId);
        confVO.setData(setSourceFellowReq);
        ResponseEntityEx<?> object = smcConferenceScheduledService.setSourceFellow(confVO);
        AdaptHttpStateUtil.dealAdaptHttpStatus(object);
    }

    @Override
    public void cameraControl(String conferenceId, String participantId, CameraControlRequest request, String token) throws MyHttpException {
        SiteCameraCtrlReq siteCameraCtrlReq = JSON.parseObject(JSON.toJSONString(request), SiteCameraCtrlReq.class);
        ConfVO confVO = new ConfVO();
        confVO.setToken(token);
        confVO.setConferenceId(conferenceId);
        siteCameraCtrlReq.setConfId(conferenceId);
        siteCameraCtrlReq.setSiteUri(participantId);
        confVO.setData(siteCameraCtrlReq);
        ResponseEntityEx<?> object = smcConferenceScheduledService.siteCameraCtrl(confVO);
        AdaptHttpStateUtil.dealAdaptHttpStatus(object);
    }

    @Override
    public void setCommonlyUsedParticipants(String conferenceId, SetCommonlyUsedParticipantsRequest request, String token) throws MyHttpException {

    }

    @Override
    public String getCommonlyUsedParticipants(String conferenceId, GetCommonlyUsedParticipantsRequest request, String token) throws MyHttpException {
        return null;
    }

    @Override
    public void delParticipants(String conferenceId, List<String> request, String token) throws MyHttpException {
        ConfVO confVO = new ConfVO();
        confVO.setToken(token);
        confVO.setConferenceId(conferenceId);
        confVO.setData(request);
        ResponseEntityEx<?> object = smcConferenceScheduledService.deleteSitesInScheduleConf(confVO);
        AdaptHttpStateUtil.dealAdaptHttpStatus(object);
    }

    @Override
    public void subscribeParticipantsStatus(String conferenceId, String groupId, SubscribeParticipantsStatusRequest request, String token) throws MyHttpException {

    }

    @Override
    public void unSubscribeParticipantsStatus(String conferenceId, String groupId, String token) throws MyHttpException {

    }

    @Override
    public void subscribeParticipantsStatusRealTime(String conferenceId, String groupId, List<String> request, String token) throws MyHttpException {

    }

    @Override
    public void unSubscribeParticipantsStatusRealTime(String conferenceId, String groupId, String token) throws MyHttpException {

    }

    @Override
    public void setRemind(String conferenceId, String participantId, String token) throws MyHttpException {

    }

    @Override
    public void setChairmanPoll(String conferenceId, SetChairmanPollRequest request, String token) throws MyHttpException {

    }

    @Override
    public String getChairmanPoll(String conferenceId, String token) throws MyHttpException {
        return null;
    }

    @Override
    public void setBroadcastPoll(String conferenceId, SetBroadcastPollRequest request, String token) throws MyHttpException {

    }

    @Override
    public String getBroadcastPoll(String conferenceId, String token) throws MyHttpException {
        return null;
    }

    @Override
    public void setMultiPicPoll(String conferenceId, SetMultiPicPollRequest request, String token) throws MyHttpException {

    }

    @Override
    public String getMultiPicPoll(String conferenceId, String token) throws MyHttpException {
        return null;
    }

    @Override
    public String setParticipantsParameter(String conferenceId, String participantId, SetParticipantsParameterRequest request, String token) throws MyHttpException {
        return null;
    }

    @Override
    public void migrate(String conferenceId, MigrateRequest request, String token) throws MyHttpException {
        MigrateInfosReq migrateInfosReq = JSON.parseObject(JSON.toJSONString(request), MigrateInfosReq.class);
        ConfVO confVO = new ConfVO();
        confVO.setToken(token);
        confVO.setConferenceId(conferenceId);
        confVO.setData(migrateInfosReq);
        ErrorInfo errorInfo = smcBathVenueControlService.migrate(confVO);
        if(errorInfo != null){
            throw new MyHttpException(errorInfo.getHttpCode(), JSON.toJSONString(errorInfo));
        }
    }

    @Override
    public String getVideoSource(String conferenceId, List<String> request, String token) throws MyHttpException {
        return null;
    }

    @Override
    public void rseStream(String conferenceId, RseStreamRequest request, String token) throws MyHttpException {

    }

    @Override
    public void batchTextTips(String conferenceId, BatchTextTipsRequest request, String token) throws MyHttpException {
        ParticipantsBanner participantsBanner = JSON.parseObject(JSON.toJSONString(request), ParticipantsBanner.class);
        ErrorInfo errorInfo = smcMeetingBannerService.setParticipantsBanner(conferenceId, participantsBanner, token);
        if(errorInfo != null){
            throw new MyHttpException(errorInfo.getHttpCode(), JSON.toJSONString(errorInfo));
        }
    }

    @Override
    public void updateSubscribeParticipantsStatus(String conferenceId, String groupId, UpdateSubscribeParticipantsStatusRequest request, String token) throws MyHttpException {

    }

    @Override
    public void pushAiCaption(String conferenceId, String request, String token) throws MyHttpException {

    }

    @Override
    public String getPresetParam(String conferenceId, String token) throws MyHttpException {
        return null;
    }

    @Override
    public void quickHangup(String uri, String smcToken) throws MyHttpException {
        ConfVO confVO = new ConfVO();
        confVO.setToken(smcToken);
        confVO.setData(uri);
        ResponseEntityEx<?> object = smcConferenceScheduledService.quickHangup(confVO);
        AdaptHttpStateUtil.dealAdaptHttpStatus(object);
    }

    @Override
    public String callInfo(String uri, String smcToken) throws MyHttpException {
        ConfVO confVO = new ConfVO();
        confVO.setToken(smcToken);
        confVO.setData(uri);
        ResponseEntityEx<?> object = smcConferenceScheduledService.queryCallInfo(confVO);
        return AdaptHttpStateUtil.dealAdaptHttpStatus(object);
    }

    @Override
    public void changeSiteName(String conferenceId, com.suntek.vdm.gw.smc.request.meeting.control.ParticipantUpdateDto participantUpdateDto, String smcToken) throws MyHttpException {
        ModifySiteDisplayNameRequest modifySiteDisplayNameRequest = new ModifySiteDisplayNameRequest();
        modifySiteDisplayNameRequest.setName(participantUpdateDto.getParticipantNameInfo().getName());
        ErrorInfo errorInfo = smcVenueControlService.modifySiteDisplayName(conferenceId,participantUpdateDto.getParticipantNameInfo().getId(), modifySiteDisplayNameRequest, smcToken);
        if(errorInfo != null){
            throw new MyHttpException(errorInfo.getHttpCode(), JSON.toJSONString(errorInfo));
        }
    }
}
