package com.suntek.vdm.gw.welink.api.service;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.welink.api.pojo.RestMixedPictureBody;
import com.suntek.vdm.gw.welink.api.pojo.RestSwitchModeReqBody;
import com.suntek.vdm.gw.welink.api.request.*;
import com.suntek.vdm.gw.welink.api.response.GetMeetingControlTokenResponse;
import com.suntek.vdm.gw.welink.api.response.GetMeetingRealTimeInfoResponse;


public interface WeLinkMeetingControlService {
    /**
     * 添加会场
     *
     * @param conferenceID
     * @param request
     * @param conferenceToken
     * @throws MyHttpException
     */
     void addParticipants(String conferenceID, AddParticipantsRequest request, String conferenceToken) throws MyHttpException ;


    /**
     * 删除会场
     *
     * @param conferenceID
     * @param request
     * @param conferenceToken
     * @throws MyHttpException
     */
     void delParticipants(String conferenceID, DelParticipantsRequest request, String conferenceToken) throws MyHttpException ;
 

    /**
     * 删除与会者
     *
     * @param conferenceID
     * @param request
     * @param conferenceToken
     * @throws MyHttpException
     */
     void delAttendees(String conferenceID, DelAttendeesRequest request, String conferenceToken) throws MyHttpException ;
  


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
     GetMeetingControlTokenResponse getMeetingControlToken(String conferenceID, String X_Password, String X_Login_Type, String conferenceToken) throws MyHttpException ;


    /**
     * 查询会议实时信息
     *
     * @param conferenceID
     * @param conferenceToken
     * @return
     * @throws MyHttpException
     */
     GetMeetingRealTimeInfoResponse getMeetingRealTimeInfo(String conferenceID, String conferenceToken) throws MyHttpException ;


    /**
     * 重命名会场
     *
     * @param conferenceID
     * @param request
     * @param conferenceToken
     * @throws MyHttpException
     */
     void renameNamParticipants(String conferenceID, RenameNamParticipantsRequest request, String conferenceToken) throws MyHttpException ;

    /**
     * 申请/释放主持人
     *
     * @param conferenceID
     * @param participantID
     * @param request
     * @param conferenceToken
     * @throws MyHttpException
     */
     void setOrCancelChair(String conferenceID, String participantID, SetOrCancelChairRequest request, String conferenceToken) throws MyHttpException ;


    /**
     * 静音/取消静音
     *
     * @param conferenceID
     * @param participantID
     * @param request
     * @param conferenceToken
     * @throws MyHttpException
     */
     void setMute(String conferenceID, String participantID, SetMuteRequest request, String conferenceToken) throws MyHttpException ;


    /**
     * 全场静音/取消全场静音
     *
     * @param conferenceID
     * @param request
     * @param conferenceToken
     * @throws MyHttpException
     */
     void setMuteAll(String conferenceID, SetMuteRequest request, String conferenceToken) throws MyHttpException ;
 

    /**
     * 延长会议
     *
     * @param conferenceID
     * @param request
     * @param conferenceToken
     * @throws MyHttpException
     */
     void durationMeeting(String conferenceID, DurationMeetingRequest request, String conferenceToken) throws MyHttpException ;
  

    /**
     * 结束会议
     *
     * @param conferenceID
     * @param request
     * @param conferenceToken
     * @throws MyHttpException
     */
     void stopMeeting(String conferenceID, StopMeetingRequest request, String conferenceToken) throws MyHttpException ;
 

    /**
     * 主持人选看视频画面
     *
     * @param conferenceID
     * @param request
     * @param conferenceToken
     * @throws MyHttpException
     */
     void setChairView(String conferenceID, SetChairViewRequest request, String conferenceToken) throws MyHttpException ;


    /**
     * 点名会场
     *
     * @param conferenceID
     * @param participantID
     * @param conferenceToken
     * @throws MyHttpException
     */
     void rollCallParticipants(String conferenceID, String participantID, String conferenceToken) throws MyHttpException ;


    /**
     * 广播会场
     *
     * @param conferenceID
     * @param participantID
     * @param conferenceToken
     * @throws MyHttpException
     */
     String broadcastParticipants(String conferenceID, String participantID, String conferenceToken) throws MyHttpException ;


    /**
     * 锁定或者解锁某在线会场的视频源
     *
     * @param conferenceID
     * @param participantID
     * @param request
     * @param conferenceToken
     * @throws MyHttpException
     */
     void lockViewParticipants(String conferenceID, String participantID, LockViewParticipantsRequest request, String conferenceToken) throws MyHttpException ;


    /**
     * 来宾选看会场
     *
     * @param conferenceID
     * @param participantID
     * @param request
     * @param conferenceToken
     * @throws MyHttpException
     */
     void partViewParticipants(String conferenceID, String participantID, PartViewParticipantsRequest request, String conferenceToken) throws MyHttpException ;


    /**
     * 切换会议显示策略
     *
     * @param conferenceID
     * @param request
     * @param conferenceToken
     * @throws MyHttpException
     */
     void displayMode(String conferenceID, RestSwitchModeReqBody request, String conferenceToken) throws MyHttpException ;


    /**
     * 设置多画面
     *
     * @param conferenceID
     * @param request
     * @param conferenceToken
     * @throws MyHttpException
     */
     void displayMultiPicture(String conferenceID, RestMixedPictureBody request, String conferenceToken) throws MyHttpException ;

}
