package com.suntek.vdm.gw.smc.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.suntek.vdm.gw.common.api.request.MeetingControlRequest;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.GetParticipantsDetailInfoResponse;
import com.suntek.vdm.gw.common.pojo.ParticipantDetail;
import com.suntek.vdm.gw.common.pojo.ParticipantGeneralParam;
import com.suntek.vdm.gw.common.pojo.request.GetParticipantsRequest;
import com.suntek.vdm.gw.common.pojo.request.meeting.DurationMeetingRequest;
import com.suntek.vdm.gw.common.pojo.request.meeting.ParticipantsControlRequest;
import com.suntek.vdm.gw.common.pojo.response.meeting.GetMeetingDetailResponse;
import com.suntek.vdm.gw.common.pojo.response.meeting.GetParticipantsResponse;
import com.suntek.vdm.gw.common.service.HttpService;
import com.suntek.vdm.gw.common.util.SystemConfiguration;
import com.suntek.vdm.gw.smc.adaptService.AdaptMeetingControlService;
import com.suntek.vdm.gw.smc.pojo.AttendeeReq;
import com.suntek.vdm.gw.smc.pojo.CallInfoRsp;
import com.suntek.vdm.gw.common.pojo.ParticipantReq;
import com.suntek.vdm.gw.smc.pojo.VideoSrcInfo;
import com.suntek.vdm.gw.smc.request.meeting.control.*;
import com.suntek.vdm.gw.smc.response.meeting.control.*;
import com.suntek.vdm.gw.smc.service.SmcMeetingControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SmcMeetingControlServiceImpl  extends SmcBaseServiceImpl implements SmcMeetingControlService{
    @Value("${useAdapt}")
    private Boolean useAdapt;

    @Autowired
    AdaptMeetingControlService adaptMeetingControlService;

    @Autowired
    @Qualifier("smcHttpServiceImpl")
    private HttpService httpService;

    /**
     *  获取会议详情
     * @param conferenceId
     * @param token
     * @return
     */
    @Override
    public GetMeetingDetailResponse getMeetingDetail(String conferenceId, String token, String isQueryMultiPicInfo)  throws MyHttpException {
        MultiValueMap<String, String> headers= tokenHandle(token);
        if(isQueryMultiPicInfo != null) {
            headers.set("isQueryMultiPicInfo", isQueryMultiPicInfo);
        }
        String response = null;
        if(useAdapt){
            response = adaptMeetingControlService.getMeetingDetail(conferenceId, token, isQueryMultiPicInfo);
        }else {
            response = httpService.get("/online/conferences/"+conferenceId+"/detail", null, headers).getBody();
        }
        return JSON.parseObject(response, GetMeetingDetailResponse.class);
    }

    /**
     * 设置单会场字幕
     * @param conferenceId
     * @param participantId
     * @param request
     * @param token
     * @return
     */
    @Override
    public void setTextTips(String conferenceId, String participantId, SetTextTipsRequest request, String token)  throws MyHttpException {
        if(useAdapt){
            adaptMeetingControlService.setTextTips(conferenceId, participantId, request, token);
        }else {
            httpService.post("/online/conferences/"+conferenceId+"/participants/"+participantId+"/textTips", request, tokenHandle(token)).getBody();
        }
        
    }

    /**
     * 会议控制
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    @Override
    public void meetingControl(String conferenceId, MeetingControlRequest request, String token)  throws MyHttpException {
        if(useAdapt){
            adaptMeetingControlService.meetingControl(conferenceId, request, token);
        }else {
            httpService.patch("/online/conferences/"+conferenceId+"/status", request, tokenHandle(token)).getBody();
        }
    }

    /**
     *  除部分会场外全体静音
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    @Override
    public void chatMic(String conferenceId, ChatMicRequest request, String token)  throws MyHttpException {
        if(useAdapt){
            adaptMeetingControlService.chatMic(conferenceId, request, token);
        }else {
            httpService.post("/online/conferences/"+conferenceId+"/chat/mic", request, tokenHandle(token)).getBody();
        }
        
    }

    /**
     * 除部分会场外全体关闭扬声器
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    @Override
    public void chatSpeaker(String conferenceId, ChatSpeakerRequest request, String token)  throws MyHttpException {
        if(useAdapt){
            adaptMeetingControlService.chatSpeaker(conferenceId, request, token);
        }else {
            httpService.post("/online/conferences/"+conferenceId+"/chat/speaker", request, tokenHandle(token)).getBody();
        }
        
    }

    /**
     * 设置会议横幅字幕
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    @Override
    public void setTextTips(String conferenceId, SetTextTipsRequest request, String token)  throws MyHttpException {
        if(useAdapt){
            adaptMeetingControlService.setTextTips(conferenceId, request, token);
        }else {
            httpService.post("/online/conferences/"+conferenceId+"/textTips", request, tokenHandle(token)).getBody();
        }
        
    }

    /**
     * 延长会议
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    @Override
    public void duration(String conferenceId, DurationMeetingRequest request, String token)  throws MyHttpException {
        if(useAdapt){
            adaptMeetingControlService.duration(conferenceId, request, token);
        }else {
            httpService.put("/online/conferences/"+conferenceId+"/duration", request, tokenHandle(token)).getBody();
        }
        
    }

    /**
     * 结束会议
     * @param conferenceId
     * @param token
     * @return
     */
    @Override
    public void delMeeting(String conferenceId, String token)  throws MyHttpException {
        if(useAdapt){
            adaptMeetingControlService.delMeeting(conferenceId, token);
        }else {
            httpService.delete("/online/conferences/"+conferenceId, null, tokenHandle(token)).getBody();
        }
        
    }

    /**
     * 添加会场
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    @Override
    public void addParticipants(String conferenceId, List<ParticipantReq>  request, String token)  throws MyHttpException {
        if(useAdapt){
            adaptMeetingControlService.addParticipants(conferenceId, request, token);
        }else {
            httpService.post("/online/conferences/"+conferenceId+"/participants", request, tokenHandle(token)).getBody();
        }
        
    }

    /**
     * 添加与会人
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    @Override
    public void addAttendees(String conferenceId, List<AttendeeReq> request, String token)  throws MyHttpException {
        if (SystemConfiguration.smcVersionIsV2()) {
            //2.0添加与会人转为添加会场
            addParticipants(conferenceId, request.stream().map(AttendeeReq::toParticipantReq).collect(Collectors.toList()), token);
            return;
        }
        if(useAdapt){
            adaptMeetingControlService.addAttendees(conferenceId, request, token);
        }else {
            httpService.post("/online/conferences/"+conferenceId+"/attendees", request, tokenHandle(token)).getBody();
        }
        
    }

    /**
     * 获取会场列表
     * @param conferenceId
     * @param page
     * @param size
     * @param request
     * @param token
     * @return
     */
    @Override
    public GetParticipantsResponse getParticipants(String conferenceId, int page, int size, GetParticipantsRequest request, String token)  throws MyHttpException {
        String response = null;
        GetParticipantsResponse result;
        if(useAdapt){
            response = adaptMeetingControlService.getParticipants(conferenceId, page, size, request, token);
            result = JSON.parseObject(response, GetParticipantsResponse.class);
            transferParticipantData(result.getContent());
        }else {
            response = httpService.post("/online/conferences/"+conferenceId+"/participants/conditions?page="+page+"&size="+size, request, tokenHandle(token)).getBody();
            result = JSON.parseObject(response, GetParticipantsResponse.class);
        }
        return result;
    }

    private void transferParticipantData(List<ParticipantDetail> content) {
        for (ParticipantDetail item : content) {
            ParticipantGeneralParam generalParam = item.getGeneralParam();
            String encodeType = generalParam.getEncodeType();
            if ("0".equals(encodeType)) {
                generalParam.setEncodeType("ENCODE");
            } else if ("1".equals(encodeType)) {
                generalParam.setEncodeType("DECODE");
            } else if ("2".equals(encodeType)) {
                generalParam.setEncodeType("ENCODE_DECODE");
            }
        }
    }

    /**
     * 批量获取会场列表
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    @Override
    public List<GetParticipantsResponse> getParticipantsBriefs(String conferenceId, List<String> request, String token)  throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingControlService.getParticipantsBriefs(conferenceId, request, token);
        }else {
            response = httpService.post("/online/conferences/"+conferenceId+"/participants/briefs", request, tokenHandle(token)).getBody();
        }
       return JSON.parseObject(response, new TypeReference<List<GetParticipantsResponse>>() {});
    }

    /**
     * 查询会场详情
     * @param conferenceId
     * @param participantId
     * @param token
     * @return
     */
    @Override
    public GetParticipantsDetailInfoResponse getParticipantsDetailInfo(String conferenceId, String participantId, String token)  throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingControlService.getParticipantsDetailInfo(conferenceId, participantId, token);
        }else {
            response = httpService.get("/online/conferences/"+conferenceId+"/participants/"+participantId+"/detailInfo", null, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, GetParticipantsDetailInfoResponse.class);
    }

    /**
     * 查询会场视音频能力
     * @param conferenceId
     * @param participantId
     * @param token
     * @return
     */

    @Override
    public GetParticipantsCapabilityResponse getParticipantsCapability(String conferenceId, String participantId, String token)  throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingControlService.getParticipantsCapability(conferenceId, participantId, token);
        }else {
            response = httpService.get("/online/conferences/"+conferenceId+"/participants/"+participantId+"/capability", null, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, GetParticipantsCapabilityResponse.class);
    }

    /**
     * 会场控制
     * @param conferenceId
     * @param participantId
     * @param request
     * @param token
     * @return
     */

    @Override
    public String participantsControl(String conferenceId, String participantId, ParticipantsControlRequest request, String token)  throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingControlService.participantsControl(conferenceId, participantId, request, token);
        }else {
            response = httpService.patch("/online/conferences/"+conferenceId+"/participants/"+participantId+"/status", request, tokenHandle(token)).getBody();
        }
        return response;
    }

    /**
     * 批量会场控制
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    @Override
    public void participantsControl(String conferenceId, List<ParticipantsControlRequest> request, String token)  throws MyHttpException {
        if(useAdapt){
            adaptMeetingControlService.participantsControl(conferenceId, request, token);
        }else {
            httpService.patch("/online/conferences/"+conferenceId+"/participants/status", request, tokenHandle(token)).getBody();
        }
        
    }

    /**
     *  设置会场视频源跟随
     * @param conferenceId
     * @param participantId
     * @param request
     * @param token
     * @return
     */
    @Override
    public void participantsFellow(String conferenceId, String participantId, ParticipantsFellowRequest request, String token)  throws MyHttpException {
        if(useAdapt){
            adaptMeetingControlService.participantsFellow(conferenceId, participantId, request, token);
        }else {
            httpService.post("/online/conferences/"+conferenceId+"/participants/"+participantId+"/fellow", request, tokenHandle(token)).getBody();
        }
        
    }

    /**
     * 摄像机控制
     * @param conferenceId
     * @param participantId
     * @param request
     * @param token
     * @return
     */
    @Override
    public void cameraControl(String conferenceId, String participantId, CameraControlRequest request, String token)  throws MyHttpException {
        if(useAdapt){
            adaptMeetingControlService.cameraControl(conferenceId, participantId, request, token);
        }else {
            httpService.post("/online/conferences/"+conferenceId+"/participants/"+participantId+"/camera", request, tokenHandle(token)).getBody();
        }
        
    }

    /**
     * 设置常用会场
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    @Override
    public void setCommonlyUsedParticipants(String conferenceId, SetCommonlyUsedParticipantsRequest request, String token)  throws MyHttpException {
        if(useAdapt){
            adaptMeetingControlService.setCommonlyUsedParticipants(conferenceId, request, token);
        }else {
            httpService.put("/online/conferences/"+conferenceId+"/participants/order", request, tokenHandle(token)).getBody();
        }
        
    }

    /**
     * 查询常用会场列表
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    @Override
    public GetCommonlyUsedParticipantsResponse getCommonlyUsedParticipants(String conferenceId, GetCommonlyUsedParticipantsRequest request, String token)  throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingControlService.getCommonlyUsedParticipants(conferenceId, request, token);
        }else {
            response = httpService.post("/online/conferences/"+conferenceId+"/participants/order", request, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, GetCommonlyUsedParticipantsResponse.class);
    }

    /**
     *  删除会场
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    @Override
    public void delParticipants(String conferenceId, List<String> request, String token)  throws MyHttpException {
        if(useAdapt){
            adaptMeetingControlService.delParticipants(conferenceId, request, token);
        }else {
            httpService.delete("/online/conferences/"+conferenceId+"/participants", request, tokenHandle(token)).getBody();
        }
        
    }

    /**
     * 订阅会场状态
     * @param conferenceId
     * @param groupId
     * @param request
     * @param token
     * @return
     */
    @Override
    public void subscribeParticipantsStatus(String conferenceId, String groupId, SubscribeParticipantsStatusRequest request, String token)  throws MyHttpException {
        if(useAdapt){
            adaptMeetingControlService.subscribeParticipantsStatus(conferenceId, groupId, request, token);
        }else {
            httpService.post("/online/conferences/"+conferenceId+"/participants/groups/"+groupId, request, tokenHandle(token)).getBody();
        }
        
    }

    /**
     * 取消订阅会场状态
     * @param conferenceId
     * @param groupId
     * @param token
     * @return
     */
    @Override
    public void unSubscribeParticipantsStatus(String conferenceId, String groupId, String token)  throws MyHttpException {
        if(useAdapt){
            adaptMeetingControlService.unSubscribeParticipantsStatus(conferenceId, groupId, token);
        }else {
            httpService.delete("/online/conferences/"+conferenceId+"/participants/groups/"+groupId, null, tokenHandle(token)).getBody();
        }
        
    }

    /**
     * 订阅会场实时状态
     * @param conferenceId
     * @param groupId
     * @param request
     * @param token
     * @return
     */
    @Override
    public void subscribeParticipantsStatusRealTime(String conferenceId, String groupId, List<String> request, String token)  throws MyHttpException {
        if(useAdapt){
            adaptMeetingControlService.subscribeParticipantsStatusRealTime(conferenceId, groupId, request, token);
        }else {
            httpService.post("/online/conferences/"+conferenceId+"/participants/groups/"+groupId+"/realTimeInfo", request, tokenHandle(token)).getBody();
        }
        
    }

    /**
     *  取消订阅会场实时状态
     * @param conferenceId
     * @param groupId
     * @param token
     * @return
     */
    @Override
    public void unSubscribeParticipantsStatusRealTime(String conferenceId, String groupId, String token)  throws MyHttpException {
        if(useAdapt){
            adaptMeetingControlService.unSubscribeParticipantsStatusRealTime(conferenceId, groupId, token);
        }else {
            httpService.delete("/online/conferences/"+conferenceId+"/participants/groups/"+groupId+"/realTimeInfo", null, tokenHandle(token)).getBody();
        }
        
    }

    /**
     * 会场点名前提示
     * @param conferenceId
     * @param participantId
     * @param token
     * @return
     */
    @Override
    public void setRemind(String conferenceId, String participantId, String token)  throws MyHttpException {
        if(useAdapt){
            adaptMeetingControlService.setRemind(conferenceId, participantId, token);
        }else {
            httpService.post("/online/conferences/"+conferenceId+"/participants/"+participantId+"/remind", null, tokenHandle(token)).getBody();
        }
        
    }

    /**
     * 设置主席轮询
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    @Override
    public void setChairmanPoll(String conferenceId, SetChairmanPollRequest request, String token)  throws MyHttpException {
        if(useAdapt){
            adaptMeetingControlService.setChairmanPoll(conferenceId, request, token);
        }else {
            httpService.post("/online/conferences/"+conferenceId+"/participants/chairmanPoll", request, tokenHandle(token)).getBody();
        }
        
    }

    /**
     * 获取主席轮询设置
     * @param conferenceId
     * @param token
     * @return
     */
    @Override
    public GetChairmanPollResponse getChairmanPoll(String conferenceId, String token)  throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingControlService.getChairmanPoll(conferenceId, token);
        }else {
            response = httpService.get("/online/conferences/"+conferenceId+"/participants/chairmanPoll", null, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, GetChairmanPollResponse.class);
    }

    /**
     * 设置定时广播
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    @Override
    public void setBroadcastPoll(String conferenceId, SetBroadcastPollRequest request, String token)  throws MyHttpException {
        if(useAdapt){
            adaptMeetingControlService.setBroadcastPoll(conferenceId, request, token);
        }else {
            httpService.post("/online/conferences/"+conferenceId+"/broadcastPoll", request, tokenHandle(token)).getBody();
        }
        
    }

    /**
     * 获取定时广播设置
     * @param conferenceId
     * @param token
     * @return
     */
    @Override
    public GetBroadcastPollResponse getBroadcastPoll(String conferenceId, String token)  throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingControlService.getBroadcastPoll(conferenceId, token);
        }else {
            response = httpService.get("/online/conferences/"+conferenceId+"/broadcastPoll", null, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, GetBroadcastPollResponse.class);
    }

    /**
     * 设置多画面轮询
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    @Override
    public void setMultiPicPoll(String conferenceId, SetMultiPicPollRequest request, String token)  throws MyHttpException {
        if(useAdapt){
            adaptMeetingControlService.setMultiPicPoll(conferenceId, request, token);
        }else {
            httpService.post("/online/conferences/"+conferenceId+"/multiPicPoll", request, tokenHandle(token)).getBody();
        }
        
    }

    /**
     * 获取多画面轮询设置
     * @param conferenceId
     * @param token
     * @return
     */
    @Override
    public GetMultiPicPollResponse getMultiPicPoll(String conferenceId, String token)  throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingControlService.getMultiPicPoll(conferenceId, token);
        }else {
            response = httpService.get("/online/conferences/"+conferenceId+"/multiPicPoll", null, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, GetMultiPicPollResponse.class);
    }

    /**
     * 提供会场设置二次拨号信息功能。
     * @param conferenceId
     * @param participantId
     * @param request
     * @param token
     * @return
     */
    @Override
    public SetParticipantsParameterResponse setParticipantsParameter(String conferenceId, String participantId, SetParticipantsParameterRequest request, String token)  throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingControlService.setParticipantsParameter(conferenceId, participantId,request, token);
        }else {
            response = httpService.post("/online/conferences/"+conferenceId+"/participants/"+participantId+"/parameter", request, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, SetParticipantsParameterResponse.class);
    }

    /**
     * 会场迁移
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    @Override
    public void migrate(String conferenceId, MigrateRequest request, String token)  throws MyHttpException {
        if(useAdapt){
            adaptMeetingControlService.migrate(conferenceId, request, token);
        }else {
            httpService.post("/online/conferences/"+conferenceId+"/migrate", request, tokenHandle(token)).getBody();
        }
        
    }

    /**
     * 查询会场视频源
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    @Override
    public List<VideoSrcInfo> getVideoSource(String conferenceId, List<String> request, String token)  throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingControlService.getVideoSource(conferenceId, request, token);
        }else {
            response = httpService.post("/online/conferences/"+conferenceId+"/participants/videoSource", request, tokenHandle(token)).getBody();
        }
       return JSON.parseObject(response, new TypeReference<List<VideoSrcInfo>>() {});
    }

    /**
     * 推流控制
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    @Override
    public void rseStream(String conferenceId, RseStreamRequest request, String token)  throws MyHttpException {
        if(useAdapt){
            adaptMeetingControlService.rseStream(conferenceId, request, token);
        }else {
            httpService.post("/online/conferences/"+conferenceId+"/rseStream", request, tokenHandle(token)).getBody();
        }
        
    }

    /**
     * 批量设置会场字幕
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    @Override
    public void batchTextTips(String conferenceId, BatchTextTipsRequest request, String token)  throws MyHttpException {
        if(useAdapt){
            adaptMeetingControlService.batchTextTips(conferenceId, request, token);
        }else {
            httpService.post("/online/conferences/"+conferenceId+"/participants/batchTextTips", request, tokenHandle(token)).getBody();
        }
        
    }

    /**
     * 更新订阅会场状态
     * @param conferenceId
     * @param groupId
     * @param request
     * @param token
     * @return
     */

    @Override
    public void updateSubscribeParticipantsStatus(String conferenceId, String groupId, UpdateSubscribeParticipantsStatusRequest request, String token)  throws MyHttpException {
        if(useAdapt){
            adaptMeetingControlService.updateSubscribeParticipantsStatus(conferenceId, groupId, request, token);
        }else {
            httpService.patch("/online/conferences/"+conferenceId+"/participants/groups/"+groupId, request, tokenHandle(token)).getBody();
        }
        
    }

    /**
     * 推送 AI 字幕
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */

    @Override
    public void pushAiCaption(String conferenceId, String request, String token)  throws MyHttpException {
        if(useAdapt){
            adaptMeetingControlService.pushAiCaption(conferenceId, request, token);
        }else {
            httpService.post("/online/conferences/"+conferenceId+"/participants/ai/caption", request, tokenHandle(token)).getBody();
        }
        
    }

    /**
     * 获取预置多画面
     * @param conferenceId
     * @param token
     * @return
     */
    @Override
    public GetPresetParamResponse getPresetParam(String conferenceId, String token)  throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingControlService.getPresetParam(conferenceId, token);
        }else {
            response = httpService.get("/online/conferences/"+conferenceId+"/presetParam", null, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, GetPresetParamResponse.class);
    }

    @Override
    public void quickHangup(String uri, String token) throws MyHttpException {
        if(useAdapt){
            adaptMeetingControlService.quickHangup(uri, token);
        }else {
            httpService.put("/conferences/quickHangup", uri, tokenHandle(token)).getBody();
        }


    }
    @Override
    public CallInfoRsp callInfo(String uri, String token) throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingControlService.callInfo(uri, token);
        }else {
            response = httpService.post("/conferences/callInfo", uri, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, CallInfoRsp.class);
    }

    @Override
    public void changeSiteName(String conferenceId, ParticipantUpdateDto participantUpdateDto, String smcToken) throws MyHttpException {
        if(useAdapt){
            adaptMeetingControlService.changeSiteName(conferenceId,participantUpdateDto, smcToken);
        }else {
            httpService.put("/online/conferences/"+conferenceId+"/participants/param", participantUpdateDto, tokenHandle(smcToken)).getBody();
        }

    }
}
