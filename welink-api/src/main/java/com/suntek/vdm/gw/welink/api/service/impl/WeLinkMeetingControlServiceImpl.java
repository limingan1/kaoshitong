package com.suntek.vdm.gw.welink.api.service.impl;

import com.alibaba.fastjson.JSON;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.service.HttpService;
import com.suntek.vdm.gw.welink.api.pojo.RestMixedPictureBody;
import com.suntek.vdm.gw.welink.api.pojo.RestSwitchModeReqBody;
import com.suntek.vdm.gw.welink.api.request.*;
import com.suntek.vdm.gw.welink.api.response.GetMeetingControlTokenResponse;
import com.suntek.vdm.gw.welink.api.response.GetMeetingRealTimeInfoResponse;
import com.suntek.vdm.gw.welink.api.service.WeLinkMeetingControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class WeLinkMeetingControlServiceImpl extends WeLinkBaseServiceImpl implements WeLinkMeetingControlService {
    @Autowired
    @Qualifier("weLinkHttpServiceImpl")
    private HttpService httpService;

    /**
     * 添加会场
     *
     * @param conferenceID
     * @param request
     * @param conferenceToken
     * @throws MyHttpException
     */
    public void addParticipants(String conferenceID, AddParticipantsRequest request, String conferenceToken) throws MyHttpException {
        String response = httpService.post(String.format("/mmc/control/conferences/participants?conferenceID=%s", conferenceID), request, conferenceTokenHandle(conferenceToken)).getBody();
    }

    /**
     * 挂断会场
     *
     * @param conferenceID
     * @param request
     * @param conferenceToken
     * @throws MyHttpException
     */
    public void delParticipants(String conferenceID, DelParticipantsRequest request, String conferenceToken) throws MyHttpException {
        String response = httpService.post(String.format("/mmc/control/conferences/participants/delete?conferenceID=%s", conferenceID), request, conferenceTokenHandle(conferenceToken)).getBody();
    }

    /**
     * 删除与会者
     *
     * @param conferenceID
     * @param request
     * @param conferenceToken
     * @throws MyHttpException
     */
    public void delAttendees(String conferenceID, DelAttendeesRequest request, String conferenceToken) throws MyHttpException {
        String response = httpService.post(String.format("/mmc/control/conferences/attendees/delete?conferenceID=%s", conferenceID), request, conferenceTokenHandle(conferenceToken)).getBody();
    }


    /**
     * 获取会控 Token
     *
     * @param conferenceID
     * @param X_Password
     * @param X_Login_Type
     * @param conferenceToken
     * @return
     * @throws MyHttpException
     */
    public GetMeetingControlTokenResponse getMeetingControlToken(String conferenceID, String X_Password, String X_Login_Type, String conferenceToken) throws MyHttpException {
        String response = httpService.get(String.format("/mmc/control/conferences/token?conferenceID=%s&X-Password=%s&X-Login-Type=%s", conferenceID, X_Password, X_Login_Type), null, conferenceTokenHandle(conferenceToken)).getBody();
        return JSON.parseObject(response, GetMeetingControlTokenResponse.class);
    }

    /**
     * 查询会议实时信息
     *
     * @param conferenceID
     * @param conferenceToken
     * @return
     * @throws MyHttpException
     */
    public GetMeetingRealTimeInfoResponse getMeetingRealTimeInfo(String conferenceID, String conferenceToken) throws MyHttpException {
        String response = httpService.get(String.format("/mmc/control/conferences/realTimeInfo?conferenceID=%s", conferenceID), null, conferenceTokenHandle(conferenceToken)).getBody();
        return JSON.parseObject(response, GetMeetingRealTimeInfoResponse.class);
    }

    /**
     * 重命名会场
     *
     * @param conferenceID
     * @param request
     * @param conferenceToken
     * @throws MyHttpException
     */
    public void renameNamParticipants(String conferenceID, RenameNamParticipantsRequest request, String conferenceToken) throws MyHttpException {
        String response = httpService.put(String.format("/mmc/control/conferences/participants/name?conferenceID=%s", conferenceID), request, conferenceTokenHandle(conferenceToken)).getBody();
    }

    /**
     * 申请/释放主持人
     *
     * @param conferenceID
     * @param participantID
     * @param request
     * @param conferenceToken
     * @throws MyHttpException
     */
    public void setOrCancelChair(String conferenceID, String participantID, SetOrCancelChairRequest request, String conferenceToken) throws MyHttpException {
        String response = httpService.put(String.format("/mmc/control/conferences/participants/role?conferenceID=%s&participantID=%s", conferenceID, participantID), request, conferenceTokenHandle(conferenceToken)).getBody();
    }

    /**
     * 静音/取消静音
     *
     * @param conferenceID
     * @param participantID
     * @param request
     * @param conferenceToken
     * @throws MyHttpException
     */
    public void setMute(String conferenceID, String participantID, SetMuteRequest request, String conferenceToken) throws MyHttpException {
        String response = httpService.put(String.format("/mmc/control/conferences/participants/mute?conferenceID=%s&participantID=%s", conferenceID, participantID), request, conferenceTokenHandle(conferenceToken)).getBody();
    }

    /**
     * 全场静音/取消全场静音
     *
     * @param conferenceID
     * @param request
     * @param conferenceToken
     * @throws MyHttpException
     */
    public void setMuteAll(String conferenceID, SetMuteRequest request, String conferenceToken) throws MyHttpException {
        String response = httpService.put(String.format("/mmc/control/conferences/mute?conferenceID=%s", conferenceID), request, conferenceTokenHandle(conferenceToken)).getBody();
    }

    /**
     * 延长会议
     *
     * @param conferenceID
     * @param request
     * @param conferenceToken
     * @throws MyHttpException
     */
    public void durationMeeting(String conferenceID, DurationMeetingRequest request, String conferenceToken) throws MyHttpException {
        String response = httpService.put(String.format("/mmc/control/conferences/duration?conferenceID=%s", conferenceID), request, conferenceTokenHandle(conferenceToken)).getBody();
    }

    /**
     * 结束会议
     *
     * @param conferenceID
     * @param request
     * @param conferenceToken
     * @throws MyHttpException
     */
    public void stopMeeting(String conferenceID, StopMeetingRequest request, String conferenceToken) throws MyHttpException {
        String response = httpService.put(String.format("/mmc/control/conferences/stop?conferenceID=%s", conferenceID), request, conferenceTokenHandle(conferenceToken)).getBody();
    }

    /**
     * 主持人选看视频画面
     *
     * @param conferenceID
     * @param request
     * @param conferenceToken
     * @throws MyHttpException
     */
    public void setChairView(String conferenceID, SetChairViewRequest request, String conferenceToken) throws MyHttpException {
        String response = httpService.put(String.format("/mmc/control/conferences/chairView?conferenceID=%s", conferenceID), request, conferenceTokenHandle(conferenceToken)).getBody();
    }

    /**
     * 点名会场
     *
     * @param conferenceID
     * @param participantID
     * @param conferenceToken
     * @throws MyHttpException
     */
    public void rollCallParticipants(String conferenceID, String participantID, String conferenceToken) throws MyHttpException {
        String response = httpService.put(String.format("/mmc/control/conferences/participants/rollCall?conferenceID=%s&participantID=%s", conferenceID, participantID), null, conferenceTokenHandle(conferenceToken)).getBody();
    }

    /**
     * 广播会场
     *
     * @param conferenceID
     * @param participantID
     * @param conferenceToken
     * @throws MyHttpException
     */
    public String broadcastParticipants(String conferenceID, String participantID, String conferenceToken) throws MyHttpException {
        String response = httpService.put(String.format("/mmc/control/conferences/participants/broadcast?conferenceID=%s&participantID=%s", conferenceID, participantID), null, conferenceTokenHandle(conferenceToken)).getBody();
        return response;
    }

    /**
     * 锁定或者解锁某在线会场的视频源
     *
     * @param conferenceID
     * @param participantID
     * @param request
     * @param conferenceToken
     * @throws MyHttpException
     */
    public void lockViewParticipants(String conferenceID, String participantID, LockViewParticipantsRequest request, String conferenceToken) throws MyHttpException {
        String response = httpService.put(String.format("/mmc/control/conferences/lockView?conferenceID=%s&participantID=%s", conferenceID, participantID), request, conferenceTokenHandle(conferenceToken)).getBody();
    }

    /**
     * 来宾选看会场
     *
     * @param conferenceID
     * @param participantID
     * @param request
     * @param conferenceToken
     * @throws MyHttpException
     */
    public void partViewParticipants(String conferenceID, String participantID, PartViewParticipantsRequest request, String conferenceToken) throws MyHttpException {
        String response = httpService.put(String.format("/mmc/control/conferences/participants/partView?conferenceID=%s&participantID=%s", conferenceID, participantID), request, conferenceTokenHandle(conferenceToken)).getBody();
    }


    /**
     * 切换会议显示策略
     *
     * @param conferenceID
     * @param request
     * @param conferenceToken
     * @throws MyHttpException
     */
    public void displayMode(String conferenceID, RestSwitchModeReqBody request, String conferenceToken) throws MyHttpException {
        String response = httpService.put(String.format("/mmc/control/conferences/display/mode?conferenceID=%s", conferenceID), request, conferenceTokenHandle(conferenceToken)).getBody();
    }

    /**
     * 设置多画面
     *
     * @param conferenceID
     * @param request
     * @param conferenceToken
     * @throws MyHttpException
     */
    public void displayMultiPicture(String conferenceID, RestMixedPictureBody request, String conferenceToken) throws MyHttpException {
        String response = httpService.put(String.format("/mmc/control/conferences/display/customMultiPicture?conferenceID=%s", conferenceID), request, conferenceTokenHandle(conferenceToken)).getBody();
    }

}
