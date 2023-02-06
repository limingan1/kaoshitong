package com.suntek.vdm.gw.conf.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.suntek.vdm.gw.common.api.request.*;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.enums.*;
import com.suntek.vdm.gw.common.pojo.*;
import com.suntek.vdm.gw.common.pojo.node.GwNode;
import com.suntek.vdm.gw.common.pojo.request.AddCasChannelReq;
import com.suntek.vdm.gw.common.pojo.request.GetChannelStatusReq;
import com.suntek.vdm.gw.common.pojo.request.GetParticipantsRequest;
import com.suntek.vdm.gw.common.pojo.request.meeting.ParticipantsControlRequest;
import com.suntek.vdm.gw.common.pojo.response.AddCasChannelResp;
import com.suntek.vdm.gw.common.pojo.response.meeting.GetMeetingDetailResponse;
import com.suntek.vdm.gw.common.pojo.response.meeting.GetParticipantsResponse;
import com.suntek.vdm.gw.common.util.CommonHelper;
import com.suntek.vdm.gw.common.util.SystemConfiguration;
import com.suntek.vdm.gw.common.util.TransactionManage;
import com.suntek.vdm.gw.common.util.UtcTimeUtil;
import com.suntek.vdm.gw.conf.api.request.PullSourceRequest;
import com.suntek.vdm.gw.conf.api.request.ScheduleMeetingRequestEx;
import com.suntek.vdm.gw.conf.api.request.SetTextTipsRequestEx;
import com.suntek.vdm.gw.conf.pojo.ChildMeetingInfo;
import com.suntek.vdm.gw.conf.pojo.MeetingInfo;
import com.suntek.vdm.gw.conf.pojo.ParticipantInfo;
import com.suntek.vdm.gw.conf.pojo.WatchInfo;
import com.suntek.vdm.gw.conf.service.*;
import com.suntek.vdm.gw.core.customexception.BaseStateException;
import com.suntek.vdm.gw.core.entity.NodeData;
import com.suntek.vdm.gw.core.entity.VmNodeData;
import com.suntek.vdm.gw.core.enumeration.NodeBusinessType;
import com.suntek.vdm.gw.core.service.*;
import com.suntek.vdm.gw.smc.pojo.*;
import com.suntek.vdm.gw.smc.request.meeting.control.*;
import com.suntek.vdm.gw.smc.request.meeting.management.ScheduleMeetingRequest;
import com.suntek.vdm.gw.smc.response.meeting.control.*;
import com.suntek.vdm.gw.smc.service.SmcMeetingControlService;
import com.suntek.vdm.gw.welink.pojo.WelinkConference;
import com.suntek.vdm.gw.welink.service.impl.WelinkMeetingManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
public class MeetingControlServiceImpl extends BaseServiceImpl implements MeetingControlService {
    @Autowired
    private SmcMeetingControlService smcMeetingControlService;
    @Autowired
    private RemoteGwService remoteGwService;
    @Autowired
    private MeetingInfoManagerService meetingInfoManagerService;
    @Autowired
    private ParticipantInfoManagerService participantInfoManagerService;
    @Autowired
    private NodeDataService nodeDataService;
    @Autowired
    private CascadeChannelManageService cascadeChannelManageService;
    @Autowired
    private VideoSourceService videoSourceService;
    @Autowired
    private MeetingService meetingService;
    @Autowired
    private WelinkMeetingManagerService welinkMeetingManagerService;
    @Autowired
    private NodeManageService nodeManageService;
    @Autowired
    private SubscribeManageService subscribeManageService;
    @Autowired
    private ProxySubscribeConferencesService proxySubscribeConferencesService;

    @Autowired
    private RoutManageService routManageService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CascadeService cascadeService;
    @Autowired
    private VmNodeDataService vmNodeDataService;
    @Autowired
    private LocalTokenManageService localTokenManageService;

    @Override
    public void delMeeting(String conferenceId, String token, String confCasId, Boolean keepByCasState) throws MyHttpException {
        MeetingInfo meetingInfo = meetingInfoManagerService.get(conferenceId);
        if(meetingInfo == null){
            smcMeetingControlService.delMeeting(conferenceId, getSmcToken(token));
            return;
        }
        String mainConfCasId = meetingInfoManagerService.get(conferenceId).getConfCasId();
        //找不到会议缓存就直接结束本级会议
        if (mainConfCasId == null) {
            smcMeetingControlService.delMeeting(conferenceId, getSmcToken(token));
            return;
        }
        if (confCasId != null && !confCasId.equals(mainConfCasId)) {
            ChildMeetingInfo childMeetingInfo = meetingInfoManagerService.getChildByCasConfId(conferenceId, confCasId);
            if (childMeetingInfo == null) {
                log.warn("meetingInfo can not found in memory:{} {}", conferenceId, confCasId);
                //TODO 抛异常
                return;
            }
            remoteGwService.toByGwId(childMeetingInfo.getGwId()).delete(String.format(ConfApiUrl.DEL_MEETING.value(), childMeetingInfo.getId(), confCasId, keepByCasState), null);
            return;
        }
        //缓存中查询下级会议结束下级会议
        if (meetingInfoManagerService.contains(conferenceId)) {
            if (keepByCasState) {
                Map<String, ParticipantInfo> localCasParticipantDown = participantInfoManagerService.getLocalCasParticipant(conferenceId, CascadeParticipantDirection.DOWN);
                Set<String> delMap = new HashSet<>();
                for (ParticipantInfo participantInfo : localCasParticipantDown.values()) {
                    if (participantInfo.getOnline()) {
                        delMap.add(participantInfo.getUri());
                    }
                }
                for (ChildMeetingInfo childMeetingInfo : meetingInfoManagerService.get(conferenceId).getChildMeetingInfoMap().values()) {
                    if (!delMap.contains(childMeetingInfo.getConfCasId())) {
                        continue;
                    }
                    try {
                        remoteGwService.toByGwId(childMeetingInfo.getGwId()).delete(String.format(ConfApiUrl.DEL_MEETING.value(), childMeetingInfo.getId(), childMeetingInfo.getConfCasId(), keepByCasState), null);
                    } catch (Exception e) {
                        log.error("exception", e);
                    }
                }
            } else {
                for (ChildMeetingInfo item : meetingInfoManagerService.get(conferenceId).getChildMeetingInfoMap().values()) {
                    //结束下级会议
                    try {
                        remoteGwService.toByGwId(item.getGwId()).delete(String.format(ConfApiUrl.DEL_MEETING.value(), item.getId(), item.getConfCasId(), keepByCasState), null);
                    } catch (Exception e) {
                        log.error("exception", e);
                    }
                }
            }

            //TODO 其它清理 比如 websocket
            //清除本地会议结构
            meetingInfoManagerService.del(conferenceId);
        } else {
            log.warn("get conferences not found id:{}", conferenceId);
        }
        //后结束本级会议
        smcMeetingControlService.delMeeting(conferenceId, getSmcToken(token));
    }


    public void meetingControlBeforeFilter(String conferenceId, MeetingControlType meetingControlType, String pid, ConferenceState conferenceState, MeetingControlRequest request, String smcToken) throws MyHttpException {
        String conferenceStatePid = conferenceState.getByType(meetingControlType);
        //取消
        if (StringUtils.isEmpty(pid)) {
            //判断会议中有没有
            if (!StringUtils.isEmpty(conferenceStatePid)) {
                try {
                    if(MeetingControlType.PRESENTER.equals(meetingControlType)){
                        request.setId(conferenceStatePid);
                    }
                    smcMeetingControlService.meetingControl(conferenceId, request, smcToken);
                } catch (MyHttpException e) {

                }
            }
        } else {
            if (StringUtils.isEmpty(conferenceStatePid)) {
                smcMeetingControlService.meetingControl(conferenceId, request, smcToken);
            } else {
                //先取消 再设置
                MeetingControlRequest cancelRequest = new MeetingControlRequest();
                if(MeetingControlType.PRESENTER.equals(meetingControlType)){
                    request.setId(conferenceStatePid);
                }
                cancelRequest.assignValueAccordingToType(meetingControlType, "");
                smcMeetingControlService.meetingControl(conferenceId, cancelRequest, smcToken);
                TransactionId transactionId = new TransactionId(TransactionType.CONFERENCES_STATUS, conferenceId);
                TransactionManage.wait(transactionId, 1000);
                TransactionManage.clean(transactionId);//使用完立马清掉
                CommonHelper.sleep(100);
                smcMeetingControlService.meetingControl(conferenceId, request, smcToken);
            }
        }
    }

    /**
     * 会议广播处理
     *
     * @param conferenceId
     * @param request
     * @param smcToken
     * @throws MyHttpException
     */
    public void meetingControlBroadcast(String conferenceId, MeetingControlRequestEx request, MeetingControlMeetingInfo remoteMeetingInfo, String smcToken) throws MyHttpException {
        //如果是SMC2.0如果存在需要先取消 再设置
        MeetingInfo meetingInfo = meetingInfoManagerService.get(conferenceId);
        ConferenceState conferenceState = meetingInfo.getConferenceState();
        if (SystemConfiguration.smcVersionIsV2()) {
            String conferenceStatePid = conferenceState.getByType(MeetingControlType.BROADCASTER);

            if (!StringUtils.isEmpty(conferenceStatePid) && !"".equals(request.getBroadcaster())){
                if (remoteMeetingInfo != null && remoteMeetingInfo.getSmcVersionType().equals(SmcVersionType.V2) && SystemConfiguration.smcVersionIsV2() && request.fromUp()) {
                    MeetingControlRequestEx cancelRequestEx = CommonHelper.copyBean(request, MeetingControlRequestEx.class);
                    cancelRequestEx.setBroadcaster("");
                    remoteGwService.toByGwId(remoteMeetingInfo.getGwId()).patch(String.format(ConfApiUrl.CONTROLLER_CONFERENCE_STATUS_DIRECT.value(), remoteMeetingInfo.getId()), cancelRequestEx);
                } else {
                    MeetingControlRequest requestLocal = new MeetingControlRequest();
                    requestLocal.setBroadcaster("");
                    smcMeetingControlService.meetingControl(conferenceId, requestLocal, smcToken);
                }
                TransactionId transactionId = new TransactionId(TransactionType.CONFERENCES_STATUS, conferenceId);
                TransactionManage.wait(transactionId, 1000);
                TransactionManage.clean(transactionId);//使用完立马清掉
                CommonHelper.sleep(200);
            }
            if (remoteMeetingInfo != null && remoteMeetingInfo.getSmcVersionType().equals(SmcVersionType.V2) && SystemConfiguration.smcVersionIsV2()  && request.fromUp()) {
                remoteGwService.toByGwId(remoteMeetingInfo.getGwId()).patch(String.format(ConfApiUrl.CONTROLLER_CONFERENCE_STATUS_DIRECT.value(), remoteMeetingInfo.getId()), request);
            } else {
                MeetingControlRequest requestLocal = new MeetingControlRequest();
                requestLocal.setBroadcaster(request.getBroadcaster());
                smcMeetingControlService.meetingControl(conferenceId, requestLocal, smcToken);
            }
        } else {
            smcMeetingControlService.meetingControl(conferenceId, request, smcToken);
        }
    }


    /**
     * 会议控制前置处理
     *
     * @param conferenceId
     * @param request
     * @param smcToken
     * @throws MyHttpException
     */
    public void meetingControlBefore(String conferenceId, MeetingControlRequest request, String smcToken) throws MyHttpException {
        //如果是SMC2.0如果存在需要先取消 再设置
        if (SystemConfiguration.smcVersionIsV2()) {
            request = CommonHelper.copyBean(request, MeetingControlRequest.class);
            MeetingInfo meetingInfo = meetingInfoManagerService.get(conferenceId);
            if(meetingInfo == null){
                smcMeetingControlService.meetingControl(conferenceId, request, smcToken);
                return;
            }
            List<MeetingControlType> handleTypes = new ArrayList<>();
            handleTypes.add(MeetingControlType.CHAIRMAN);
            handleTypes.add(MeetingControlType.BROADCASTER);
            handleTypes.add(MeetingControlType.SPOKESMAN);
            for (MeetingControlType item : handleTypes) {
                String pid = request.getByType(item);
                if (pid != null) {
                    MeetingControlRequest requestOne = new MeetingControlRequest();
                    requestOne.assignValueAccordingToType(item, pid);
                    if (item.equals(MeetingControlType.SPOKESMAN)) {
                        requestOne.setIsRolled(request.getIsRolled());
                    }
                    meetingControlBeforeFilter(conferenceId, item, pid, meetingInfo.getConferenceState(), requestOne, smcToken);
                    if (item.equals(MeetingControlType.SPOKESMAN)) {
                        request.setIsRolled(null);
                    }
                    request.assignValueAccordingToType(item, null);
                }
            }
            //判断 是否有要发送的字段(除开上面的)
            if (JSON.toJSONBytes(request).length > 2) {
                smcMeetingControlService.meetingControl(conferenceId, request, smcToken);
            }
        } else {
            smcMeetingControlService.meetingControl(conferenceId, request, smcToken);
        }
    }

    @Override
    public void meetingControlTop(MeetingControlRequest meetingControlRequest, String confCasId, String
            lowConferenceId, String token) throws MyHttpException {
        MeetingInfo meetingInfo = meetingInfoManagerService.getByCasConfId(confCasId);
        if (meetingInfo != null) {
            meetingControlBefore(meetingInfo.getId(), meetingControlRequest, getSmcToken(token));
            sendTop(meetingInfo.getId(), meetingControlRequest);
            Set<String> excludeConferenceIdSet = new HashSet<>();
            excludeConferenceIdSet.add(lowConferenceId);
            sendChild(meetingInfo.getId(), meetingControlRequest, excludeConferenceIdSet);
        }
    }


    @Override
    public void meetingControlDirect(MeetingControlRequestEx request, String conferenceId, String token) throws
            MyHttpException {
        meetingControlBefore(conferenceId, request.toMeetingControlRequest(), getSmcToken(token));
    }

    @Override
    public void meetingControl(MeetingControlRequestEx meetingControlRequestEx, String conferenceId, String token) throws
            MyHttpException {
        String chairman = meetingControlRequestEx.getChairman();
        String broadcaster = meetingControlRequestEx.getBroadcaster();
        String spokesman = meetingControlRequestEx.getSpokesman();
        String lockPresenter = meetingControlRequestEx.getLockPresenter();
        String presenter = meetingControlRequestEx.getPresenter();
        Boolean isOnline = meetingControlRequestEx.getIsOnline();
        Boolean isLock = meetingControlRequestEx.getIsLock();
        Boolean isMute = meetingControlRequestEx.getIsMute();
        Boolean isQuiet = meetingControlRequestEx.getIsQuiet();
        Boolean isVoiceActive = meetingControlRequestEx.getIsVoiceActive();
        MultiPicInfo multiPicInfo = meetingControlRequestEx.getMultiPicInfo();
        String recordOpType = meetingControlRequestEx.getRecordOpType();
        MultiPicInfo recordSource = meetingControlRequestEx.getRecordSource();
        String mode = meetingControlRequestEx.getMode();
        if (isLock != null) {
            MeetingControlRequest request = new MeetingControlRequest();
            request.setIsLock(isLock);
            if (!SmcVersionType.V2.equals(meetingControlRequestEx.getSmcVersionType()) || !SystemConfiguration.smcVersionIsV2()) {
                smcMeetingControlService.meetingControl(conferenceId, request, getSmcToken(token));
            }
            sendChild(conferenceId, request);
        }
        if (lockPresenter != null) {
            MeetingControlRequestEx requestEx = meetingControlRequestEx.purify();
            commonControl(conferenceId, MeetingControlType.LOCKPRESENTER, lockPresenter, requestEx, token);
        }
        //主席
        if (chairman != null) {
            MeetingControlRequestEx requestEx = meetingControlRequestEx.purify();
            setChairman(conferenceId, token, chairman, requestEx);
        }
        //广播会场  多画面传00000000-0000-0000-0000-000000000000
        if (broadcaster != null) {
            MeetingControlRequestEx requestEx = meetingControlRequestEx.purify();
            broadcastHandle(conferenceId, broadcaster, MeetingControlType.BROADCASTER, requestEx, token);
        }
        if (spokesman != null) {
            MeetingControlRequestEx requestEx = meetingControlRequestEx.purify();
            requestEx.setIsRolled(meetingControlRequestEx.getIsRolled());
            commonControl(conferenceId, MeetingControlType.SPOKESMAN, spokesman, requestEx, token);
        }
        //呼叫会场
        if (isOnline != null) {
            setIsOnline(conferenceId, token, isOnline);
        }
        //静音
        if (isMute != null) {
            controlLocalWithoutCasChannel(conferenceId, token, isMute, "isMute");
        }
        //关闭扬声器
        if (isQuiet != null) {
            controlLocalWithoutCasChannel(conferenceId, token, isQuiet, "isQuiet");
        }
        //发送演示
        if (presenter != null) {
            setPresenter(conferenceId, token, chairman, presenter);
        }
        //声控
        if (isVoiceActive != null) {
            CascadeParticipantDirection cascadeParticipantDirection = meetingControlRequestEx.getFrom();
            setIsVoiceActive(conferenceId, token, isVoiceActive, cascadeParticipantDirection);
        }
        //设置会议多画面
        if (multiPicInfo != null) {
            setMultiPicInfo(conferenceId, token, multiPicInfo);
        }
        if (mode != null) {
            setMode(conferenceId, token, mode);
        }
        if (recordOpType != null || recordSource != null) {
            setRecordOpTypeOrRecordSource(conferenceId, token, recordOpType, recordSource);
        }
    }

    private void setChairman(String conferenceId, String token, String chairman, MeetingControlRequestEx requestEx) throws MyHttpException {
        MeetingInfo meetingInfo = meetingInfoManagerService.get(conferenceId);
        requestEx.setChairman(chairman);
        if(meetingInfo == null){
            smcMeetingControlService.meetingControl(conferenceId, requestEx, getSmcToken(token));
            return;
        }
        String conferenceStatePid = meetingInfo.getConferenceState().getByType(MeetingControlType.CHAIRMAN);
        ParticipantInfo conferenceStateParticipantInfo = null;
        if(conferenceStatePid != null){
            conferenceStateParticipantInfo = meetingInfo.getLocalParticipant().get(conferenceStatePid);
        }

        if("".equals(chairman)){
            if(StringUtils.isEmpty(conferenceStatePid)){
                smcMeetingControlService.meetingControl(conferenceId, requestEx, getSmcToken(token));
            }else {
                if(conferenceStateParticipantInfo != null && !conferenceStateParticipantInfo.isCascadeParticipantH323()){
                    smcMeetingControlService.meetingControl(conferenceId, requestEx, getSmcToken(token));
                }
            }
            sendChild(conferenceId, requestEx);
            return;
        }
        if (chairman.contains(CoreConfig.PARTICIPANT_SIGN)) {
            CasChannelParameter casChannelParameter = CasChannelParameter.valueOf(chairman);
            if(casChannelParameter != null){
                chairman = cascadeChannelManageService.getCascadeChannelOne(conferenceId, casChannelParameter.getDirection(), casChannelParameter.getConfId(), casChannelParameter.getIndex()).getParticipantId();
            }
        }
        ParticipantInfo participantInfo = participantInfoManagerService.getParticipant(conferenceId, chairman);
        if(participantInfo == null){
            CascadeChannelInfo cascadeChannelInfoMain = cascadeChannelManageService.getCascadeChannelMain(conferenceId, CascadeParticipantDirection.UP, null);
            if(cascadeChannelInfoMain == null){
                log.error("Main participantId is null");
                return;
            }
            participantInfo = participantInfoManagerService.getParticipant(conferenceId, cascadeChannelInfoMain.getParticipantId());
            if (participantInfo == null) {
                log.error("participantId is null");
                return;
            }
            chairman = participantInfo.getParticipantId();
        }
        //不是本级
        if (!conferenceId.equals(participantInfo.getConferenceId())) {
            //获取对应下级主通道
            //发给下级
            ChildMeetingInfo childMeetingInfo = meetingInfoManagerService.getChildByConferenceId(conferenceId, participantInfo.getConferenceId());
//            smc2.0先取消
            if(SystemConfiguration.smcVersionIsV2() && conferenceStateParticipantInfo != null && !conferenceStateParticipantInfo.isCascadeParticipantH323()){
                MeetingControlRequest requestLocal = new MeetingControlRequest();
                requestLocal.setChairman("");
                smcMeetingControlService.meetingControl(conferenceId, requestLocal, getSmcToken(token));
                TransactionId transactionId = new TransactionId(TransactionType.CONFERENCES_STATUS, conferenceId);
                TransactionManage.wait(transactionId, 1000);
                TransactionManage.clean(transactionId);//使用完立马清掉
                CommonHelper.sleep(100);

            }
            if(SystemConfiguration.smcVersionIsV2() && conferenceStateParticipantInfo != null && conferenceStateParticipantInfo.isCascadeParticipantH323()){
                MeetingControlRequestEx cpReq = new MeetingControlRequestEx();
                cpReq.setChairman("");
                sendChild(conferenceId, cpReq);
            }
            //本级不做 留给下级做
            NodeBusinessType nodeBusinessType = nodeManageService.getNodeBusinessType(childMeetingInfo.getGwId().getNodeId());
            if (SystemConfiguration.smcVersionIsV2() && childMeetingInfo.smcVersionIsV2() && NodeBusinessType.SMC.equals(nodeBusinessType)) {
                MeetingControlMeetingInfo meetingControlMeetingInfo = new MeetingControlMeetingInfo(conferenceId, nodeDataService.getLocal().toGwId(), SystemConfiguration.getSmcVersion());
                requestEx.setRemoteMeetingInfo(meetingControlMeetingInfo);
            }
            requestEx.setFrom(CascadeParticipantDirection.UP);
            remoteGwService.toByGwId(childMeetingInfo.getGwId()).patch(String.format(ConfApiUrl.CONTROLLER_CONFERENCE_STATUS.value(), childMeetingInfo.getId()), requestEx);
            childCasChannelControl(conferenceId, chairman, MeetingControlType.CHAIRMAN, childMeetingInfo.getId(), null);
            if ( !SystemConfiguration.smcVersionIsV2() || !childMeetingInfo.smcVersionIsV2()) {
                //设置本级的下级主通道为会控状态
                CascadeChannelInfo cascadeChannelInfoMain = cascadeChannelManageService.getCascadeChannelMain(conferenceId, CascadeParticipantDirection.DOWN, childMeetingInfo.getId());
                if (cascadeChannelInfoMain == null) {
                    log.error("chair child node master channel not found child meeting info: {}", childMeetingInfo);
                }
                String cascadeChannelPid = cascadeChannelInfoMain.getParticipantId();
                MeetingControlRequest requestLocal = new MeetingControlRequest();
                requestLocal.setChairman(cascadeChannelPid);
                smcMeetingControlService.meetingControl(conferenceId, requestLocal, getSmcToken(token));
            }

            return;
        }

        if(SystemConfiguration.smcVersionIsV2() && conferenceStateParticipantInfo != null && !conferenceStateParticipantInfo.isCascadeParticipantH323()){
            MeetingControlRequest requestLocal = new MeetingControlRequest();
            requestLocal.setChairman("");
            smcMeetingControlService.meetingControl(conferenceId, requestLocal, getSmcToken(token));
            TransactionId transactionId = new TransactionId(TransactionType.CONFERENCES_STATUS, conferenceId);
            TransactionManage.wait(transactionId, 1000);
            TransactionManage.clean(transactionId);//使用完立马清掉
            CommonHelper.sleep(100);
        }
        childCasChannelControl(conferenceId, chairman, MeetingControlType.CHAIRMAN, "", null);
        //远端为V2
        if(requestEx.remoteV2() && participantInfo.isCascadeParticipant()){
            return;
        }
        MeetingControlRequest requestLocal = new MeetingControlRequest();
        requestLocal.setChairman(chairman);
        smcMeetingControlService.meetingControl(conferenceId, requestLocal, getSmcToken(token));
    }

    private void setRecordOpTypeOrRecordSource(String conferenceId, String token, String recordOpType, MultiPicInfo
            recordSource) throws MyHttpException {
        MeetingControlRequest request = new MeetingControlRequest();
        request.setRecordOpType(recordOpType);
        if (recordSource != null) {
            meetingControlDealMultiPicInfo(conferenceId, recordSource);
            request.setRecordSource(recordSource);
        }
        meetingControlBefore(conferenceId, request, getSmcToken(token));
    }

    private void setMode(String conferenceId, String token, String mode) throws MyHttpException {
        MeetingControlRequest request = new MeetingControlRequest();
        request.setMode(mode);
        meetingControlBefore(conferenceId, request, getSmcToken(token));
        sendChild(conferenceId, request);
    }

    private void setIsVoiceActive(String conferenceId, String token, Boolean isVoiceActive, CascadeParticipantDirection cascadeParticipantDirection) throws MyHttpException {
        if (isVoiceActive) {
            //检测本会议是否有welink
//            MeetingInfo meetingInfo = meetingInfoManagerService.get(conferenceId);
//            CasConfInfo casConfInfo = cascadeService.casConferenceInfos(conferenceId);
//            String confCasId = checkHasWelink(casConfInfo);
//            if(confCasId != null){
//                //检测当前是否广播会议多画面
//                if(meetingInfo.getConferenceState() != null && "00000000-0000-0000-0000-000000000000".equals(meetingInfo.getConferenceState().getBroadcastId())){
//                    setSingleMultiPic(conferenceId, token);
//                }
//            }
            //上级的上空请求，都要设置声控单画面，防止下级上传会议多画面到上级的多画面子窗口
            if(CascadeParticipantDirection.UP.equals(cascadeParticipantDirection)){
                setSingleMultiPic(conferenceId, token);
            }

            //设置声控
            MeetingControlRequest voiceActiveRequest = new MeetingControlRequest();
            voiceActiveRequest.setIsVoiceActive(isVoiceActive);
            meetingControlBefore(conferenceId, voiceActiveRequest, getSmcToken(token));

            //发送到下级
            sendChild(conferenceId, voiceActiveRequest);

        } else {
            //取消广播
            //取消声控
            MeetingControlRequest request = new MeetingControlRequest();
            request.setIsVoiceActive(isVoiceActive);
//            request.setBroadcaster("");
            meetingControlBefore(conferenceId, request, getSmcToken(token));

            //发送到下级
            sendChild(conferenceId, request);
        }
    }

    private boolean setSingleMultiPic(String conferenceId, String token) throws MyHttpException {
        MeetingControlRequest request = new MeetingControlRequest();
        MultiPicInfo childMultiPicInfo = new MultiPicInfo();
        childMultiPicInfo.setPicNum(1);
        childMultiPicInfo.setMode(1);
        List<SubPic> list = new ArrayList<>();
        SubPic subPic = new SubPic();
        //任意在线会场
        Map<String, ParticipantInfo> participantInfos = participantInfoManagerService.getAllParticipant(conferenceId);
        if (participantInfos.size() <= 0) {
            log.warn("Thare has not participants in the conference. conferenceId: {}", conferenceId);
            //TODO 抛异常
            return true;
        }
        ParticipantInfo onlineParticipant = null;
        for(ParticipantInfo participantInfo: participantInfos.values()){
            if(participantInfo.getOnline() && participantInfo.getCascadeParticipantParameter() == null && !"ENCODE".equals(participantInfo.getEncodeType())){
                onlineParticipant = participantInfo;
                break;
            }
        }
        if(onlineParticipant == null){
            for(ParticipantInfo participantInfo: participantInfos.values()){
                if(participantInfo.getOnline() && !"ENCODE".equals(participantInfo.getEncodeType())){
                    onlineParticipant = participantInfo;
                    break;
                }
            }
        }
        if(onlineParticipant == null){
            log.warn("Thare has not online participants in the conference. conferenceId: {}", conferenceId);
            //TODO 抛异常
            return true;
        }
        subPic.setParticipantId(onlineParticipant.getParticipantId());
        subPic.setStreamNumber(0);
        list.add(subPic);
        childMultiPicInfo.setSubPicList(list);
        request.setMultiPicInfo(childMultiPicInfo);
        meetingControlBefore(conferenceId, request, getSmcToken(token));
        request.setMultiPicInfo(null);
        request.setBroadcaster("00000000-0000-0000-0000-000000000000");
        meetingControlBefore(conferenceId, request, getSmcToken(token));
        return false;
    }

    private String checkHasWelink(CasConfInfo casConfInfo){
        if(casConfInfo.getChildConf() != null && !casConfInfo.getChildConf().isEmpty()){
            for(CasConfInfo childCasConfInfo: casConfInfo.getChildConf()){
                if(childCasConfInfo.getIsWeLink()){
                    return childCasConfInfo.getConfCasId();
                }
                String result = checkHasWelink(childCasConfInfo);
                if(result != null){
                    return result;
                }
            }
        }
        return null;
    }

    private void setMultiPicInfo(String conferenceId, String token, MultiPicInfo multiPicInfo) throws
            MyHttpException {
        meetingControlDealMultiPicInfo(conferenceId, multiPicInfo);
        //设本级多画面
        MeetingControlRequest request = new MeetingControlRequest();
        request.setMultiPicInfo(multiPicInfo);
        meetingControlBefore(conferenceId, request, getSmcToken(token));
    }

    private void meetingControlDealMultiPicInfo(String conferenceId, MultiPicInfo multiPicInfo) throws
            MyHttpException {
        if (multiPicInfo.getSubPicList() == null) {
            return;
        }
        for (SubPic subPic : multiPicInfo.getSubPicList()) {
            String participantId = subPic.getParticipantId();
            if (StringUtils.isEmpty(participantId)) {
                continue;
            }
            //被观看的会场信息
            ParticipantInfo beWatchedParticipantInfo = participantInfoManagerService.getParticipant(conferenceId, subPic.getParticipantId());
            if (beWatchedParticipantInfo == null) {
                log.warn("participant Id can not found in memory:{}", subPic.getParticipantId());
                //TODO 抛异常
                continue;
            }
            //非本级会场
            if (!conferenceId.equals(beWatchedParticipantInfo.getConferenceId())) {
                //获取会场所在的下级会议分支
                ChildMeetingInfo childMeetingInfo = meetingInfoManagerService.getChildByConferenceId(conferenceId, beWatchedParticipantInfo.getConferenceId());
                //辅流只需要看本机级联通道辅流
                if(subPic.getStreamNumber() != null && subPic.getStreamNumber() == 1){
                    CascadeChannelInfo cascadeChannelInfo =cascadeChannelManageService.getCascadeChannelMain(conferenceId, CascadeParticipantDirection.DOWN, childMeetingInfo.getId());
                    if(cascadeChannelInfo != null){
                        subPic.setParticipantId(cascadeChannelInfo.getParticipantId());
                    }
                    continue;
                }
                //设置观看目标
                ParticipantsControlRequest request = new ParticipantsControlRequestEx();
                request.setMultiPicInfo(MultiPicInfo.valueOfDefault(subPic));
                try {
                    CasChannelParameter watchCasChannelParameter = new CasChannelParameter(childMeetingInfo.getId(), -1, CascadeParticipantDirection.UP);
                    String watchCasChannel = watchCasChannelParameter.toString();
                    String data = remoteGwService.toByGwId(childMeetingInfo.getGwId()).patch(String.format(ConfApiUrl.CONTROLLER_PARTICIPANT_STATUS.value(), childMeetingInfo.getId(), watchCasChannel), request).getBody();
                    WatchInfo watchInfo = JSON.parseObject(data, WatchInfo.class);
                    //使用指定通道
                    String pid = cascadeChannelManageService.getCascadeChannelOne(conferenceId, CascadeParticipantDirection.DOWN, childMeetingInfo.getId(), watchInfo.getIndex()).getParticipantId();
                    //替换pid
                    subPic.setParticipantId(pid);
                }catch (MyHttpException e){
                    log.error("view remote failed");
                    subPic.setParticipantId("");
                }
            }
        }
    }

    private void setPresenter(String conferenceId, String token, String chairman, String presenter) throws
            MyHttpException {
        MeetingControlRequest request = new MeetingControlRequest();
        request.setPresenter(presenter);
        if ("".equals(presenter)) {
            boolean isSuccess = sendChild1(conferenceId, request);
            try {
                MeetingInfo meetingInfo = meetingInfoManagerService.get(conferenceId);
                String presenterId = meetingInfo.getConferenceState().getPresenterId();
                if(!StringUtils.isEmpty(presenterId)){
                    request.setId(presenterId);
                    meetingControlBefore(conferenceId, request, getSmcToken(token));
                }
            } catch (MyHttpException e) {
                if (isSuccess) {
                    return;
                }
                log.error("send to local error: {}", e.toString());
                throw e;
            }
        } else {
            ParticipantInfo participantInfo = participantInfoManagerService.getParticipant(conferenceId, presenter);
            if (participantInfo == null) {
                log.warn("participant Id can not found in memory:{}", chairman);
                //TODO 抛异常
                meetingControlBefore(conferenceId, request, getSmcToken(token));
                return;
            }
            if (conferenceId.equals(participantInfo.getConferenceId())) {
                meetingControlBefore(conferenceId, request, getSmcToken(token));
            } else {
                ChildMeetingInfo childMeetingInfo = meetingInfoManagerService.getChildByConferenceId(conferenceId, participantInfo.getConferenceId());
                remoteGwService.toByGwId(childMeetingInfo.getGwId()).patch(String.format(ConfApiUrl.CONTROLLER_CONFERENCE_STATUS.value(), childMeetingInfo.getId()), request);
            }
        }
    }

    private void setIsOnline(String conferenceId, String token, Boolean isOnline) throws MyHttpException {
        MeetingControlRequest request = new MeetingControlRequest();
        request.setIsOnline(isOnline);
//        if (isOnline) {
//            meetingControlBefore(conferenceId, request, getSmcToken(token));
//            sendChild(conferenceId, request);
//        } else {
        //取本级非级联通道会场
        //呼叫和挂断只针对普通会场，不对级联通道进行操作
        Map<String, ParticipantInfo> localNotCasParticipant = participantInfoManagerService.getLocalNotCasParticipant(conferenceId);
        if (localNotCasParticipant.size() > 0) {
            List<ParticipantsControlRequest> participantsControlRequests = new ArrayList<>();
            for (ParticipantInfo item : localNotCasParticipant.values()) {
                ParticipantsControlRequest participantsControlRequest = new ParticipantsControlRequest();
                participantsControlRequest.setId(item.getParticipantId());
                participantsControlRequest.setIsOnline(isOnline);
                participantsControlRequests.add(participantsControlRequest);
            }
            smcMeetingControlService.participantsControl(conferenceId, participantsControlRequests, getSmcToken(token));
        }
        sendChild(conferenceId, request);
//        }
    }

    private void controlLocalWithoutCasChannel(String conferenceId, String token, Boolean control, String action) throws
            MyHttpException {
        MeetingControlRequest request = new MeetingControlRequest();
        switch (action) {
            case "isMute": {
                request.setIsMute(control);
                break;
            }
            case "isQuiet": {
                request.setIsQuiet(control);
                break;
            }
            default:
        }
        if (!control) {
            meetingControlBefore(conferenceId, request, getSmcToken(token));
            sendChild(conferenceId, request);
        } else {
            List<String> list = new ArrayList<>();
            Map<String, ParticipantInfo> localCasParticipant = participantInfoManagerService.getLocalCasParticipant(conferenceId);
            for (ParticipantInfo casParticipantInfo : localCasParticipant.values()) {
                list.add(casParticipantInfo.getParticipantId());
            }
            switch (action) {
                case "isMute": {
                    ChatMicRequest chatMicRequest = new ChatMicRequest();
                    chatMicRequest.setSet(true);
                    chatMicRequest.setExcludeParticipants(list);
                    chatMicBefore(conferenceId, chatMicRequest, getSmcToken(token));
                    break;
                }
                case "isQuiet": {
                    ChatSpeakerRequest chatSpeakerRequest = new ChatSpeakerRequest();
                    chatSpeakerRequest.setSet(true);
                    chatSpeakerRequest.setExcludeParticipants(list);
                    chatSpeakerBefore(conferenceId, chatSpeakerRequest, getSmcToken(token));
                    break;
                }
                default:
            }
            sendChild(conferenceId, request);
        }
    }


    /**
     * 广播
     *
     * @param conferenceId
     * @param pid
     * @param type
     * @param token
     * @throws MyHttpException
     */
    private void broadcastHandle(String conferenceId, String pid, MeetingControlType type, MeetingControlRequestEx
            requestEx, String token) throws MyHttpException {
        String upPid = null;
        Boolean lock = true;
        if (requestEx.fromUp()) {
            lock = false;
        }
        if (lock) {
            CascadeChannelInfo cascadeChannelInfoMain = cascadeChannelManageService.getCascadeChannelMain(conferenceId, CascadeParticipantDirection.UP, conferenceId);
            //有上级级联通道才操作
            if (cascadeChannelInfoMain != null) {
                ParticipantInfo casParticipantInfo = participantInfoManagerService.getParticipant(conferenceId, cascadeChannelInfoMain.getParticipantId());
                if (casParticipantInfo != null) {
                    if (casParticipantInfo.getOnline()) {
                        upPid = casParticipantInfo.getParticipantId();
                    }
                    if (upPid != null) {
                        //锁定
                        ParticipantsControlRequest participantsControlRequest = new ParticipantsControlRequest();
                        participantsControlRequest.setVideoSwitchAttribute(1);
                        smcMeetingControlService.participantsControl(conferenceId, upPid, participantsControlRequest, getSmcToken(token));
                    }
                }
            }
        }
        try {
            commonControl(conferenceId, type, pid, requestEx, token);
        } catch (Exception e) {
            throw e;
        } finally {
            if (type.equals(MeetingControlType.BROADCASTER)) {
                if (upPid != null) {
                    //解锁
                    ParticipantsControlRequest participantsControlRequest = new ParticipantsControlRequest();
                    participantsControlRequest.setVideoSwitchAttribute(0);
                    smcMeetingControlService.participantsControl(conferenceId, upPid, participantsControlRequest, getSmcToken(token));
                }
            }
        }
    }


    @Override
    public WatchInfo participantsControl(ParticipantsControlRequestEx participantsControlRequestEx, String
            conferenceId, String participantId, String token) throws MyHttpException {
        WatchInfo watchInfo = null;
        if (participantsControlRequestEx.getWatchMeetingControlType() == null) {
            //默认设置为普通观看
            participantsControlRequestEx.setWatchMeetingControlType(MeetingControlType.WATCH);
        }
        CasChannelParameter watchCasChannel = CasChannelParameter.valueOf(participantId);
        if (watchCasChannel == null && !participantsControlRequestEx.isAutoCascadeChannel()) {
            //非级联通道观看处理
            ParticipantInfo watchParticipantInfo = participantInfoManagerService.getParticipant(conferenceId, participantId);
            if (watchParticipantInfo == null) {
                log.warn("participant Id can not found in memory:{}", participantId);
                ParticipantsControlRequest participantsControlRequest = JSON.parseObject(JSON.toJSONString(participantsControlRequestEx), ParticipantsControlRequest.class);
                smcMeetingControlService.participantsControl(conferenceId, participantId, participantsControlRequest, getSmcToken(token));
                return watchInfo;
            }
            //观看者的会议不在本级  在本级的话就去下面处理了 此处比较绕
            if (!conferenceId.equals(watchParticipantInfo.getConferenceId())) {
                //观看者的下级会议信息
                ChildMeetingInfo watchChildMeetingInfo = meetingInfoManagerService.getChildByConferenceId(conferenceId, watchParticipantInfo.getConferenceId());
                if (watchChildMeetingInfo == null) {
                    log.warn("participant Id can not found in memory:{}", participantId);
                    return watchInfo;
                }
                //MultiPicInfo中subPic加原始的GWId
                if (participantsControlRequestEx.getMultiPicInfo() != null) {
                    if (participantsControlRequestEx.getParticipantPositionInfo() == null) {
                        participantsControlRequestEx.setParticipantPositionInfo(new HashMap<>());
                    }
                    dealMultiPicInfoSubPic(conferenceId, participantsControlRequestEx.getMultiPicInfo(), participantsControlRequestEx.getParticipantPositionInfo());
                }
                remoteGwService.toByGwId(watchChildMeetingInfo.getGwId()).patch(String.format(ConfApiUrl.CONTROLLER_PARTICIPANT_STATUS.value(), watchChildMeetingInfo.getId(), participantId), participantsControlRequestEx);
                return watchInfo;
            }
        }
        MultiPicInfo multiPicInfo = participantsControlRequestEx.getMultiPicInfo();
        //MultiPicInfo oldMultiPicInfo = CommonHelper.copyBean(multiPicInfo, MultiPicInfo.class);
        //核心处理
        dealParticipantControlMultiPicInfo(multiPicInfo, participantsControlRequestEx.getParticipantPositionInfo(), conferenceId, participantId, participantsControlRequestEx.getWatchMeetingControlType());
        if (participantsControlRequestEx.isAutoCascadeChannel()) {
            //告诉远端看哪个会场(直接指定pid的那种)
            watchInfo = new WatchInfo(multiPicInfo.getFirstParticipantId());
        } else {
            //复用
            Boolean reuse = false;
            if (watchCasChannel != null) {
                //分配上级级联通道  AllocateCasChannelInfo
                AllocateCasChannelInfo allocateCasChannelInfo = cascadeChannelManageService.allocateCasChannel(conferenceId, watchCasChannel.getDirection(), watchCasChannel.getConfId(), participantsControlRequestEx.getWatchMeetingControlType(), multiPicInfo.getFirstParticipantId());
                if (!allocateCasChannelInfo.find()) {
                    log.info("[级联通道] 上级级联通道分配失败 id:{} participantId:{} meetingControlType:{}", conferenceId, participantId, participantsControlRequestEx.getWatchMeetingControlType().name());
                    throw new MyHttpException(409, GwErrorCode.CASCADE_CHANNEL_ALLOCATION_FAILED.toString());
                }
                reuse = allocateCasChannelInfo.getReuse();
                CascadeChannelInfo watchCascadeChannelInfo = allocateCasChannelInfo.getCascadeChannelInfo();
                String watchCasChannelPId = watchCascadeChannelInfo.getParticipantId();
                ParticipantInfo casParticipantInfo = participantInfoManagerService.getParticipant(conferenceId, watchCasChannelPId);
                if(casParticipantInfo.isCascadeParticipantH323()){
                    return new WatchInfo(multiPicInfo.getFirstParticipantId());
                }
                participantId = watchCasChannelPId;
                //告诉远端看那条级联通道
                watchInfo = new WatchInfo(watchCascadeChannelInfo.getBaseInfo().getIndex());
            }
            if (!reuse) {
                ParticipantsControlRequest participantsControlRequest = JSON.parseObject(JSON.toJSONString(participantsControlRequestEx), ParticipantsControlRequest.class);
                //本级设置观看
                smcMeetingControlService.participantsControl(conferenceId, participantId, participantsControlRequest, getSmcToken(token));
            }
        }
        return watchInfo;
    }


    private void dealMultiPicInfoSubPic(String conferenceId, MultiPicInfo
            multiPicInfo, Map<String, ParticipantPositionInfo> participantPositionInfoMap) {
        if (multiPicInfo.getSubPicList() == null) {
            return;
        }
        MeetingInfo meetingInfo = meetingInfoManagerService.get(conferenceId);
        for (SubPic subPic : multiPicInfo.getSubPicList()) {
            String participantId = subPic.getParticipantId();
            ParticipantInfo participantInfo = participantInfoManagerService.getParticipant(conferenceId, participantId);
            if (participantInfo != null) {
                ParticipantPositionInfo participantPositionInfo = new ParticipantPositionInfo(nodeDataService.getLocal().toGwId(), meetingInfo.getId());
                participantPositionInfoMap.put(participantId, participantPositionInfo);
            }
        }
    }

    private MyHttpException dealParticipantControlMultiPicInfo(MultiPicInfo
                                                            multiPicInfo, Map<String, ParticipantPositionInfo> participantPositionInfoMap, String conferenceId, String
                                                            participantId, MeetingControlType watchMeetingControlType) throws MyHttpException {
        if (multiPicInfo != null && !CollectionUtils.isEmpty(multiPicInfo.getSubPicList())) {
            //处理观看
            for (SubPic subPic : multiPicInfo.getSubPicList()) {
                String beWatchedId = subPic.getParticipantId();
                //为空，为多画面有本级处理
                if (StringUtils.isEmpty(beWatchedId) || CoreConfig.CONFERENCE_PARTICIPANT_ID.equals(beWatchedId)) {
                    continue;
                }
                //被观看的下级会议信息
                ParticipantInfo beWatchedChildParticipantInfo = participantInfoManagerService.getParticipant(conferenceId, beWatchedId);
                String pid = subPic.getParticipantId();
                if (beWatchedChildParticipantInfo == null) {
                    //查看本是否有上级，有往上级发
                    Map<String, ParticipantInfo> localCasParticipantUp = participantInfoManagerService.getLocalCasParticipant(conferenceId, CascadeParticipantDirection.UP);
                    if (CollectionUtils.isEmpty(localCasParticipantUp)) {
                        log.warn("participantId can not found in memory with has not top conference:{} participantId:{}", conferenceId, participantId);
                        //TODO 抛异常
                        return null;
                    }
                    //辅流只需要看本机级联通道辅流
                    if(subPic.getStreamNumber() != null && subPic.getStreamNumber() == 1){
                        CascadeChannelInfo cascadeChannelInfo =cascadeChannelManageService.getCascadeChannelMain(conferenceId, CascadeParticipantDirection.UP, null);
                        if(cascadeChannelInfo != null){
                            subPic.setParticipantId(cascadeChannelInfo.getParticipantId());
                        }
                        continue;
                    }
                    if (participantPositionInfoMap == null || !participantPositionInfoMap.containsKey(beWatchedId)) {
                        continue;
                    }
                    ParticipantPositionInfo participantPositionInfo = participantPositionInfoMap.get(beWatchedId);
                    PullSourceRequest request = new PullSourceRequest(beWatchedId, false, watchMeetingControlType);
                    try {
                        String data = remoteGwService.toByGwId(participantPositionInfo.getGwId()).patch(String.format(ConfApiUrl.PARTICIPANT_TO_PULL_SOURCE.value(), participantPositionInfo.getConferenceId(), participantId), request).getBody();
                        WatchInfo watchInfo = JSON.parseObject(data, WatchInfo.class);
                        if (watchInfo != null) {
                            pid = watchInfo.getTargetParticipantId(conferenceId, CascadeParticipantDirection.UP, null, cascadeChannelManageService);
                        } else {
                            //TODO 级联通道分配失败处理
                        }
                    }catch (MyHttpException e){
                        log.error("view remote fail");
                        pid = "";
                    }
                } else if (!conferenceId.equals(beWatchedChildParticipantInfo.getConferenceId())) {
                    //被观看者的下级会议信息
                    ChildMeetingInfo beWatchedChildMeetingInfo = meetingInfoManagerService.getChildByConferenceId(conferenceId, beWatchedChildParticipantInfo.getConferenceId());
                    if (beWatchedChildMeetingInfo == null) {
                        log.warn("child meetingInfo not found in conference memory. :{}", beWatchedChildParticipantInfo.getConferenceId());
                        //TODO 抛异常
                        return null;
                    }
                    //辅流只需要看本机级联通道辅流
                    if(subPic.getStreamNumber() != null && subPic.getStreamNumber() == 1){
                        CascadeChannelInfo cascadeChannelInfo =cascadeChannelManageService.getCascadeChannelMain(conferenceId, CascadeParticipantDirection.DOWN, beWatchedChildMeetingInfo.getId());
                        if(cascadeChannelInfo != null){
                            subPic.setParticipantId(cascadeChannelInfo.getParticipantId());
                        }
                        continue;
                    }
                    ParticipantsControlRequestEx request = new ParticipantsControlRequestEx();
                    request.setMultiPicInfo(MultiPicInfo.valueOfDefault(subPic));
                    request.setWatchMeetingControlType(watchMeetingControlType);
                    String watchPid;
                    NodeBusinessType nodeBusinessType = nodeManageService.getNodeBusinessType(beWatchedChildMeetingInfo.getGwId().getNodeId());
                    if (beWatchedChildMeetingInfo.smcVersionIsV2() && SystemConfiguration.smcVersionIsV2() && NodeBusinessType.SMC.equals(nodeBusinessType)) {
                        request.setAutoCascadeChannel(true);//设置为自动通道
                        watchPid = "0";//没有特殊含义 这个反正也不会用 可以任意更改
                    } else {
                        //让下级自己分配一条上级级联通道
                        CasChannelParameter casChannelParameter = new CasChannelParameter(beWatchedChildParticipantInfo.getConferenceId(), -1, CascadeParticipantDirection.UP);
                        watchPid = casChannelParameter.toString();
                    }
                    try {
                        String data = remoteGwService.toByGwId(beWatchedChildMeetingInfo.getGwId()).patch(String.format(ConfApiUrl.CONTROLLER_PARTICIPANT_STATUS.value(), beWatchedChildMeetingInfo.getId(), watchPid), request).getBody();
                        WatchInfo watchInfo = JSON.parseObject(data, WatchInfo.class);
                        if (watchInfo != null) {
                            pid = watchInfo.getTargetParticipantId(conferenceId, CascadeParticipantDirection.DOWN, beWatchedChildMeetingInfo.getId(), cascadeChannelManageService);
                        }
                    }catch (MyHttpException e){
                        log.error("view remote fail");
                        pid = "";
                        subPic.setParticipantId(pid);
                        return e;
                    }
                }
                subPic.setParticipantId(pid);
            }
        }
        return null;
    }

    @Override
    public WatchInfo pullSource(String conferenceId, String participantId, PullSourceRequest request, String token) throws
            MyHttpException, BaseStateException {
        WatchInfo watchInfo = null;
        //被观看的级联通道
        String beWatchedPId = request.getBeWatchedPId();
        CasChannelParameter beWatchedCasChannelParameter = CasChannelParameter.valueOf(beWatchedPId);
        if (beWatchedCasChannelParameter != null) {
            beWatchedPId = cascadeChannelManageService.getCascadeChannelOne(conferenceId, beWatchedCasChannelParameter.getDirection(), beWatchedCasChannelParameter.getConfId(), beWatchedCasChannelParameter.getIndex()).getParticipantId();
        }
        //观看者会场信息
        ParticipantInfo watchParticipantInfo = participantInfoManagerService.getParticipant(conferenceId, participantId);
        if (watchParticipantInfo == null) {
            //TODO 报错
            log.error("participantInfo or viewed participantInfo not found.");
            throw new BaseStateException("participantInfo or viewed participantInfo not found.");
        }
        //被观看者会场信息
        ParticipantInfo beWatchedParticipantInfo = participantInfoManagerService.getParticipant(conferenceId, beWatchedPId);
        if (!request.isAutoCascadeChannel()) {
            //自动级联通道被观看者可能在上级  允许找不到
            if (beWatchedParticipantInfo == null) {
                //TODO 报错
                log.error("participantInfo or viewed beWatchedParticipantInfo not found.");
                throw new BaseStateException("participantInfo or viewed beWatchedParticipantInfo not found.");
            }
        }
        //观看者就在本级
        if (conferenceId.equals(watchParticipantInfo.getConferenceId())) {
            if (request.isAutoCascadeChannel()) {
                watchInfo = new WatchInfo(beWatchedPId);
            } else {
                watchInfo = new WatchInfo(beWatchedParticipantInfo.getCascadeParticipantParameter().getIndex());
            }
            return watchInfo;
        } else {
            //观看者下级会议信息
            ChildMeetingInfo watchChildMeetingInfo = meetingInfoManagerService.getChildByConferenceId(conferenceId, watchParticipantInfo.getConferenceId());
            if (request.isAutoCascadeChannel()) {
                return pullSourceToLower(conferenceId, participantId, token, watchChildMeetingInfo, beWatchedPId, request.getWatchMeetingControlType());
            } else {
                //被观者在本级
                if (conferenceId.equals(beWatchedParticipantInfo.getConferenceId())) {
                    return pullSourceToLower(conferenceId, participantId, token, watchChildMeetingInfo, beWatchedPId, request.getWatchMeetingControlType());
                } else {
                    //被观看者在下级
                    ChildMeetingInfo beWatchedChildMeetingInfo = meetingInfoManagerService.getChildByConferenceId(conferenceId, beWatchedParticipantInfo.getConferenceId());
                    //生成一个观看请求,让上级级联通道去看悲观看着者
                    CasChannelParameter beWatchedChildChannelParameter = new CasChannelParameter(beWatchedChildMeetingInfo.getId(), -1, CascadeParticipantDirection.UP);
                    String watchedCasChannelPId = beWatchedChildChannelParameter.toString();
                    ParticipantsControlRequestEx participantsControlRequestEx = new ParticipantsControlRequestEx();
                    SubPic subPic = new SubPic();
                    subPic.setParticipantId(beWatchedPId);
                    participantsControlRequestEx.setMultiPicInfo(MultiPicInfo.valueOfDefault(subPic));
                    if (beWatchedChildMeetingInfo.smcVersionIsV2() && SystemConfiguration.smcVersionIsV2()) {
                        participantsControlRequestEx.setAutoCascadeChannel(true);
                    }
                    participantsControlRequestEx.setWatchMeetingControlType(request.getWatchMeetingControlType());
                    String data = remoteGwService.toByGwId(beWatchedChildMeetingInfo.getGwId()).patch(String.format(ConfApiUrl.CONTROLLER_PARTICIPANT_STATUS.value(), beWatchedChildMeetingInfo.getId(), watchedCasChannelPId), participantsControlRequestEx).getBody();
                    watchInfo = JSON.parseObject(data, WatchInfo.class);
                    String beWatchedCasChannelPId = watchInfo.getTargetParticipantId(conferenceId, CascadeParticipantDirection.DOWN, beWatchedChildMeetingInfo.getId(), cascadeChannelManageService);
                    //让观看所在的下级级联通道去看 被观看的下级会议级联通道
                    return pullSourceToLower(conferenceId, participantId, token, watchChildMeetingInfo, beWatchedCasChannelPId, request.getWatchMeetingControlType());
                }
            }
        }
    }

    private WatchInfo pullSourceToLower(String conferenceId, String participantId, String token, ChildMeetingInfo
            watchChildMeetingInfo, String beWatchedPId, MeetingControlType watchMeetingControlType) throws MyHttpException {
        if (watchChildMeetingInfo.smcVersionIsV2() && SystemConfiguration.smcVersionIsV2()) {
            PullSourceRequest pullSourceRequest = new PullSourceRequest(beWatchedPId, true, watchMeetingControlType);
            String data = remoteGwService.toByGwId(watchChildMeetingInfo.getGwId()).patch(String.format(ConfApiUrl.PARTICIPANT_TO_PULL_SOURCE.value(), watchChildMeetingInfo.getId(), participantId), pullSourceRequest).getBody();
            WatchInfo watchInfo = JSON.parseObject(data, WatchInfo.class);
            //响应使用哪条通道
            return watchInfo;
        } else {
            //往被观看者方向送画面
            AllocateCasChannelInfo allocateCasChannelInfo = cascadeChannelManageService.allocateCasChannel(conferenceId, CascadeParticipantDirection.DOWN, watchChildMeetingInfo.getId(), watchMeetingControlType, beWatchedPId);
            if (!allocateCasChannelInfo.find()) {
                log.info("[级联通道] 下级级联通道分配失败 id:{} participantId:{} meetingControlType:{}", conferenceId, participantId, watchMeetingControlType.name());
                throw new MyHttpException(409, GwErrorCode.CASCADE_CHANNEL_ALLOCATION_FAILED.toString());
            }
            CascadeChannelInfo watchCascadeChannelInfo = allocateCasChannelInfo.getCascadeChannelInfo();
            if (!allocateCasChannelInfo.getReuse()) {
                ParticipantsControlRequest participantsControlRequest = new ParticipantsControlRequest();
                SubPic subPic = new SubPic();
                subPic.setParticipantId(beWatchedPId);
                participantsControlRequest.setMultiPicInfo(MultiPicInfo.valueOfDefault(subPic));
                //本级的下级级联通道去观看被观看者(可能是会场 可能是级联会场 都可以)
                smcMeetingControlService.participantsControl(conferenceId, watchCascadeChannelInfo.getParticipantId(), participantsControlRequest, getSmcToken(token));
            }
            //让下级的目标会场去看上级级联通道
            CasChannelParameter beWatchedCasChannelParameter = new CasChannelParameter(watchChildMeetingInfo.getId(), watchCascadeChannelInfo.getBaseInfo().getIndex(), CascadeParticipantDirection.UP);
            //被观看级联通道ID
            String beWatchedCasChannelPId = beWatchedCasChannelParameter.toString();
            //发给下级的上级级联通道去看目标会场
            PullSourceRequest pullSourceRequest = new PullSourceRequest(beWatchedCasChannelPId, false, watchMeetingControlType);
            String data = remoteGwService.toByGwId(watchChildMeetingInfo.getGwId()).patch(String.format(ConfApiUrl.PARTICIPANT_TO_PULL_SOURCE.value(), watchChildMeetingInfo.getId(), participantId), pullSourceRequest).getBody();
            WatchInfo watchInfo = JSON.parseObject(data, WatchInfo.class);
            //响应使用哪条通道
            return watchInfo;
        }
    }

    @Override
    public void participantsControl(List<ParticipantsControlRequestEx> participantsControlRequestExs, String
            conferenceId, String token) throws MyHttpException {
        //报文按会议Id分类，会议Id找不到，丢弃请求
        Map<String, List<ParticipantsControlRequestEx>> confIdToParticipantsReqMap = new HashMap<>();
        for (ParticipantsControlRequestEx participantsControlRequestEx : participantsControlRequestExs) {
            String participantId = participantsControlRequestEx.getId();
            ParticipantInfo participantInfo = participantInfoManagerService.getParticipant(conferenceId, participantId);
            if (participantInfo == null) {
                log.warn("participantId can not found in memory. :{}", participantId);
                if (confIdToParticipantsReqMap.containsKey(conferenceId)) {
                    List<ParticipantsControlRequestEx> list = confIdToParticipantsReqMap.get(conferenceId);
                    list.add(participantsControlRequestEx);
                } else {
                    List<ParticipantsControlRequestEx> list = new ArrayList<>();
                    list.add(participantsControlRequestEx);
                    confIdToParticipantsReqMap.put(conferenceId, list);
                }
                continue;
            }
            if (participantsControlRequestEx.getMultiPicInfo() != null) {
                if (participantsControlRequestEx.getParticipantPositionInfo() == null) {
                    participantsControlRequestEx.setParticipantPositionInfo(new HashMap<>());
                }
                dealMultiPicInfoSubPic(conferenceId, participantsControlRequestEx.getMultiPicInfo(), participantsControlRequestEx.getParticipantPositionInfo());
            }
            if (confIdToParticipantsReqMap.containsKey(participantInfo.getConferenceId())) {
                List<ParticipantsControlRequestEx> list = confIdToParticipantsReqMap.get(participantInfo.getConferenceId());
                list.add(participantsControlRequestEx);
            } else {
                List<ParticipantsControlRequestEx> list = new ArrayList<>();
                list.add(participantsControlRequestEx);
                confIdToParticipantsReqMap.put(participantInfo.getConferenceId(), list);
            }
        }

        for (Map.Entry<String, List<ParticipantsControlRequestEx>> entry : confIdToParticipantsReqMap.entrySet()) {
            if (!conferenceId.equals(entry.getKey())) {
                //远端
                ChildMeetingInfo childMeetingInfo = meetingInfoManagerService.getChildByConferenceId(conferenceId, entry.getKey());
                if (childMeetingInfo == null) {
                    log.warn("child meetingInfo not found in conference memory. :{}", entry.getKey());
                    //TODO 抛异常
                    return;
                }
                remoteGwService.toByGwId(childMeetingInfo.getGwId()).patch(String.format(ConfApiUrl.CONTROLLER_PARTICIPANTS_STATUS.value(), childMeetingInfo.getId()), entry.getValue());

            } else {
                //本级
                dealLocalParticipantsControl(conferenceId, token, entry);
            }
        }
    }

    private void dealLocalParticipantsControl(String conferenceId, String
            token, Map.Entry<String, List<ParticipantsControlRequestEx>> entry) throws MyHttpException {
        //处理,是否有做观看，并且目标不是本级
        List<ParticipantsControlRequestEx> partViewReq = new ArrayList<>();
        List<ParticipantsControlRequest> localReq = new ArrayList<>();
        for (ParticipantsControlRequestEx participantsControlRequestEx : entry.getValue()) {
            if (participantsControlRequestEx.getMultiPicInfo() == null) {
                ParticipantsControlRequest participantsControlRequest = JSON.parseObject(JSON.toJSONString(participantsControlRequestEx), ParticipantsControlRequest.class);
                localReq.add(participantsControlRequest);
                continue;
            }
            boolean localReqFlag = true;
            MultiPicInfo multiPicInfo = participantsControlRequestEx.getMultiPicInfo();
            List<SubPic> subPicList = multiPicInfo.getSubPicList();
            if (subPicList != null) {
                for (SubPic subPic : subPicList) {
                    String pid = subPic.getParticipantId();
                    if (StringUtils.isEmpty(pid)) {
                        continue;
                    }
                    ParticipantInfo participantInfo = participantInfoManagerService.getParticipant(conferenceId, pid);
                    if (participantInfo == null || !conferenceId.equals(participantInfo.getConferenceId())) {
                        partViewReq.add(participantsControlRequestEx);
                        localReqFlag = false;
                        break;
                    }
                }
            }

            if (localReqFlag) {
                ParticipantsControlRequest participantsControlRequest = JSON.parseObject(JSON.toJSONString(participantsControlRequestEx), ParticipantsControlRequest.class);
                localReq.add(participantsControlRequest);
            }
        }
        if (localReq.size() > 0) {
            dealCascadeOnlineStatus(conferenceId, localReq);
            smcMeetingControlService.participantsControl(conferenceId, localReq, getSmcToken(token));
        }
        for (ParticipantsControlRequestEx participantsControlRequestEx : partViewReq) {
            participantsControl(participantsControlRequestEx, conferenceId, participantsControlRequestEx.getId(), token);
        }
    }

    private void dealCascadeOnlineStatus(String conferenceId, List<ParticipantsControlRequest> localReq) {
        List<ParticipantsControlRequest> casReq = new ArrayList<>();
        MeetingInfo meetingInfo = meetingInfoManagerService.get(conferenceId);
        for(ParticipantsControlRequest participantsControlRequest: localReq){
            if(participantsControlRequest.getIsOnline() != null){
                String participantId = participantsControlRequest.getId();
                ParticipantInfo participantInfo = participantInfoManagerService.getParticipant(conferenceId, participantId);

                if(participantInfo == null || !participantInfo.isCascadeParticipant()){
                    continue;
                }
                log.debug("participantInfo: {}", participantInfo);
                String uri = participantInfo.getUri();
                if(uri.contains("**")){
                    uri = uri.split("\\*\\*")[0];
                }


                CascadeChannelInfo cascadeChannelInfo = meetingInfo.getCascadeChannelInfoMap().get(participantId);
                log.debug("CascadeChannelInfo: {}", cascadeChannelInfo);
                String childConfId = "";
                Map<String, ChildMeetingInfo> childMeetingInfoMap = meetingInfo.getChildMeetingInfoMap();
                log.debug("childMeetingInfoMap: {}", childMeetingInfoMap);
                ChildMeetingInfo childMeetingInfo = null;
                for(ChildMeetingInfo cMeetingInfo: childMeetingInfoMap.values()){
                    if(uri.equals(cMeetingInfo.getAccessCode())){
                        childMeetingInfo = cMeetingInfo;
                    }
                }
                if(childMeetingInfo != null && childMeetingInfo.smcVersionIsV2() && SystemConfiguration.smcVersionIsV2()){
                    participantsControlRequest.setId(participantsControlRequest.getId()+"@0");
                    continue;
                }

                Map<String, ParticipantInfo> localCasParticipant =participantInfoManagerService.getLocalCasParticipant(conferenceId,cascadeChannelInfo.getBaseInfo().getDirection());
                log.debug("localCasParticipant: {}", localCasParticipant);
                for(ParticipantInfo participantInfo1: localCasParticipant.values()){
                    if(!participantInfo1.getUri().contains(uri)){
                        continue;
                    }
                    if(participantInfo1.getParticipantId().equals(participantId)){
                        continue;
                    }
                    ParticipantsControlRequest participantsControlRequest1 = new ParticipantsControlRequest();
                    participantsControlRequest1.setId(participantInfo1.getParticipantId());
                    participantsControlRequest1.setIsOnline(participantsControlRequest.getIsOnline());
                    casReq.add(participantsControlRequest1);
                }
                //子会议为smc2.0
                if(participantInfo.isCascadeParticipantH323()){
                    participantsControlRequest.setId(participantsControlRequest.getId()+"@0");
                }
            }
        }
        if(casReq.size()>0){
            localReq.addAll(casReq);
        }
    }

    @Override
    public void setTextTips(SetTextTipsRequestEx setTextTipsRequestEx, String conferenceId, String token) throws
            MyHttpException {
        smcMeetingControlService.setTextTips(conferenceId, setTextTipsRequestEx, getSmcToken(token));
        for (ChildMeetingInfo item : meetingInfoManagerService.get(conferenceId).getChildMeetingInfoMap().values()) {
            if (item.getId() == null) {
                continue;
            }
            if (meetingInfoManagerService.childIsOnline(conferenceId, item.getId())) {
                try{
                    remoteGwService.toByGwId(item.getGwId()).post(String.format(ConfApiUrl.SET_TEXT_TIPS.value(), item.getId()), setTextTipsRequestEx);
                }catch (MyHttpException e){
                    if(HttpStatus.UNAUTHORIZED.value() == e.getCode()){
                        continue;
                    }
                    throw e;
                }

            }
        }
    }

    @Override
    public void setTextTips(SetTextTipsRequestEx setTextTipsRequestEx, String conferenceId, String
            participantId, String token) throws MyHttpException {
        ParticipantInfo participantInfo = participantInfoManagerService.getParticipant(conferenceId, participantId);
        if (participantInfo == null) {

            log.warn("participant Id can not found in memory:{}", participantId);
            //TODO 抛异常
            return;
        }
        if (conferenceId.equals(participantInfo.getConferenceId())) {
            smcMeetingControlService.setTextTips(conferenceId, participantId, setTextTipsRequestEx, getSmcToken(token));
        } else {
            ChildMeetingInfo childMeetingInfo = meetingInfoManagerService.getChildByConferenceId(conferenceId, participantInfo.getConferenceId());
            if (childMeetingInfo == null) {
                log.warn("Conference Id can not found in memory:{}", participantInfo.getConferenceId());
                //TODO 抛异常
                return;
            }
            remoteGwService.toByGwId(childMeetingInfo.getGwId()).post(String.format(ConfApiUrl.SET_PARTICIPANT_TEXT_TIP.value(), childMeetingInfo.getId(), participantId), setTextTipsRequestEx);
        }
    }

    @Override
    public void duration(DurationMeetingRequestEx durationMeetingRequestEx, String conferenceId, String token) throws
            MyHttpException {
        smcMeetingControlService.duration(conferenceId, durationMeetingRequestEx, getSmcToken(token));
        for (ChildMeetingInfo item : meetingInfoManagerService.get(conferenceId).getChildMeetingInfoMap().values()) {
            if (item.getId() == null) {
                continue;
            }
            if (meetingInfoManagerService.childIsOnline(conferenceId, item.getId())) {
                try{
                    remoteGwService.toByGwId(item.getGwId()).put(String.format(ConfApiUrl.DURATION.value(), item.getId()), durationMeetingRequestEx);
                }catch (MyHttpException e){
                    if(HttpStatus.UNAUTHORIZED.value() == e.getCode()){
                        continue;
                    }
                    throw e;
                }

            }
        }
    }

    /**
     *
     * @param participantReqs 会场列表
     * @param conferenceId 会议号
     * @param token  token
     * @param confCasId 会议接入号
     * @param createSign 是否需要召集会议 true : 是
     */
    @Override
    public void addParticipants(List<ParticipantReq> participantReqs, String conferenceId, String token,
                                String confCasId,boolean createSign) throws MyHttpException {
        MeetingInfo meetingInfo = meetingInfoManagerService.get(conferenceId);
        String mainConfCasId = null;
        String realCondCasId = confCasId;
        if(confCasId != null && confCasId.contains("@")){
            String[] confCasIds = confCasId.split("@");
            if(confCasIds.length == 2){
                realCondCasId = confCasIds[1];
            }
        }
        log.info("create sign:{}",createSign);
        if (meetingInfo != null) {
            mainConfCasId = meetingInfo.getConfCasId();
            if (createSign && (confCasId == null || realCondCasId.equals(mainConfCasId))) {
                //召集会议，需要找到是召集哪个子会议
                List<NodeData> lowNodeList = nodeDataService.getLow();
                NodeData nodeData = lowNodeList.stream().filter(item -> NodeBusinessType.isWelinkOrCloudLink(item.getBusinessType())).collect(Collectors.toList()).get(0);
                GwId gwId = nodeData.toGwId();
                String subject = meetingInfo.getName();
                if(subject == null){
                    GetMeetingDetailResponse response = null;
                    response = smcMeetingControlService.getMeetingDetail(conferenceId, getSmcToken(token), null);
                    if (response != null) {
                        if (!meetingInfoManagerService.contains(conferenceId)) {
                            meetingInfoManagerService.create(conferenceId, response.getConferenceUiParam().getAccessCode(), response.getConferenceUiParam().getSubject(), true, response.getConferenceState());
                        }
                        meetingInfo = meetingInfoManagerService.get(conferenceId);
                        subject = meetingInfo.getName();
                    }
                }
                synchronized (nodeData.getId()) {
                    if (meetingInfo.getChildMeetingByNodeId(nodeData.getId()) == null) {  //子会议中没有welink会议，则需要召集
                        ScheduleMeetingRequestEx request = new ScheduleMeetingRequestEx();
                        request.setParticipants(participantReqs);
                        ConferenceReq conferenceReq = new ConferenceReq();
                        conferenceReq.setSubject(subject);
                        conferenceReq.setDuration(1440);
                        conferenceReq.setScheduleStartTime(UtcTimeUtil.getUTCTimeStr());
                        conferenceReq.setConferenceTimeType("INSTANT_CONFERENCE");
                        request.setConference(conferenceReq);
                        int cascadeNum = getCascadeNum(participantReqs.get(0));
                        request.setCascadeNum(cascadeNum);
                        request.setAccessCode(realCondCasId);
                        String body = remoteGwService.toByGwId(gwId).post(ConfApiUrl.CONFERENCES.value(), request).getBody();
                        ScheduleMeetingRequest scheduleMeetingRequest = new ScheduleMeetingRequest();
                        conferenceReq = new ConferenceReq();
                        conferenceReq.setConferenceTimeType("INSTANT_CONFERENCE");
                        scheduleMeetingRequest.setConference(conferenceReq);
                        meetingService.afterScheduleChildMeeting(body, conferenceId, cascadeNum, scheduleMeetingRequest, realCondCasId, gwId, subject, token);
                        return;
                    }
                }
                ChildMeetingInfo welinkMeeting = meetingInfo.getChildMeetingByNodeId(nodeData.getId());
                String welinkConferenceId = welinkMeeting.getId();
                WelinkConference welinkConference = welinkMeetingManagerService.getWelinkConference(welinkConferenceId);
                welinkConference.assertTokenNotNull(null);
                realCondCasId = welinkMeeting.getAccessCode();
                confCasId = welinkMeeting.getAccessCode();
            }
        }

        if (confCasId != null && mainConfCasId != null && !realCondCasId.equals(mainConfCasId)) {
            ChildMeetingInfo childMeetingInfo = meetingInfoManagerService.getChildByCasConfId(conferenceId, realCondCasId);
            String uri = String.format(ConfApiUrl.ADD_PARTICAPANTS.value(), childMeetingInfo.getId(), confCasId);
            uri += "&createSign=" + createSign;
            remoteGwService.toByGwId(childMeetingInfo.getGwId()).post(uri, participantReqs);
        } else {
            smcMeetingControlService.addParticipants(conferenceId, participantReqs, getSmcToken(token));
        }
    }

    private int getCascadeNum(ParticipantReq participantReq) {
        String uri = participantReq.getUri();
        if (uri == null || !uri.contains("%")) {
            log.error("cant get cascadeNum from uri,uri : {}", uri);
            return 4;
        }
        return Integer.parseInt(uri.split("%")[1]);
    }

    @Override
    public void mergeConference(String confCasId,String conferenceId, MergeConferenceReq req, String token) throws MyHttpException {
        List<MergeConference> mergeConferences = req.getMergeConferences();
        if(mergeConferences == null ||  mergeConferences.size() == 0){
            return;
        }
        //检查本级会议是否存在
        MeetingInfo meetingInfo = meetingInfoManagerService.get(conferenceId);
        if(meetingInfo == null){
            log.info("conference not exist. confId: {}", conferenceId);
            throw new MyHttpException(409, GwErrorCode.CONFERENCE_NOT_EXIST.toString());
        }
        if (confCasId != null && !confCasId.equals("") && !meetingInfo.getConfCasId().equals(confCasId)) {
            //不是在本级添加
            ChildMeetingInfo child = meetingInfoManagerService.getChildByCasConfId(conferenceId, confCasId);
            if (child == null) {
                log.info("target child conference not exist. confId: {},confCasId:{}", conferenceId, confCasId);
                throw new MyHttpException(409, GwErrorCode.CONFERENCE_NOT_EXIST.toString());
            }
            GwId childGwId = child.getGwId();
            remoteGwService.toByGwId(childGwId).post(String.format(ConfApiUrl.MERGE_CONFERENCE.value(), child.getId()), req);
            return;
        }
        String accessCode = meetingInfo.getAccessCode();

        for(MergeConference mergeConference: mergeConferences){
            //检查下级会议是否存在
            String targetConferenceId = mergeConference.getTargetConfId();
            GwId gwId = new GwId(mergeConference.getTargetGwId());

            String uri = String.format("/conf-portal/online/conferences/%s/detail", targetConferenceId);
            String respBody = remoteGwService.toByGwId(gwId).get(uri, null).getBody();
            GetMeetingDetailResponse getMeetingDetailResponse = JSON.parseObject(respBody, GetMeetingDetailResponse.class);

            if(getMeetingDetailResponse == null){
                log.info("target conference not exist. confId: {}", conferenceId);
                throw new MyHttpException(409, GwErrorCode.CONFERENCE_NOT_EXIST.toString());
            }
            String subject = getMeetingDetailResponse.getConferenceUiParam().getSubject();
            //下级添加呼入通道
            GwId realLocalGwId = localTokenManageService.getRealLocalGwIdByToken(token);
            AddCasChannelReq addCasChannelReq = new AddCasChannelReq(
                    targetConferenceId,
                    mergeConference.getCascadeNum(),
                    accessCode,
                    nodeDataService.getLocal().getName(),
                    SystemConfiguration.getSmcVersion(),
                    realLocalGwId
            );
            String respString = remoteGwService.toByGwId(gwId).post(ConfApiUrl.CASCADE_ADD_CHANNEL.value(), addCasChannelReq).getBody();
            AddCasChannelResp addCasChannelResp = JSONObject.parseObject(respString, AddCasChannelResp.class);

            //本级添加呼出通道
            List<ParticipantReq> participantReqs = new ArrayList<>();
            NodeBusinessType nodeBusinessType = nodeManageService.getNodeBusinessType(gwId.getNodeId());
            boolean isWelink = false;
            if(NodeBusinessType.WELINK.equals(nodeBusinessType) || NodeBusinessType.CLOUDLINK.equals(nodeBusinessType)){
                isWelink = true;
            }
            for (int index = 0; index < mergeConference.getCascadeNum(); index++) {
                ParticipantReq participantReq = meetingService.addCasParticipantsHandle(index, mergeConference.getCascadeNum(),
                        CascadeParticipantDirection.DOWN,
                        addCasChannelResp.getRemoteSmcVersionType(),
                        subject,
                        addCasChannelResp.getRemoteAccessCode(),
                        addCasChannelResp.getGwId(), accessCode, isWelink, realLocalGwId);
                if(isWelink && index == 0){
                    participantReq.setDtmfInfo(addCasChannelResp.getChairPassword()+"#");
                }
                participantReqs.add(participantReq);
                if(isWelink){
                    break;
                }
                if (addCasChannelResp.getRemoteSmcVersionType().equals(SmcVersionType.V2) && SystemConfiguration.getSmcVersion().equals(SmcVersionType.V2)) {
                    break;
                }
            }
            ScheduleMeetingRequest scheduleMeetingRequest = new ScheduleMeetingRequest();
            ConferenceReq conference = new ConferenceReq();
            conference.setConferenceTimeType("INSTANT_CONFERENCE");
            scheduleMeetingRequest.setConference(conference);
            meetingService.addParticipantsByConferencesType(scheduleMeetingRequest, conferenceId, participantReqs, token);

            meetingInfoManagerService.createChild(conferenceId, targetConferenceId, addCasChannelResp.getRemoteAccessCode(), subject, gwId);
            //缓存下级会议会场
            //处理订阅
            String subscribeParticipantsDestination = "/topic/conferences/%s/participants/general";
            String subscribeConferencesDestination = "/topic/conferences/%s";
            //查询是否有人订阅了本（非下级）会议 订阅了话需要向下补发订阅
            String[] destinations = new String[]{subscribeParticipantsDestination, subscribeConferencesDestination};
//            String parentConferenceId = req.getConferenceId() == null ? conferenceId : req.getConferenceId();
//            String parentConferenceId = conferenceId;
            for (String item : destinations) {
                String destinationP = String.format(item, conferenceId);
                String destination = String.format(item, targetConferenceId);
                if (subscribeManageService.hasSubScribe(destinationP)) {
                    log.info("The parent meeting contains a subscription [{}] and the child subscription [{}] needs to be reissued", destinationP, destination);
                    proxySubscribeConferencesService.subscribeChild(targetConferenceId,
                            addCasChannelResp.getRemoteAccessCode(),
                            gwId,
                            destination,
                            conferenceId,
                            destinationP);
                }
            }
        }

        //检查是否需要订阅，完善下级内存
    }

    @Override
    public void delParticipants(List<String> delParticipantIds, String conferenceId, String token, String
            confCasId) throws MyHttpException {
        Map<String, List<String>> confIdToDelListMap = new HashMap<>();
        sortParticipantsByConferenceId(delParticipantIds, conferenceId, confIdToDelListMap);
        for (Map.Entry<String, List<String>> entry : confIdToDelListMap.entrySet()) {
            if (!conferenceId.equals(entry.getKey())) {
                //远端
                ChildMeetingInfo childMeetingInfo = meetingInfoManagerService.getChildByConferenceId(conferenceId, entry.getKey());
                if (childMeetingInfo == null) {
                    log.warn("child meetingInfo not found in conference memory. :{}", entry.getKey());
                    continue;
                }
                remoteGwService.toByGwId(childMeetingInfo.getGwId()).delete(String.format(ConfApiUrl.DELETE_PANTICIPANTS.value(), childMeetingInfo.getId()), entry.getValue());
            } else {
                //本级
                smcMeetingControlService.delParticipants(conferenceId, entry.getValue(), getSmcToken(token));
            }
        }
    }

    private void sortParticipantsByConferenceId(List<String> participantIds, String
            conferenceId, Map<String, List<String>> map) {
        for (String participantId : participantIds) {
            ParticipantInfo participantInfo = participantInfoManagerService.getParticipant(conferenceId, participantId);
            if (participantInfo == null) {
                log.warn("participantId can not found in memory. :{}", participantId);
                List<String> list = map.get(conferenceId);
                if (list == null) {
                    list = new ArrayList<>();
                }
                list.add(participantId);
                map.put(conferenceId, list);
                continue;
            }
            if (map.containsKey(participantInfo.getConferenceId())) {
                List<String> list = map.get(participantInfo.getConferenceId());
                list.add(participantId);
            } else {
                List<String> list = new ArrayList<>();
                list.add(participantId);
                map.put(participantInfo.getConferenceId(), list);
            }
        }
    }

    @Override
    public GetMeetingDetailResponse getMeetingDetail(String conferenceId, String token, String confCasId, String isQueryMultiPicInfo) throws
            MyHttpException, BaseStateException {
        MeetingInfo meetingInfo = meetingInfoManagerService.get(conferenceId);
        GetMeetingDetailResponse response = null;
        if (meetingInfo == null) {
            response = smcMeetingControlService.getMeetingDetail(conferenceId, getSmcToken(token), isQueryMultiPicInfo);
            if (response != null) {
                if (!meetingInfoManagerService.contains(conferenceId)) {
                    meetingInfoManagerService.create(conferenceId, response.getConferenceUiParam().getAccessCode(), response.getConferenceUiParam().getSubject(), true, response.getConferenceState());
                }
                meetingInfo = meetingInfoManagerService.get(conferenceId);
            }
        }
        if (meetingInfo == null) {
            log.warn("meetingInfo can not found in memory. :{}", confCasId);
            throw new BaseStateException("meetingInfo can not found in memory");
            //TODO 抛异常
        }
        String mainConfCasId = meetingInfo.getConfCasId();
        if (confCasId != null && !confCasId.equals(mainConfCasId)) {
            ChildMeetingInfo childMeetingInfo = meetingInfoManagerService.getChildByCasConfId(conferenceId, confCasId);
            if (childMeetingInfo == null) {
                log.warn("confCasId can not found in memory. :{}", confCasId);
                throw new BaseStateException("confCasId can not found in memory");
                //TODO 抛异常
            }
            String respBody = remoteGwService.toByGwId(childMeetingInfo.getGwId()).get(String.format(ConfApiUrl.GET_DETAIL.value(), childMeetingInfo.getId(), confCasId), null).getBody();
            response = JSON.parseObject(respBody, GetMeetingDetailResponse.class);
            return response;
        }
        if (response != null) {
            return response;
        }
        //主席状态和广播状态是级联通道，需要替换级联通道观看的会场Pid
        GetMeetingDetailResponse getMeetingDetailResponse = smcMeetingControlService.getMeetingDetail(conferenceId, getSmcToken(token), isQueryMultiPicInfo);
        //替换会议多画面中级联通道观看的下级会场
        dealMultiPicInfo(conferenceId, getMeetingDetailResponse);
        getMeetingDetailResponse.setSmcVersion(SystemConfiguration.getSmcVersion().getValue());
        return getMeetingDetailResponse;
    }

    private void dealMultiPicInfo(String conferenceId, GetMeetingDetailResponse getMeetingDetailResponse){
        ConferenceState conferenceState = getMeetingDetailResponse.getConferenceState();
        MultiPicInfo multiPicInfo = conferenceState.getMultiPicInfo();
        if (multiPicInfo == null){
            return;
        }
        List<SubPic> subPicList = multiPicInfo.getSubPicList();
        if (subPicList == null){
            return;
        }
        MeetingInfo meetingInfo = meetingInfoManagerService.get(conferenceId);
        for(SubPic subPic: subPicList){
            String participantId = subPic.getParticipantId();
            if(StringUtils.isEmpty(participantId)){
                continue;
            }
            MultiPicInfo cascadeMultiPicInfo = meetingInfo.getCascadeMultiPicInfoMap().get(participantId);
            if (cascadeMultiPicInfo == null){
                continue;
            }
            if(cascadeMultiPicInfo.getPicNum()!=1){
                continue;
            }
            subPic.setParticipantId(cascadeMultiPicInfo.getFirstParticipantId());
        }
    }

    /**
     * 查询会议中的会场 提供缓存
     *
     * @param getParticipantsRequest
     * @param conferenceId
     * @param token
     * @param confCasId
     * @param page
     * @param size
     * @return
     * @throws MyHttpException
     */
    @Override
    public GetParticipantsResponse getParticipants(GetParticipantsRequest getParticipantsRequest, String
            conferenceId, String token, String confCasId, Integer page, Integer size, String towall) throws MyHttpException {
        GetParticipantsResponse response = null;
        if (getParticipantsRequest != null && getParticipantsRequest.getTowallUris() != null) {
            //电视墙请求
            response = towallToGetGetParticipantsResponse(conferenceId, getParticipantsRequest.getTowallUris());
            if (response != null) {
                return response;
            }
        }
        String participantConferenceId = null;
        MeetingInfo meetingInfo = meetingInfoManagerService.get(conferenceId);
        String mainConfCasId = null;
        if (meetingInfo != null) {
            mainConfCasId = meetingInfo.getConfCasId();
        }
        if (confCasId != null && mainConfCasId != null && !confCasId.equals(mainConfCasId)) {
            ChildMeetingInfo childMeetingInfo = meetingInfoManagerService.getChildByCasConfId(conferenceId, confCasId);
            String respBody = remoteGwService.toByGwId(childMeetingInfo.getGwId()).post(String.format(ConfApiUrl.GET_CONDITIONS.value(), childMeetingInfo.getId(), confCasId, page, size), getParticipantsRequest).getBody();
            participantConferenceId = childMeetingInfo.getId();
//            response = JSON.parseObject(respBody, GetParticipantsResponse.class);
            try {
                response = objectMapper.readValue(respBody, GetParticipantsResponse.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            doInitParticipant(response, conferenceId, participantConferenceId);
        } else {
            response = new GetParticipantsResponse();
            List<ParticipantDetail> content = new ArrayList<>();
            response.setContent(content);
            if (getParticipantsRequest != null && getParticipantsRequest.getParticipantIds() != null
                    && getParticipantsRequest.getParticipantIds().size() > 0) {
                Map<String, List<String>> map = new HashMap<>();
                sortParticipantsByConferenceId(getParticipantsRequest.getParticipantIds(), conferenceId, map);
                for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                    String conferenceIdGrouped = entry.getKey(); //分组后的会议id
                    GetParticipantsResponse res;
                    if (conferenceId.equals(conferenceIdGrouped)) {
                        //发往本级
                        res = smcMeetingControlService.getParticipants(conferenceId, page, size, getParticipantsRequest, getSmcToken(token));
                        doInitParticipant(res, conferenceId, conferenceId);
                    } else {
                        //发往下级
                        ChildMeetingInfo childMeetingInfo = meetingInfoManagerService.getChildByConferenceId(conferenceId, conferenceIdGrouped);
                        if(childMeetingInfo == null){
                            continue;
                        }
                        String respBody = remoteGwService.toByGwId(childMeetingInfo.getGwId()).post(String.format(ConfApiUrl.GET_CONDITIONS.value(), childMeetingInfo.getId(), confCasId, page, size), getParticipantsRequest).getBody();
                        res = JSON.parseObject(respBody, GetParticipantsResponse.class);
                        doInitParticipant(res, conferenceId, childMeetingInfo.getId());
                    }
                    if (res != null && res.getContent() != null) {
                        content.addAll(res.getContent());
                    }
                }
                int contentSize = response.getContent().size();
                response.setEmpty(contentSize == 0);
                response.setTotalElements(contentSize);
                response.setFirst(true);
                response.setLast(true);
                response.setNumber(1);
                response.setNumberOfElements(contentSize);
            } else {
                //发往本级
                if (getParticipantsRequest != null && getParticipantsRequest.getHandUp()!= null
                        && getParticipantsRequest.getHandUp() && SystemConfiguration.smcVersionIsV2()) {
                    response = new GetParticipantsResponse();
                    response.setFirst(true);
                    response.setLast(true);
                    response.setTotalPages(1);
                    response.setTotalElements(0);
                    response.setNumber(1);
                    response.setNumberOfElements(0);
                    response.setContent(new ArrayList<>());
                    return response;
                }
                response = smcMeetingControlService.getParticipants(conferenceId, page, size, getParticipantsRequest, getSmcToken(token));
                doInitParticipant(response, conferenceId, conferenceId);
            }
        }
        //删除级联通道
        //response.getContent().removeIf(x->x.getGeneralParam().getVdcMarkCascadeParticipant()!=null);
        return response;
    }

    private void doInitParticipant(GetParticipantsResponse response, String conferenceId, String participantConferenceId) {
        initParticipant(response, conferenceId, participantConferenceId);
        if (conferenceId.equals(participantConferenceId)) {
            videoSourceService.videoSourceHandleByParticipantSelect(conferenceId, response.getContent());
        }
    }

    private Map<String, List<ParticipantInfo>> groupByConferenceId(List<String> participantIds,MeetingInfo meetingInfo) throws MyHttpException {
//        List<String> participantIds = getParticipantsRequest.getParticipantIds();
        List<ParticipantInfo> participantInfos = new ArrayList<>();
        Map<String, ParticipantInfo> allParticipantMap = meetingInfo.getAllParticipantMap();
        for (String participantId : participantIds) {
            ParticipantInfo participantInfo = allParticipantMap.get(participantId);
            participantInfos.add(participantInfo);
        }
        return participantInfos.stream().collect(Collectors.groupingBy(ParticipantInfo::getConferenceId));
    }

    public void initParticipant(GetParticipantsResponse response, String conferenceId, String
            participantConferenceId) {
        //FIXME 可能涉及线程安全问题
        if (participantConferenceId == null) {
            participantConferenceId = conferenceId;
        }
        for (ParticipantDetail participantDetail : response.getContent()) {
            ParticipantGeneralParam generalParam = participantDetail.getGeneralParam();
            ParticipantInfo participantInfo = new ParticipantInfo();
            participantInfo.setParticipantId(generalParam.getId());
            participantInfo.setConferenceId(participantConferenceId);
            participantInfo.setUri(generalParam.getUri());
            participantInfo.setName(generalParam.getName());
            if (generalParam.getVdcMarkCascadeParticipant() != null) {
                CascadeParticipantParameter cascadeParticipantParameter = CascadeParticipantParameter.valueOf(generalParam.getVdcMarkCascadeParticipant());
                participantInfo.setCascadeParticipantParameter(cascadeParticipantParameter);
                //设置级联通道名称
                if(generalParam.getCasChannelName() == null && !participantInfo.isCascadeParticipantH323()){
                    setChannelName(generalParam, cascadeParticipantParameter);
                }
                
                if (conferenceId.equals(participantInfo.getConferenceId())) {
                    //是否welink下级主级联通道
                    if(cascadeParticipantParameter != null && cascadeParticipantParameter.getIndex() == 0
                            && CascadeParticipantDirection.DOWN.equals(cascadeParticipantParameter.getDirection())){
                        String nodeId = cascadeParticipantParameter.getGwId().getNodeId();
                        NodeBusinessType nodeBusinessType = null;
                        if("00000000000000001111111111111111".equals(nodeId)){
                            GwId gwId = routManageService.getWayByGwId(cascadeParticipantParameter.getGwId());
                            if(gwId != null){
                                nodeBusinessType = nodeManageService.getNodeBusinessType(gwId.getNodeId());
                            }
                        }else{
                            nodeBusinessType = nodeManageService.getNodeBusinessType(cascadeParticipantParameter.getGwId().getNodeId());
                        }
                        if(nodeBusinessType != null && (NodeBusinessType.WELINK.equals(nodeBusinessType) || NodeBusinessType.CLOUDLINK.equals(nodeBusinessType))){
                            generalParam.setIsWelink(true);
                        }
                    }

                    Boolean mute = participantDetail.getState().getMute();
                    Boolean quiet = participantDetail.getState().getQuiet();
                    Integer videoSwitchAttribute = participantDetail.getState().getVideoSwitchAttribute();
                    meetingService.CascadeParticipantStatusHandle(conferenceId, generalParam.getId(), participantInfo.getCascadeParticipantParameter().isMain(), mute, quiet, videoSwitchAttribute);
                }
            }
            participantInfo.setOnline(participantDetail.getState().getOnline());
            participantInfo.setMultiPicInfo(JSON.parseObject(JSON.toJSONString(participantDetail.getState().getMultiPicInfo()), MultiPicInfo.class));
            if(generalParam.getSubTpParams() != null){
                initTPParticipantInfo(conferenceId, participantConferenceId, participantDetail);
            }
            participantInfoManagerService.addParticipant(conferenceId, participantInfo);
        }
    }

    private void setChannelName(ParticipantGeneralParam generalParam, CascadeParticipantParameter cascadeParticipantParameter) {
        try {
            GwId gwId = routManageService.getCompleteGwIdBy(cascadeParticipantParameter.getGwId());
            if(gwId == null){
                return;
            }
            GwNode moGwNode = nodeManageService.getByNodeId(gwId.getNodeId());
            String casChannelName = moGwNode.getName();
            boolean isChange = true;
            if(StringUtils.isEmpty(casChannelName)){
                casChannelName = generalParam.getName();
                isChange = false;
            }
//            GwId realId = routManageService.getWayByGwId(cascadeParticipantParameter.getGwId());
//            String casChannelName = null;
//
//            if (realId == null) {
//                NodeData nodeData = nodeDataService.getOneByGwId(cascadeParticipantParameter.getGwId());
//                if (nodeData == null) {
//                    VmNodeData vmNodeData = vmNodeDataService.getOneById(cascadeParticipantParameter.getGwId().getNodeId());
//                    if (vmNodeData == null) {
//                        casChannelName = generalParam.getName();
//                        isChange = false;
//                    } else {
//                        casChannelName = vmNodeData.getName();
//                    }
//                } else {
//                    casChannelName = nodeData.getName();
//                }
//            } else {
//                GwNode gwNode = nodeManageService.getGwNodeById(realId.getNodeId());
//                if (gwNode == null) {
//                    NodeData nodeData = nodeDataService.getOneByGwId(realId);
//                    if (nodeData == null) {
//                        VmNodeData vmNodeData = vmNodeDataService.getOneById(cascadeParticipantParameter.getGwId().getNodeId());
//                        if (vmNodeData == null) {
//                            casChannelName = generalParam.getName();
//                            isChange = false;
//                        } else {
//                            casChannelName = vmNodeData.getName();
//                        }
//                    } else {
//                        casChannelName = nodeData.getName();
//                    }
//                } else {
//                    if(!gwNode.getId().equals(realId.getNodeId())){
//                        VmNodeData vmNodeData = vmNodeDataService.getOneById(cascadeParticipantParameter.getGwId().getNodeId());
//                        if (vmNodeData == null) {
//                            casChannelName = generalParam.getName();
//                            isChange = false;
//                        } else {
//                            casChannelName = vmNodeData.getName();
//                        }
//                    }else{
//                        casChannelName = gwNode.getName();
//                    }
//                }
//            }
            if (isChange) {
                casChannelName = casChannelName + "(" + (cascadeParticipantParameter.getIndex() + 1) + ")";
            }
            generalParam.setCasChannelName(casChannelName);
        }catch (Exception e){
            log.error("except in change channel name. {}", e);
        }
    }

    private void initTPParticipantInfo(String conferenceId, String participantConferenceId, ParticipantDetail participantDetail) {
        for(TpGeneralParam tpGeneralParam: participantDetail.getGeneralParam().getSubTpParams()){
            ParticipantInfo tpParticipantInfo = new ParticipantInfo();
            tpParticipantInfo.setConferenceId(participantConferenceId);
            tpParticipantInfo.setParticipantId(tpGeneralParam.getId());
            tpParticipantInfo.setUri(tpGeneralParam.getUri());
            tpParticipantInfo.setName(tpGeneralParam.getName());
            tpParticipantInfo.setOnline(false);
            if(participantDetail.getSubTpState() != null){
                for(ParticipantState participantState: participantDetail.getSubTpState()){
                    if(!participantState.getParticipantId().equals(tpGeneralParam.getId())){
                        continue;
                    }
                    tpParticipantInfo.setMultiPicInfo(participantState.getMultiPicInfo());
                    tpParticipantInfo.setOnline(participantState.getOnline());
                }
            }
            participantInfoManagerService.addParticipant(conferenceId, tpParticipantInfo);
        }
    }





    private GetParticipantsResponse towallToGetGetParticipantsResponse(String conferenceId, List<String> towallUris) {
        Map<String, ParticipantInfo> participantsMap = participantInfoManagerService.getAllParticipant(conferenceId);
        if (participantsMap == null) {
            log.warn("conferenceId can not found in memory. :{}", conferenceId);
            //TODO 抛异常
            return null;
        }
        GetParticipantsResponse getParticipantsResponse = new GetParticipantsResponse();
        List<ParticipantDetail> content = new ArrayList<>();
        for (String uri : towallUris) {
            boolean isFound = false;
            String screen = "";
            if(uri.contains("(")){
                String URI = uri.substring(0, uri.indexOf("("));
                screen = uri.substring(uri.indexOf("("));
                uri = URI;
            }
            for (Map.Entry<String, ParticipantInfo> entry : participantsMap.entrySet()) {
                ParticipantInfo participantInfo = entry.getValue();
                if (!participantInfo.getUri().equals(uri)) {
                    continue;
                }
                ParticipantDetail participantDetail = new ParticipantDetail();
                ParticipantGeneralParam participantGeneralParam = new ParticipantGeneralParam();
                participantGeneralParam.setId(participantInfo.getParticipantId());
                participantGeneralParam.setUri(participantInfo.getUri()+screen);
                participantDetail.setGeneralParam(participantGeneralParam);
                content.add(participantDetail);
                isFound = true;
                break;
            }
            if (!isFound) {
                log.warn("uri can not found in memory. :{}", uri);
                return null;
            }
        }
        getParticipantsResponse.setContent(content);
        return getParticipantsResponse;
    }


    @Override
    public void cameraControl(CameraControlRequest cameraControlRequest, String conferenceId, String
            participantId, String token, String confCasId) throws MyHttpException {
        ParticipantInfo participantInfo = participantInfoManagerService.getParticipant(conferenceId, participantId);
        if (participantInfo == null) {
            log.warn("participantId can not found in memory. :{}", participantId);
            //TODO 抛异常
            return;
        }
        if (conferenceId.equals(participantInfo.getConferenceId())) {
            smcMeetingControlService.cameraControl(conferenceId, participantId, cameraControlRequest, getSmcToken(token));
        } else {
            ChildMeetingInfo childMeetingInfo = meetingInfoManagerService.getChildByConferenceId(conferenceId, participantInfo.getConferenceId());
            if (childMeetingInfo == null) {
                log.warn("Conference Id can not found in memory:{}", participantInfo.getConferenceId());
                //TODO 抛异常
                return;
            }
            remoteGwService.toByGwId(childMeetingInfo.getGwId()).post(String.format(ConfApiUrl.CAMERA_CONTROL.value(), childMeetingInfo.getId(), participantId), cameraControlRequest);
        }
    }

    @Override
    public void participantsFellow(ParticipantsFellowRequest participantsFellowRequest, String conferenceId, String
            participantId, String token, String confCasId) throws MyHttpException {
        //只能本级
        String targetPid = participantsFellowRequest.getSourceId();
        ParticipantInfo participantInfo = participantInfoManagerService.getParticipant(conferenceId, targetPid);
        if(participantInfo == null || !conferenceId.equals(participantInfo.getConferenceId())){
//          非本级会场不允许观看
            throw new MyHttpException(409, GwErrorCode.LOWER_PARTICIPANT_CAN_NOT_FELLOW.toString());
        }
        smcMeetingControlService.participantsFellow(conferenceId, participantId, participantsFellowRequest, getSmcToken(token));
    }


    private void chatMicBefore(String conferenceId, ChatMicRequest chatMicRequest, String smcToken) throws MyHttpException {
        if (SystemConfiguration.smcVersionIsV2()) {
            GetParticipantsResponse getParticipantsResponse = smcMeetingControlService.getParticipants(conferenceId, 1, 1000, null, smcToken);
            List<ParticipantsControlRequest> request = new ArrayList<>();
            for (ParticipantDetail item : getParticipantsResponse.getContent()) {
                if (!chatMicRequest.getExcludeParticipants().contains(item.getGeneralParam().getId())) {
                    ParticipantsControlRequest participantsControlRequest = new ParticipantsControlRequest();
                    participantsControlRequest.setId(item.getGeneralParam().getId());
                    participantsControlRequest.setIsMute(chatMicRequest.getSet());
                    request.add(participantsControlRequest);
                }
            }
            smcMeetingControlService.participantsControl(conferenceId, request, smcToken);
        } else {
            smcMeetingControlService.chatMic(conferenceId, chatMicRequest, smcToken);
        }
    }


    @Override
    public void chatMic(String conferenceId, ChatMicRequest chatMicRequest, String token) throws MyHttpException {
        //按会议Id分类
        MeetingInfo meetingInfo = meetingInfoManagerService.get(conferenceId);
        for (ChildMeetingInfo childMeetingInfo : meetingInfo.getChildMeetingInfoMap().values()) {
            if (childMeetingInfo.getId() == null) {
                continue;
            }
            if (meetingInfoManagerService.childIsOnline(conferenceId, childMeetingInfo.getId())) {
                remoteGwService.toByGwId(childMeetingInfo.getGwId()).post(String.format(ConfApiUrl.CHAT_MIC.value(), childMeetingInfo.getId()), chatMicRequest);
            }
        }
        List<String> chatMicList = chatMicRequest.getExcludeParticipants();
        if (chatMicRequest.getSet()) {
            Map<String, ParticipantInfo> localCasParticipant = participantInfoManagerService.getLocalCasParticipant(conferenceId);
            for (ParticipantInfo casParticipantInfo : localCasParticipant.values()) {
                chatMicList.add(casParticipantInfo.getParticipantId());
            }
        }
        chatMicBefore(conferenceId, chatMicRequest, getSmcToken(token));
    }


    private void chatSpeakerBefore(String conferenceId, ChatSpeakerRequest chatSpeakerRequest, String smcToken) throws MyHttpException {
        if (SystemConfiguration.smcVersionIsV2()) {
            GetParticipantsResponse getParticipantsResponse = smcMeetingControlService.getParticipants(conferenceId, 1, 1000, null, smcToken);
            List<ParticipantsControlRequest> request = new ArrayList<>();
            for (ParticipantDetail item : getParticipantsResponse.getContent()) {
                if (!chatSpeakerRequest.getExcludeParticipants().contains(item.getGeneralParam().getId())) {
                    ParticipantsControlRequest participantsControlRequest = new ParticipantsControlRequest();
                    participantsControlRequest.setId(item.getGeneralParam().getId());
                    participantsControlRequest.setIsQuiet(chatSpeakerRequest.getSet());
                    request.add(participantsControlRequest);
                }
            }
            smcMeetingControlService.participantsControl(conferenceId, request, smcToken);
        } else {
            smcMeetingControlService.chatSpeaker(conferenceId, chatSpeakerRequest, smcToken);
        }
    }


    @Override
    public void chatSpeaker(String conferenceId, ChatSpeakerRequest chatSpeakerRequest, String token) throws
            MyHttpException {
        //按会议Id分类
        MeetingInfo meetingInfo = meetingInfoManagerService.get(conferenceId);
        for (ChildMeetingInfo childMeetingInfo : meetingInfo.getChildMeetingInfoMap().values()) {
            if (childMeetingInfo.getId() == null) {
                continue;
            }
            if (meetingInfoManagerService.childIsOnline(conferenceId, childMeetingInfo.getId())) {
                remoteGwService.toByGwId(childMeetingInfo.getGwId()).post(String.format(ConfApiUrl.CHAT_SPEAKER.value(), childMeetingInfo.getId()), chatSpeakerRequest);
            }
        }
        List<String> speakerList = chatSpeakerRequest.getExcludeParticipants();
        if (chatSpeakerRequest.getSet()) {
            Map<String, ParticipantInfo> localCasParticipant = participantInfoManagerService.getLocalCasParticipant(conferenceId);
            for (ParticipantInfo casParticipantInfo : localCasParticipant.values()) {
                speakerList.add(casParticipantInfo.getParticipantId());
            }
        }
        chatSpeakerBefore(conferenceId, chatSpeakerRequest, getSmcToken(token));
    }

    @Override
    public void addAttendees(String conferenceId, List<AttendeeReq> addAttendeesRequest, String token, String
            confCasId) throws MyHttpException {
        //TODO 和添加会场一样
        String mainConfCasId = meetingInfoManagerService.get(conferenceId).getConfCasId();
        if (confCasId != null && !confCasId.equals(mainConfCasId)) {
            ChildMeetingInfo childMeetingInfo = meetingInfoManagerService.getChildByCasConfId(conferenceId, confCasId);
            remoteGwService.toByGwId(childMeetingInfo.getGwId()).post(String.format(ConfApiUrl.ADD_ATTENDEES.value(), childMeetingInfo.getId()), addAttendeesRequest);
            return;
        }
        smcMeetingControlService.addAttendees(conferenceId, addAttendeesRequest, getSmcToken(token));
    }

    @Override
    public List<GetParticipantsResponse> getParticipantsBriefs(String conferenceId, List<String> request, String
            token) throws MyHttpException {
        Map<String, List<String>> confIdToParticipantListMap = new HashMap<>();
        sortParticipantsByConferenceId(request, conferenceId, confIdToParticipantListMap);
        List<GetParticipantsResponse> respList = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : confIdToParticipantListMap.entrySet()) {
            if (!conferenceId.equals(entry.getKey())) {
                //远端
                ChildMeetingInfo childMeetingInfo = meetingInfoManagerService.getChildByConferenceId(conferenceId, entry.getKey());
                if (childMeetingInfo == null) {
                    log.warn("child meetingInfo not found in conference memory. :{}", entry.getKey());
                    //TODO 抛异常
                    return null;
                }
                String respBody = remoteGwService.toByGwId(childMeetingInfo.getGwId()).post(String.format(ConfApiUrl.GET_BRIEFS.value(), childMeetingInfo.getId()), entry.getValue()).getBody();
                List<GetParticipantsResponse> getParticipantsResponses = JSON.parseObject(respBody, new TypeReference<List<GetParticipantsResponse>>() {
                });
                respList.addAll(getParticipantsResponses);
            } else {
                List<GetParticipantsResponse> getParticipantsResponses = smcMeetingControlService.getParticipantsBriefs(conferenceId, request, getSmcToken(token));
                respList.addAll(getParticipantsResponses);
            }
        }
        return respList;
    }

    @Override
    public GetParticipantsDetailInfoResponse getParticipantsDetailInfo(String conferenceId, String
            participantId, String token) throws MyHttpException {
        ParticipantInfo participantInfo = participantInfoManagerService.getParticipant(conferenceId, participantId);
        if (participantInfo == null) {
            log.warn("participantId can not found in memory. :{}", participantId);
            //TODO 抛异常
            return null;
        }
        if (conferenceId.equals(participantInfo.getConferenceId())) {
//            if (SystemConfiguration.smcVersionIsV2()) {
//                GetParticipantsDetailInfoResponse response = new GetParticipantsDetailInfoResponse();
//                GetParticipantsRequest getParticipantsRequest = new GetParticipantsRequest();
//                ArrayList<String> participantIds = new ArrayList<>();
//                participantIds.add(participantId);
//                getParticipantsRequest.setParticipantIds(participantIds);
//                GetParticipantsResponse getParticipantsResponse = smcMeetingControlService.getParticipants(conferenceId, 0, 100, getParticipantsRequest, getSmcToken(token));
//                if (!CollectionUtils.isEmpty(getParticipantsResponse.getContent())) {
//                    List<ParticipantDetail> content = getParticipantsResponse.getContent();
//                    for(ParticipantDetail participantDetail : content){
//                        ParticipantGeneralParam generalParam = participantDetail.getGeneralParam();
//                        if(generalParam.getId().equals(participantId) || generalParam.getUri().equals(participantId)){
//                            response.setName(generalParam.getName());
//                            response.setUri(generalParam.getUri());
//                            response.setMcuName(generalParam.getMcuName());
//                            //SMC2.0接口不支持查询会场类型  写死sip
//                            response.setIpProtocolType(1);
//                        }
//                    }
//                }
//                return response;
//
//            } else {
                return smcMeetingControlService.getParticipantsDetailInfo(conferenceId, participantId, getSmcToken(token));
//            }
        } else {
            ChildMeetingInfo childMeetingInfo = meetingInfoManagerService.getChildByConferenceId(conferenceId, participantInfo.getConferenceId());
            if (childMeetingInfo == null) {
                log.warn("Conference Id can not found in memory:{}", participantInfo.getConferenceId());
                //TODO 抛异常
                return null;
            }
            String respBody = remoteGwService.toByGwId(childMeetingInfo.getGwId()).get(String.format(ConfApiUrl.GET_PARTICIPANT_DETAILINFO.value(), childMeetingInfo.getId(), participantId), null).getBody();
            return JSON.parseObject(respBody, GetParticipantsDetailInfoResponse.class);
        }
    }

    @Override
    public GetParticipantsCapabilityResponse getParticipantsCapability(String conferenceId, String
            participantId, String token) throws MyHttpException {
        ParticipantInfo participantInfo = participantInfoManagerService.getParticipant(conferenceId, participantId);
        if (participantInfo == null) {
            log.warn("participantId can not found in memory. :{}", participantId);
            //TODO 抛异常
            return null;
        }
        if (conferenceId.equals(participantInfo.getConferenceId())) {
            return smcMeetingControlService.getParticipantsCapability(conferenceId, participantId, getSmcToken(token));

        } else {
            ChildMeetingInfo childMeetingInfo = meetingInfoManagerService.getChildByConferenceId(conferenceId, participantInfo.getConferenceId());
            if (childMeetingInfo == null) {
                log.warn("Conference Id can not found in memory:{}", participantInfo.getConferenceId());
                //TODO 抛异常
                return null;
            }
            String respBody = remoteGwService.toByGwId(childMeetingInfo.getGwId()).get(String.format(ConfApiUrl.GET_PARTICIPANT_CAPABILITY.value(), childMeetingInfo.getId(), participantId), null).getBody();
            return JSON.parseObject(respBody, GetParticipantsCapabilityResponse.class);

        }
    }

    @Override
    public void setCommonlyUsedParticipants(String conferenceId, SetCommonlyUsedParticipantsRequest request, String
            token) throws MyHttpException {
        String participantList = request.getParticipantIdList();
        String[] arr = participantList.split(",");
        Map<String, String> confIdToparticipantListMap = new HashMap<>();
        for (String participantId : arr) {
            ParticipantInfo participantInfo = participantInfoManagerService.getParticipant(conferenceId, participantId);
            if (participantInfo == null) {
                log.warn("participantId can not found in memory. :{}", participantId);
                continue;
            }
            String confId = participantInfo.getConferenceId();
            if (confIdToparticipantListMap.containsKey(confId)) {
                String participantIdList = confIdToparticipantListMap.get(confId) + "," + participantId;
                confIdToparticipantListMap.put(confId, participantIdList);
            } else {
                confIdToparticipantListMap.put(confId, participantId);
            }
        }

        for (Map.Entry<String, String> entry : confIdToparticipantListMap.entrySet()) {
            if (!conferenceId.equals(entry.getKey())) {
                //远端
                ChildMeetingInfo childMeetingInfo = meetingInfoManagerService.getChildByConferenceId(conferenceId, entry.getKey());
                if (childMeetingInfo == null) {
                    log.warn("child meetingInfo not found in conference memory. :{}", entry.getKey());
                    //TODO 抛异常
                    return;
                }
                SetCommonlyUsedParticipantsRequest setCommonlyUsedParticipantsRequest = new SetCommonlyUsedParticipantsRequest();
                setCommonlyUsedParticipantsRequest.setSet(request.getSet());
                setCommonlyUsedParticipantsRequest.setParticipantIdList(entry.getValue());
                remoteGwService.toByGwId(childMeetingInfo.getGwId()).put(String.format(ConfApiUrl.SET_COMMONLY_USED_PARTICIPANTS.value(), childMeetingInfo.getId()), setCommonlyUsedParticipantsRequest);
            } else {
                //本级添加本级级联通道
                request.setParticipantIdList(entry.getValue());
                smcMeetingControlService.setCommonlyUsedParticipants(conferenceId, request, getSmcToken(token));
            }
        }
    }

    @Override
    public GetCommonlyUsedParticipantsResponse getCommonlyUsedParticipants(String
                                                                                   conferenceId, GetCommonlyUsedParticipantsRequest request, String token) throws MyHttpException {
        //查询常用会场列表
        //是否查所有层级
        return smcMeetingControlService.getCommonlyUsedParticipants(conferenceId, request, getSmcToken(token));
    }

    @Override
    public void subscribeParticipantsStatus(String conferenceId, String groupId, SubscribeParticipantsStatusRequest
            request, String token) throws MyHttpException {
        smcMeetingControlService.subscribeParticipantsStatus(conferenceId, groupId, request, getSmcToken(token));
    }

    @Override
    public void unSubscribeParticipantsStatus(String conferenceId, String groupId, String token) throws
            MyHttpException {
        smcMeetingControlService.unSubscribeParticipantsStatus(conferenceId, groupId, getSmcToken(token));
    }

    @Override
    public void subscribeParticipantsStatusRealTime(String conferenceId, String
            groupId, List<String> request, String token) throws MyHttpException {
        smcMeetingControlService.subscribeParticipantsStatusRealTime(conferenceId, groupId, request, getSmcToken(token));
    }

    @Override
    public void unSubscribeParticipantsStatusRealTime(String conferenceId, String groupId, String token) throws
            MyHttpException {
        smcMeetingControlService.unSubscribeParticipantsStatusRealTime(conferenceId, groupId, getSmcToken(token));
    }

    @Override
    public void setRemind(String conferenceId, String participantId, String token) throws MyHttpException {
        ParticipantInfo participantInfo = participantInfoManagerService.getParticipant(conferenceId, participantId);
        if (participantInfo == null) {
            log.warn("participantId can not found in memory. :{}", participantId);
            //TODO 抛异常
            return;
        }
        if (conferenceId.equals(participantInfo.getConferenceId())) {
            smcMeetingControlService.setRemind(conferenceId, participantId, getSmcToken(token));

        } else {
            ChildMeetingInfo childMeetingInfo = meetingInfoManagerService.getChildByConferenceId(conferenceId, participantInfo.getConferenceId());
            if (childMeetingInfo == null) {
                log.warn("Conference Id can not found in memory:{}", participantInfo.getConferenceId());
                //TODO 抛异常
                return;
            }
            remoteGwService.toByGwId(childMeetingInfo.getGwId()).post(String.format(ConfApiUrl.SET_REMIND.value(), childMeetingInfo.getId(), participantId), null);

        }
    }

    @Override
    public void setChairmanPoll(String conferenceId, SetChairmanPollRequest request, String token) throws
            MyHttpException {

        //TODO 在smc侧的轮询，无法跨级
        smcMeetingControlService.setChairmanPoll(conferenceId, request, getSmcToken(token));
    }

    @Override
    public GetChairmanPollResponse getChairmanPoll(String conferenceId, String token) throws MyHttpException {
        return smcMeetingControlService.getChairmanPoll(conferenceId, getSmcToken(token));
    }

    @Override
    public void setBroadcastPoll(String conferenceId, SetBroadcastPollRequest request, String token) throws
            MyHttpException {
        //TODO 在smc侧
        smcMeetingControlService.setBroadcastPoll(conferenceId, request, getSmcToken(token));
    }

    @Override
    public GetBroadcastPollResponse getBroadcastPoll(String conferenceId, String token) throws MyHttpException {
        return smcMeetingControlService.getBroadcastPoll(conferenceId, getSmcToken(token));
    }

    @Override
    public void setMultiPicPoll(String conferenceId, SetMultiPicPollRequest request, String token) throws
            MyHttpException {
        //TODO 在smc侧
        smcMeetingControlService.setMultiPicPoll(conferenceId, request, getSmcToken(token));
    }

    @Override
    public GetMultiPicPollResponse getMultiPicPoll(String conferenceId, String token) throws MyHttpException {
        return smcMeetingControlService.getMultiPicPoll(conferenceId, getSmcToken(token));
    }

    @Override
    public SetParticipantsParameterResponse setParticipantsParameter(String conferenceId, String
            participantId, SetParticipantsParameterRequest request, String token) throws MyHttpException {
        ParticipantInfo participantInfo = participantInfoManagerService.getParticipant(conferenceId, participantId);
        if (participantInfo == null) {
            log.warn("participantId can not found in memory. :{}", participantId);
            //TODO 抛异常
            return null;
        }
        if (conferenceId.equals(participantInfo.getConferenceId())) {
            return smcMeetingControlService.setParticipantsParameter(conferenceId, participantId, request, getSmcToken(token));

        } else {
            ChildMeetingInfo childMeetingInfo = meetingInfoManagerService.getChildByConferenceId(conferenceId, participantInfo.getConferenceId());
            if (childMeetingInfo == null) {
                log.warn("Conference Id can not found in memory:{}", participantInfo.getConferenceId());
                //TODO 抛异常
                return null;
            }
            String respBody = remoteGwService.toByGwId(childMeetingInfo.getGwId()).post(String.format(ConfApiUrl.SET_PARTICIPANTS_PARAMETER.value(), childMeetingInfo.getId(), participantId), request).getBody();
            return JSON.parseObject(respBody, SetParticipantsParameterResponse.class);
        }
    }

    @Override
    public void migrate(String conferenceId, MigrateRequest request, String token) throws MyHttpException {
        //请求参数体两参数只可填其一
        String targetConferenceId = request.getTargetConferenceId();
        if (targetConferenceId != null) {
            MigrateRequest migrateRequest = new MigrateRequest();
            migrateRequest.setTargetConferenceId(request.getTargetConferenceId());
            migrateRequest.setMigrateInfos(null);
            smcMeetingControlService.migrate(conferenceId, migrateRequest, getSmcToken(token));
            return;
        }
        List<MigrateInfo> list = request.getMigrateInfos();
        Map<String, List<MigrateInfo>> map = new HashMap<>();
        for (MigrateInfo migrateInfo : list) {
            String participantId = migrateInfo.getParticipantId();
            ParticipantInfo participantInfo = participantInfoManagerService.getParticipant(conferenceId, participantId);
            if (participantInfo == null) {
                log.warn("participantId can not found in memory. :{}", participantId);
                continue;
            }
            if (map.containsKey(participantInfo.getConferenceId())) {
                List<MigrateInfo> migrateInfos = map.get(participantInfo.getConferenceId());
                migrateInfos.add(migrateInfo);
            } else {
                List<MigrateInfo> migrateInfos = new ArrayList<>();
                migrateInfos.add(migrateInfo);
                map.put(participantInfo.getConferenceId(), migrateInfos);
            }
        }
        for (Map.Entry<String, List<MigrateInfo>> entry : map.entrySet()) {
            if (!conferenceId.equals(entry.getKey())) {
                //远端
                ChildMeetingInfo childMeetingInfo = meetingInfoManagerService.getChildByConferenceId(conferenceId, entry.getKey());
                if (childMeetingInfo == null) {
                    log.warn("child meetingInfo not found in conference memory. :{}", entry.getKey());
                    //TODO 抛异常
                    return;
                }
                remoteGwService.toByGwId(childMeetingInfo.getGwId()).post(String.format(ConfApiUrl.MIGRATE.value(), childMeetingInfo.getId()), entry.getValue());
            } else {
                //本级
                for (MigrateInfo migrateInfo : entry.getValue()) {
                    MultiPicInfo multiPicInfo = migrateInfo.getMultiPicInfo();
                    String participantId = migrateInfo.getParticipantId();
                    MyHttpException exception = dealParticipantControlMultiPicInfo(multiPicInfo, null, conferenceId, participantId, MeetingControlType.WATCH);
                    if (exception != null) {
                        throw exception;
                    }
                    migrateInfo.setMultiPicInfo(multiPicInfo);

                }
                MigrateRequest migrateRequest = new MigrateRequest();
                migrateRequest.setTargetConferenceId(null);
                migrateRequest.setMigrateInfos(entry.getValue());
                smcMeetingControlService.migrate(conferenceId, migrateRequest, getSmcToken(token));
            }
        }
    }

    @Override
    public List<VideoSrcInfo> getVideoSource(String conferenceId, List<String> request, String token) throws
            MyHttpException {
        Map<String, List<String>> confIdToParticipantListMap = new HashMap<>();
        sortParticipantsByConferenceId(request, conferenceId, confIdToParticipantListMap);
        List<VideoSrcInfo> respList = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : confIdToParticipantListMap.entrySet()) {
            if (!conferenceId.equals(entry.getKey())) {
                //远端
                ChildMeetingInfo childMeetingInfo = meetingInfoManagerService.getChildByConferenceId(conferenceId, entry.getKey());
                if (childMeetingInfo == null) {
                    log.warn("child meetingInfo not found in conference memory. :{}", entry.getKey());
                    //TODO 抛异常
                    return null;
                }
                String respBody = remoteGwService.toByGwId(childMeetingInfo.getGwId()).post(String.format(ConfApiUrl.GET_VIDEO_SOURCE.value(), childMeetingInfo.getId()), entry.getValue()).getBody();
                List<VideoSrcInfo> VideoSrcInfos = JSON.parseObject(respBody, new TypeReference<List<VideoSrcInfo>>() {
                });
                respList.addAll(VideoSrcInfos);
            } else {
                //本级添加本级级联通道
                List<VideoSrcInfo> VideoSrcInfos = smcMeetingControlService.getVideoSource(conferenceId, entry.getValue(), getSmcToken(token));
                respList.addAll(VideoSrcInfos);
            }
        }
        return respList;
    }

    @Override
    public void rseStream(String conferenceId, RseStreamRequest request, String token) throws MyHttpException {
        smcMeetingControlService.rseStream(conferenceId, request, getSmcToken(token));
    }

    @Override
    public void batchTextTips(String conferenceId, BatchTextTipsRequest request, String token) throws
            MyHttpException {
        Map<String, List<String>> confIdToParticipantListMap = new HashMap<>();
        sortParticipantsByConferenceId(request.getParticipantIds(), conferenceId, confIdToParticipantListMap);
        for (Map.Entry<String, List<String>> entry : confIdToParticipantListMap.entrySet()) {
            if (!conferenceId.equals(entry.getKey())) {
                //远端
                ChildMeetingInfo childMeetingInfo = meetingInfoManagerService.getChildByConferenceId(conferenceId, entry.getKey());
                if (childMeetingInfo == null) {
                    log.warn("child meetingInfo not found in conference memory. :{}", entry.getKey());
                    //TODO 抛异常
                    return;
                }
                BatchTextTipsRequest batchTextTipsRequest = new BatchTextTipsRequest();
                batchTextTipsRequest.setTextTip(request.getTextTip());
                batchTextTipsRequest.setParticipantIds(entry.getValue());
                remoteGwService.toByGwId(childMeetingInfo.getGwId()).post(String.format(ConfApiUrl.BATCH_TEXT_TIIPS.value(), childMeetingInfo.getId()), batchTextTipsRequest);
            } else {
                //本级添加本级级联通道
                request.setParticipantIds(entry.getValue());
                smcMeetingControlService.batchTextTips(conferenceId, request, getSmcToken(token));
            }
        }
    }

    @Override
    public void updateSubscribeParticipantsStatus(String conferenceId, String
            groupId, UpdateSubscribeParticipantsStatusRequest request, String token) throws MyHttpException {
        smcMeetingControlService.updateSubscribeParticipantsStatus(conferenceId, groupId, request, getSmcToken(token));
    }

    @Override
    public void pushAiCaption(String conferenceId, String request, String token) throws MyHttpException {
        smcMeetingControlService.pushAiCaption(conferenceId, request, getSmcToken(token));
        for (ChildMeetingInfo item : meetingInfoManagerService.get(conferenceId).getChildMeetingInfoMap().values()) {
            if (item.getId() == null) {
                continue;
            }
            if (meetingInfoManagerService.childIsOnline(conferenceId, item.getId())) {
                remoteGwService.toByGwId(item.getGwId()).post(String.format(ConfApiUrl.PUSH_AI_CAPTION.value(), item.getId()), request);
            }
        }
    }

    @Override
    public GetPresetParamResponse getPresetParam(String conferenceId, String token) throws MyHttpException {
        return smcMeetingControlService.getPresetParam(conferenceId, getSmcToken(token));
    }


    public void commonControl(String conferenceId, MeetingControlType meetingControlType, String
            pid, MeetingControlRequestEx requestEx, String
                                      token) throws MyHttpException {
        if (requestEx == null) {
            requestEx = new MeetingControlRequestEx();
        }
        requestEx.assignValueAccordingToType(meetingControlType, pid);
        if ("".equals(pid)) {
            meetingControlBefore(conferenceId, requestEx.toMeetingControlRequest(), getSmcToken(token));
            sendChild(conferenceId, requestEx);
        } else {
            if (pid.contains(CoreConfig.PARTICIPANT_SIGN)) {
                CasChannelParameter casChannelParameter = CasChannelParameter.valueOf(pid);
                if(casChannelParameter != null){
                    pid = cascadeChannelManageService.getCascadeChannelOne(conferenceId, casChannelParameter.getDirection(), casChannelParameter.getConfId(), casChannelParameter.getIndex()).getParticipantId();
                }
            }
            //判断是否有上级，并请求来自上级，否，设置:上级主通道
            ParticipantInfo participantInfo = participantInfoManagerService.getParticipant(conferenceId, pid);
            if (participantInfo == null) {
                if (requestEx.remoteV2()) {
//                    MeetingControlRequest requestLocal = new MeetingControlRequest();
//                    requestLocal.assignValueAccordingToType(meetingControlType, "");
//                    meetingControlBefore(conferenceId, requestLocal, getSmcToken(token));
                    childCasChannelControl(conferenceId, pid, meetingControlType);
                } else {
                    log.warn("participantId can not found in memory:{}", pid);
                    meetingControlBefore(conferenceId, requestEx.toMeetingControlRequest(), getSmcToken(token));
                }
                return;
            }

            /**
             * 需要排除的下级会议号 优先级会控广播使用的
             */
            String excludeChildConferenceId = null;
            //如果请求不是来自上级
            if (!requestEx.fromUp()) {
                switch (meetingControlType) {
                    case BROADCASTER:
                    case SPOKESMAN: {
                        //检测本级是否有广播 优先级会控
                        MeetingInfo meetingInfo = meetingInfoManagerService.get(conferenceId);
                        String broadcastId = meetingInfo.getConferenceState().getBroadcastId();
                        if ((!StringUtils.isEmpty(broadcastId))) {
                            ParticipantInfo broadcastParticipantInfo = meetingInfo.getLocalParticipant().get(broadcastId);
                            //广播的必须是级联通道
                            if (broadcastParticipantInfo != null && broadcastParticipantInfo.isCascadeParticipant()) {
                                //检测上级的主通道是否看着广播的会场
                                CascadeChannelInfo cascadeChannelInfoUPMain = cascadeChannelManageService.getCascadeChannelMain(conferenceId, CascadeParticipantDirection.UP, null);
                                if (cascadeChannelInfoUPMain != null) {
                                    ParticipantInfo localUpCasParticipantMain = meetingInfo.getAllParticipantMap().get(cascadeChannelInfoUPMain.getParticipantId());
                                    //获取上级是否广播本级主级联通道
                                    try {
                                        CascadeParticipantParameter cascadeParticipantParameter = localUpCasParticipantMain.getCascadeParticipantParameter();
                                        GetChannelStatusReq getChannelStatusReq = new GetChannelStatusReq();
                                        getChannelStatusReq.setConfCasId(localUpCasParticipantMain.getConfCasId());
                                        getChannelStatusReq.setChildCondId(meetingInfo.getId());
                                        String result = remoteGwService.toByGwId(cascadeParticipantParameter.getGwId()).post(ConfApiUrl.CASCADE_CHANNEL_STATUS.value(), getChannelStatusReq).getBody();
                                        JSONObject jsonObject = JSONObject.parseObject(result);
                                        if(!jsonObject.getBoolean("status")){
                                            break;
                                        }
                                    } catch (MyHttpException e) {
                                        log.error("MyHttpException: {}, {}, {}, {}",e.getCode(),e.getBody(),e.getMessage(),e.getStackTrace());
                                        e.printStackTrace();
                                    }
                                    if (localUpCasParticipantMain.getMultiPicInfo() != null) {
                                        //上级级联通道看的是广播的下级级联通道  下级操作就不往这级发送了
                                        if (broadcastId.equals(localUpCasParticipantMain.getMultiPicInfo().getFirstParticipantId())) {
                                            if (!conferenceId.equals(participantInfo.getConferenceId())) {
                                                //TODO 抛错误 不允许广播非本级的会场
                                                //TODO 抛错误 不允许广播非本级的会场
                                                //TODO 抛错误 不允许广播非本级的会场
                                                throw new MyHttpException(409,"{\"errorType\":\"SMC2.0\", \"errorNo\": \"50331836\", \"errorDesc\":\"The participant whose video source is locked cannot be moved.\"}");
                                            }
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
            if (conferenceId.equals(participantInfo.getConferenceId())) {
                //本级
                requestEx.assignValueAccordingToType(meetingControlType, pid);
                if (meetingControlTypeFilter(meetingControlType) && requestEx.remoteV2() && SystemConfiguration.smcVersionIsV2()) {
                    remoteGwService.toByGwId(requestEx.getRemoteMeetingInfo().getGwId()).patch(String.format(ConfApiUrl.CONTROLLER_CONFERENCE_STATUS_DIRECT.value(), requestEx.getRemoteMeetingInfo().getId()), requestEx);
                } else {
                    meetingControlBefore(conferenceId, requestEx.toMeetingControlRequest(), getSmcToken(token));
                }

                if (excludeChildConferenceId != null) {
                    childCasChannelControl(conferenceId, pid, meetingControlType, excludeChildConferenceId, requestEx.getIsRolled());
                } else {
                    childCasChannelControl(conferenceId, pid, meetingControlType, null, requestEx.getIsRolled());
                }
            } else {
                //发给下级
                ChildMeetingInfo childMeetingInfo = meetingInfoManagerService.getChildByConferenceId(conferenceId, participantInfo.getConferenceId());
                requestEx.setFrom(CascadeParticipantDirection.UP);
                MeetingControlMeetingInfo remoteMeetingInfo = requestEx.getRemoteMeetingInfo();
                if (meetingControlTypeFilter(meetingControlType) && SystemConfiguration.smcVersionIsV2() && childMeetingInfo.smcVersionIsV2()) {
                    if (!requestEx.remoteV2()) {
                        MeetingControlMeetingInfo meetingControlMeetingInfo = new MeetingControlMeetingInfo(conferenceId, nodeDataService.getLocal().toGwId(), SystemConfiguration.getSmcVersion());
                        requestEx.setRemoteMeetingInfo(meetingControlMeetingInfo);
                    }
                }else{
                    if(requestEx.remoteV2() && !SmcVersionType.V2.equals(SystemConfiguration.getSmcVersion())){
                        MeetingControlMeetingInfo meetingControlMeetingInfo = new MeetingControlMeetingInfo(conferenceId, nodeDataService.getLocal().toGwId(), SystemConfiguration.getSmcVersion());
                        requestEx.setRemoteMeetingInfo(meetingControlMeetingInfo);
                    }
                }
                remoteGwService.toByGwId(childMeetingInfo.getGwId()).patch(String.format(ConfApiUrl.CONTROLLER_CONFERENCE_STATUS.value(), childMeetingInfo.getId()), requestEx);

                MeetingControlRequestEx cprequestEx = CommonHelper.copyBean(requestEx, MeetingControlRequestEx.class);
                //本级不做 留给下级做
                NodeBusinessType nodeBusinessType = nodeManageService.getNodeBusinessType(childMeetingInfo.getGwId().getNodeId());
                if (meetingControlTypeFilter(meetingControlType) && SystemConfiguration.smcVersionIsV2() && childMeetingInfo.smcVersionIsV2() && NodeBusinessType.SMC.equals(nodeBusinessType)) {
                    if (!requestEx.remoteV2()) {
                        MeetingControlMeetingInfo meetingControlMeetingInfo = new MeetingControlMeetingInfo(conferenceId, nodeDataService.getLocal().toGwId(), SystemConfiguration.getSmcVersion());
                        requestEx.setRemoteMeetingInfo(meetingControlMeetingInfo);
                    }
                } else {
                    //设置本级的下级主通道为会控状态
                    CascadeChannelInfo cascadeChannelInfoMain = cascadeChannelManageService.getCascadeChannelMain(conferenceId, CascadeParticipantDirection.DOWN, childMeetingInfo.getId());
                    if (cascadeChannelInfoMain == null) {
                        log.error("child node master channel not found child meeting info: {}", childMeetingInfo);
                    }
                    String cascadeChannelPid = cascadeChannelInfoMain.getParticipantId();
                    if (SystemConfiguration.smcVersionIsV2() && childMeetingInfo.smcVersionIsV2() && NodeBusinessType.SMC.equals(nodeBusinessType)) {
                        cascadeChannelPid = "";
                    }
                    MeetingControlRequest requestLocal = new MeetingControlRequest();
                    requestLocal.assignValueAccordingToType(meetingControlType, cascadeChannelPid);
                    if (meetingControlType.equals(MeetingControlType.SPOKESMAN)) {
                        requestLocal.setIsRolled(requestEx.getIsRolled());
                    }
                    if(meetingControlType.equals(MeetingControlType.BROADCASTER)){
                        cprequestEx.setBroadcaster(cascadeChannelPid);
                        meetingControlBroadcast(conferenceId, cprequestEx,remoteMeetingInfo, getSmcToken(token));
                    }else{
                        meetingControlBefore(conferenceId, requestLocal, getSmcToken(token));
                    }
                }
                childCasChannelControl(conferenceId, pid, meetingControlType, childMeetingInfo.getId(), requestEx.getIsRolled());
            }
        }
    }


    private boolean meetingControlTypeFilter(MeetingControlType meetingControlType) {
        switch (meetingControlType) {
            case BROADCASTER:
                return true;
            default:
                return false;
        }
    }


    public void childCasChannelControl(String conferenceId, String pid, MeetingControlType
            meetingControlType) {
        childCasChannelControl(conferenceId, pid, meetingControlType, null, null);
    }

    public void childCasChannelControl(String conferenceId, String pid, MeetingControlType
            meetingControlType, String excludeChildConferenceId, Boolean isRolled) {
        for (ChildMeetingInfo item : meetingInfoManagerService.get(conferenceId).getChildMeetingInfoMap().values()) {
            if (excludeChildConferenceId != null) {
                if (item.getId().equals(excludeChildConferenceId)) {
                    continue;
                }
            }
            //设置本级的下级主通道为会控状态
            CascadeChannelInfo cascadeChannelInfoMain = cascadeChannelManageService.getCascadeChannelMain(conferenceId, CascadeParticipantDirection.DOWN, item.getId());
            if(cascadeChannelInfoMain == null){
                log.error("Main participantId is null");
                continue;
            }
            ParticipantInfo cascadeChannelParticipantInfo = participantInfoManagerService.getParticipant(conferenceId, cascadeChannelInfoMain.getParticipantId());
            if (cascadeChannelParticipantInfo == null) {
                log.error("participantId is null");
                continue;
            }
            if (cascadeChannelParticipantInfo.getOnline()) {
                try {
                    MeetingControlRequestEx requestEx = new MeetingControlRequestEx();
                    String requestPid;
                    NodeBusinessType nodeBusinessType = nodeManageService.getNodeBusinessType(item.getGwId().getNodeId());
                    if (item.smcVersionIsV2() && SystemConfiguration.smcVersionIsV2() && NodeBusinessType.SMC.equals(nodeBusinessType)) {
                        MeetingControlMeetingInfo meetingControlMeetingInfo = new MeetingControlMeetingInfo(conferenceId, nodeDataService.getLocal().toGwId(), SystemConfiguration.getSmcVersion());
                        requestEx.setRemoteMeetingInfo(meetingControlMeetingInfo);
                        requestPid = pid;
                    } else {
                        CasChannelParameter casChannelParameter = new CasChannelParameter(item.getId(), cascadeChannelParticipantInfo.getCascadeParticipantParameter().getIndex(), CascadeParticipantDirection.UP);
                        requestPid = casChannelParameter.toString();
                    }
                    requestEx.assignValueAccordingToType(meetingControlType, requestPid);
                    if (isRolled != null) {
                        requestEx.setIsRolled(isRolled);
                    }
                    requestEx.setFrom(CascadeParticipantDirection.UP);
                    remoteGwService.toByGwId(item.getGwId()).patch(String.format(ConfApiUrl.CONTROLLER_CONFERENCE_STATUS.value(), item.getId()), requestEx);
                } catch (MyHttpException e) {

                }
            }
        }
    }

    public void sendChild(String conferenceId, MeetingControlRequest request) throws MyHttpException {
        sendChild(conferenceId, request, null);
    }


    public void sendChild(String conferenceId, MeetingControlRequest
            request, Set<String> excludeConferenceIdSet) throws
            MyHttpException {
        MeetingControlRequestEx requestEx = CommonHelper.copyBean(request, MeetingControlRequestEx.class);
        requestEx.setSmcVersionType(SystemConfiguration.getSmcVersion());
        requestEx.setFrom(CascadeParticipantDirection.UP);
        MeetingInfo meetingInfo = meetingInfoManagerService.get(conferenceId);
        if (meetingInfo == null) {
            return;
        }
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        headers.set("RequestFrom", "UP");
        //分发到所有下级
        for (ChildMeetingInfo item : meetingInfo.getChildMeetingInfoMap().values()) {
            if (item.getId() == null) {
                continue;
            }
            if (excludeConferenceIdSet != null && excludeConferenceIdSet.contains(item.getId())) {
                continue;
            }
            if (meetingInfoManagerService.childIsOnline(conferenceId, item.getId())) {
                try {
                    remoteGwService.toByGwId(item.getGwId()).patch(String.format(ConfApiUrl.CONTROLLER_CONFERENCE_STATUS.value(), item.getId()), requestEx, headers);
                }catch (MyHttpException e){
                    if(HttpStatus.UNAUTHORIZED.value() == e.getCode()){
                        continue;
                    }
                    throw e;
                }
            }
        }
    }


    public void sendTop(String conferenceId, MeetingControlRequest request) throws MyHttpException {
        MeetingInfo meetingInfo = meetingInfoManagerService.get(conferenceId);
        if (meetingInfo == null) {
            return;
        }
        Map<String, ParticipantInfo> localCasParticipantUp = participantInfoManagerService.getLocalCasParticipant(conferenceId, CascadeParticipantDirection.UP);
        if (!CollectionUtils.isEmpty(localCasParticipantUp)) {
            for (ParticipantInfo item : localCasParticipantUp.values()) {
                if (item.getCascadeParticipantParameter().getIndex() == 0) {
                    if (item.getOnline()) {
                        remoteGwService.toByGwId(item.getCascadeParticipantParameter().getGwId()).patch(String.format(ConfApiUrl.CONTROLLER_CONFERENCE_STATUS_TOP.value(), item.getConfCasId(), meetingInfo.getId()), request);
                    }
                    break;
                }
            }
        }
    }


    public boolean sendChild1(String conferenceId, MeetingControlRequest request) throws MyHttpException {
        boolean isSuccess = false;
        MeetingInfo meetingInfo = meetingInfoManagerService.get(conferenceId);
        if (meetingInfo == null) {
            return isSuccess;
        }
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        headers.set("RequestFrom", "UP");
        //分发到所有下级
        for (ChildMeetingInfo item : meetingInfo.getChildMeetingInfoMap().values()) {
            if (item.getId() == null) {
                continue;
            }
            if (meetingInfoManagerService.childIsOnline(conferenceId, item.getId())) {
                try {
                    remoteGwService.toByGwId(item.getGwId()).patch(String.format(ConfApiUrl.CONTROLLER_CONFERENCE_STATUS.value(), item.getId()), request, headers);
                    isSuccess = true;
                } catch (MyHttpException e) {
                    log.error("sent to child error: {}", e.toString());
                }
            }
        }
        return isSuccess;
    }


    @Override
    public void quickHangup(String uri, String token) throws MyHttpException {
        smcMeetingControlService.quickHangup(uri, getSmcToken(token));
//        if(uri == null){
//            //TODO 异常
//            return;
//        }
//        ParticipantInfo participantInfo = participantManagerService.getByUri(uri);
//        if(participantInfo == null){
//            smcMeetingControlService.quickHangup(uri,getSmcToken(token));
//        }
//        MeetingInfo meetingInfo = meetingInfoManagerService.get(participantInfo.getConferenceId());
//        if(meetingInfo == null || routManageService.isLocal(meetingInfo.getGwId())){
//            smcMeetingControlService.quickHangup(uri,getSmcToken(token));
//            return;
//        }
//        remoteGwService.toByGwId(meetingInfo.getGwId()).patch(String.format(ConfApiUrl.QUICKHANGUP.value()), uri);
    }

    @Override
    public CallInfoRsp callInfo(String uri, String token) throws MyHttpException {
        CallInfoRsp callInfoRsp = smcMeetingControlService.callInfo(uri, getSmcToken(token));
//        if(uri == null){
//            //TODO 异常
//            return null;
//        }
//        ParticipantInfo participantInfo = participantManagerService.getByUri(uri);
//        MeetingInfo meetingInfo = meetingInfoManagerService.get(participantInfo.getConferenceId());
//        String response = remoteGwService.toByGwId(meetingInfo.getGwId()).patch(String.format(ConfApiUrl.CALLINFO.value()), uri).getBody();
//        CallInfoRsp callInfoRsp = JSON.parseObject(response,CallInfoRsp.class);
        return callInfoRsp;
    }

    @Override
    public void changeSiteName(String conferenceId, ParticipantUpdateDto participantUpdateDto, String token) throws MyHttpException {
        ParticipantNameInfo participantNameInfo = participantUpdateDto.getParticipantNameInfo();
        if(participantNameInfo == null){
            log.warn("param error. participantUpdateDto:{}", participantUpdateDto);
            //TODO 抛异常
            return ;
        }
        String participantId = participantNameInfo.getId();
        if(participantId == null){
            log.warn("participantId is empty. participantUpdateDto:{}", participantUpdateDto);
            //TODO 抛异常
            return ;
        }
        ParticipantInfo participantInfo = participantInfoManagerService.getParticipant(conferenceId, participantId);
        if (participantInfo == null) {
            log.warn("participantId can not found in memory. :{}", participantId);
            //TODO 抛异常
            return ;
        }
        if (conferenceId.equals(participantInfo.getConferenceId())) {
            smcMeetingControlService.changeSiteName(conferenceId, participantUpdateDto, getSmcToken(token));
        } else {
            ChildMeetingInfo childMeetingInfo = meetingInfoManagerService.getChildByConferenceId(conferenceId, participantInfo.getConferenceId());
            if (childMeetingInfo == null) {
                log.warn("Conference Id can not found in memory:{}", participantInfo.getConferenceId());
                //TODO 抛异常
                return;
            }
            remoteGwService.toByGwId(childMeetingInfo.getGwId()).post(String.format(ConfApiUrl.CAMERA_CONTROL.value(), childMeetingInfo.getId(), participantId), participantUpdateDto);
        }
    }
}
