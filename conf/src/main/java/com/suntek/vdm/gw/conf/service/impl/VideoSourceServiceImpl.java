package com.suntek.vdm.gw.conf.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.*;
import com.suntek.vdm.gw.common.enums.CascadeChannelNotifyType;
import com.suntek.vdm.gw.common.pojo.node.GwNode;
import com.suntek.vdm.gw.conf.enumeration.SubscribeUserType;
import com.suntek.vdm.gw.conf.pojo.*;
import com.suntek.vdm.gw.conf.service.*;
import com.suntek.vdm.gw.common.util.CommonHelper;
import com.suntek.vdm.gw.common.pojo.websocket.SubscribeAttachInfo;
import com.suntek.vdm.gw.common.pojo.websocket.SubscribeBusinessType;
import com.suntek.vdm.gw.common.pojo.websocket.SubscribeMessage;
import com.suntek.vdm.gw.core.entity.NodeData;
import com.suntek.vdm.gw.core.enumeration.NodeBusinessType;
import com.suntek.vdm.gw.core.service.NodeDataService;
import com.suntek.vdm.gw.core.service.NodeManageService;
import com.suntek.vdm.gw.core.service.RoutManageService;
import com.suntek.vdm.gw.smc.request.meeting.control.ParticipantNameInfo;
import com.suntek.vdm.gw.smc.request.meeting.control.ParticipantUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class VideoSourceServiceImpl implements VideoSourceService {
    @Value("${smc_displace_name}")
    private boolean smc_displace_name;

    @Autowired
    private MeetingInfoManagerService meetingInfoManagerService;
    @Autowired
    private ParticipantInfoManagerService participantInfoManagerService;
    @Autowired
    private ProxySubscribeService proxySubscribeService;
    @Autowired
    private CascadeChannelPushService cascadeChannelPushService;
    @Autowired
    private RoutManageService routManageService;
    @Autowired
    private NodeManageService nodeManageService;
    @Autowired
    private MeetingControlService meetingControlService;
    @Autowired
    private NodeDataService nodeDataService;

    public void videoSourceHandleByRemote(String conferenceId, MultiPicInfo changeMultiPicInfo, String changeParticipantId, boolean isCasChannel) {
        MeetingInfo meetingInfo = meetingInfoManagerService.get(conferenceId);
        MultiPicInfo cascadeMultiPicInfo = meetingInfo.getCascadeMultiPicInfoMap().get(changeParticipantId);
        //??????????????? ?????????
        if (changeMultiPicInfo.equals(cascadeMultiPicInfo)) {
            return;
        }
        //???????????????
        meetingInfo.getCascadeMultiPicInfoMap().put(changeParticipantId, changeMultiPicInfo);
        //smc??????????????????
        changSmcCasChannelName(conferenceId, changeParticipantId, changeMultiPicInfo);

        //???????????????????????????
        Map<String, ParticipantInfo> localParticipant = meetingInfo.getLocalParticipant();
        List<ParticipantStatusInfo> changeList = new ArrayList<>();
        for (ParticipantInfo watchParticipantInfo : localParticipant.values()) {
            //??????????????????????????????????????????
            if (changeParticipantId.equals(watchParticipantInfo.getParticipantId())) {
                continue;
            }
            //????????? ?????????
            if (watchParticipantInfo.getOnline()==null||!watchParticipantInfo.getOnline()) {
                continue;
            }
            MultiPicInfo watchMultiPicInfo = watchParticipantInfo.getMultiPicInfo();
            watchMultiPicInfo= CommonHelper.copyBean(watchMultiPicInfo,MultiPicInfo.class);
            if (watchMultiPicInfo == null) {
                continue;
            }
            //??????????????????
            if (watchMultiPicInfo.getPicNum() > 1 && changeMultiPicInfo.getPicNum() > 1) {
                continue;
            }
            boolean isChange = false;
            boolean isUseCasMultiPicMode = false;
            for (SubPic item : watchMultiPicInfo.getSubPicList()) {
                String participantId = item.getParticipantId();
                if (participantId == null || !participantId.equals(changeParticipantId)) {
                    continue;
                }
                if (watchMultiPicInfo.getPicNum() == 1) {
                    watchMultiPicInfo.setSubPicList(changeMultiPicInfo.getSubPicList());
                    isUseCasMultiPicMode = true;
                    isChange = true;
                    break;
                }

                if (changeMultiPicInfo.getPicNum() == 1) {
                    SubPic beWatchedSubPic = changeMultiPicInfo.getSubPicList().get(0);
                    item.setName(beWatchedSubPic.getName());
                    item.setUri(beWatchedSubPic.getUri());
                    item.setParticipantId(beWatchedSubPic.getParticipantId());
                    item.setStreamNumber(beWatchedSubPic.getStreamNumber());
                    isChange = true;
                    continue;
                }
            }
            if (isChange || (watchParticipantInfo.isCascadeParticipantH323() && changeParticipantId.equals(watchParticipantInfo.getMultiPicInfo().getFirstParticipantId()))) {
                ParticipantStatusInfo participantStatusInfo = new ParticipantStatusInfo();
                participantStatusInfo.setParticipantId(watchParticipantInfo.getParticipantId());
                if (isUseCasMultiPicMode) {
                    watchMultiPicInfo.setMode(changeMultiPicInfo.getMode());
                }
                participantStatusInfo.setMultiPicInfo(watchMultiPicInfo);
                participantStatusInfo.setOnline(watchParticipantInfo.getOnline());
                changeList.add(participantStatusInfo);
                //?????????????????????
                if (watchParticipantInfo.isCascadeParticipant()) {
                    String changeCasParticipantId = watchParticipantInfo.getParticipantId();
                    String changeCasParticipantIdProxy = watchParticipantInfo.getParticipantId();
                    if (watchParticipantInfo.isCascadeParticipantH323()) {
                        changeCasParticipantIdProxy = changeParticipantId;
                    }
                    cascadeChannelPushService.pushToRemote(conferenceId, changeCasParticipantId, changeCasParticipantIdProxy, CascadeChannelNotifyType.VIDEO_SOURCE);
                }
            }
        }
        if (!CollectionUtils.isEmpty(changeList)) {
            String destination = "/topic/conferences/%s/participants/general";
            ParticipantStatusNotify participantStatusNotify = new ParticipantStatusNotify();
            participantStatusNotify.setConferenceId(meetingInfo.getId());
            participantStatusNotify.setSize(changeList.size());
            participantStatusNotify.setType(3);
            participantStatusNotify.setConfCasId(meetingInfo.getConfCasId());
            participantStatusNotify.setChangeList(changeList);
            String message = JSON.toJSONString(participantStatusNotify, SerializerFeature.DisableCircularReferenceDetect);
            ConferencesSubscribeAttachInfo conferencesSubscribeAttachInfo = new ConferencesSubscribeAttachInfo(meetingInfo.getId(), meetingInfo.getAccessCode());
            SubscribeAttachInfo subscribeAttachInfo = new SubscribeAttachInfo(SubscribeBusinessType.CONFERENCES, JSON.toJSONString(conferencesSubscribeAttachInfo));
            SubscribeMessage subscribeMessage = new SubscribeMessage(
                    null,
                    String.format(destination, meetingInfo.getId()),
                    null,
                    null,
                    message,
                    subscribeAttachInfo);
            List<SubscribeUserType> userTypeBlackList = new ArrayList<>();
            userTypeBlackList.add(SubscribeUserType.INTERNAL);
            proxySubscribeService.distributionUser(subscribeMessage, userTypeBlackList);
        }

    }

    private void changSmcCasChannelName(String conferenceId, String changeParticipantId, MultiPicInfo changeMultiPicInfo){
        if(!smc_displace_name){
            return;
        }

        String name = "";
        if(StringUtils.isEmpty(conferenceId) || StringUtils.isEmpty(changeParticipantId)|| changeMultiPicInfo == null
                || changeMultiPicInfo.getSubPicList() == null || changeMultiPicInfo.getSubPicList().isEmpty()){
            return;
        }
        ParticipantInfo participantInfo = participantInfoManagerService.getParticipant(conferenceId, changeParticipantId);
        if(participantInfo == null || participantInfo.isCascadeParticipantH323()){
            return;
        }

        if(changeMultiPicInfo.getSubPicList().size() == 1){
            name = changeMultiPicInfo.getSubPicList().get(0).getName();
        }else if(changeMultiPicInfo.getSubPicList().size() > 1){
            name = "?????????";
        }
        if("".equals(name)){
            return;
        }
        ParticipantUpdateDto participantUpdateDto = new ParticipantUpdateDto();
        ParticipantNameInfo participantNameInfo = new ParticipantNameInfo();
        participantNameInfo.setId(changeParticipantId);
        participantNameInfo.setName(name);
        participantUpdateDto.setParticipantNameInfo(participantNameInfo);
        try {
            meetingControlService.changeSiteName(conferenceId, participantUpdateDto, CoreConfig.INTERNAL_USER_TOKEN);
        } catch (MyHttpException exception) {
            exception.printStackTrace();
        }

    }


    public void videoSourceHandleByParticipantSelect(String conferenceId, List<ParticipantDetail> participantDetails) {
        if (participantDetails != null) {
            for (ParticipantDetail participantDetail : participantDetails) {
                ParticipantState participantState = participantDetail.getState();
                if (participantDetail.getGeneralParam().getVdcMarkCascadeParticipant() != null) {
                    ParticipantInfo participantInfo = participantInfoManagerService.getParticipant(conferenceId, participantState.getParticipantId());
                    String changeCasParticipantId = participantInfo.getParticipantId();
                    String changeCasParticipantIdProxy = participantInfo.getParticipantId();
                    cascadeChannelPushService.pushToRemote(conferenceId, changeCasParticipantId, changeCasParticipantIdProxy, CascadeChannelNotifyType.VIDEO_SOURCE);
                }
                MultiPicInfo multiPicInfo = participantDetail.getState().getMultiPicInfo();
                //???????????????
                changeParticipantVideoSource(conferenceId, multiPicInfo);

                //tp??????????????????
                if(participantDetail.getSubTpState() != null){
                    for(ParticipantState participantState1: participantDetail.getSubTpState()){
                        MultiPicInfo tpMultiPicInfo = participantState1.getMultiPicInfo();
                        //???????????????
                        changeParticipantVideoSource(conferenceId, tpMultiPicInfo);
                    }
                }
            }
        }
    }

    public void videoSourceHandleBySubscribeNotify(ParticipantInfo participantInfo, MultiPicInfo newMultiPicInfo) {
        if (participantInfo.isCascadeParticipant()) {
            if (videoSourceChangeDetection(participantInfo.getParticipantId(), participantInfo.getMultiPicInfo(), newMultiPicInfo)) {
                //ParticipantInfo watchParticipantInfo = JSON.parseObject(JSON.toJSONString(participantInfo), ParticipantInfo.class);
                //watchParticipantInfo.setMultiPicInfo(newMultiPicInfo);
                String changeCasParticipantId = participantInfo.getParticipantId();
                String changeCasParticipantIdProxy = participantInfo.getParticipantId();
                if(participantInfo.isCascadeParticipantH323() && newMultiPicInfo.getPicNum()==1){
                    String viewedPid = newMultiPicInfo.getFirstParticipantId();
                    ParticipantInfo viewedParticipantInfo = participantInfoManagerService.getParticipant(participantInfo.getConferenceId(), viewedPid);
                    if(viewedParticipantInfo.isCascadeParticipant()){
                        changeCasParticipantIdProxy = viewedPid;
                    }
                }
                participantInfo.setMultiPicInfo(newMultiPicInfo);
                cascadeChannelPushService.pushToRemote(participantInfo.getConferenceId(), changeCasParticipantId, changeCasParticipantIdProxy, CascadeChannelNotifyType.VIDEO_SOURCE);
            }
        }
    }


    public String changeSource(String message) {
        JSONObject outputObj = JSON.parseObject(message);
        //??????????????? welink???????????????????????????isWelink
        changeSource(outputObj);
        return JSON.toJSONString(outputObj, SerializerFeature.DisableCircularReferenceDetect);
    }

    @Override
    public void casChannelFreeHandelBySubscribeNotify(String conferenceId, ParticipantInfo participantInfo, MultiPicInfo newmultiPicInfo) {
        MeetingInfo meetingInfo = meetingInfoManagerService.get(conferenceId);
        MultiPicInfo oldMultiPicInfo = participantInfo.getMultiPicInfo();
        //??????????????? ?????????
        if (oldMultiPicInfo == null || oldMultiPicInfo.equals(newmultiPicInfo) || oldMultiPicInfo.getSubPicList() == null) {
            return;
        }
        List<SubPic> subPicList = oldMultiPicInfo.getSubPicList();
        List<SubPic> newSubPicList = newmultiPicInfo.getSubPicList();
        for(SubPic subPic: subPicList){
            String Pid = subPic.getParticipantId();
            if(StringUtils.isEmpty(Pid)){
                continue;
            }
            ParticipantInfo subParticipantInfo = participantInfoManagerService.getParticipant(conferenceId, Pid);
            if (subParticipantInfo == null || !subParticipantInfo.isCascadeParticipant()) {
                continue;
            }
            if(newSubPicList == null){
                if(participantInfo.isCascadeParticipant()){
                    CascadeChannelInfo cascadeChannelInfo = meetingInfo.getCascadeChannelInfoMap().get(participantInfo.getParticipantId());
                    //?????????????????????????????????????????????Free???????????????????????????
                    if(cascadeChannelInfo != null && cascadeChannelInfo.ischeckFree()){
                        continue;
                    }
                }
                checkOtherParticipantsHasViewThisChannel(meetingInfo, Pid, participantInfo.getParticipantId());
            }
            boolean hasLookingFor = false;
            for (SubPic newSubPic: newSubPicList){
                if(Pid.equals(newSubPic.getParticipantId())){
                    hasLookingFor = true;
                    break;
                }
            }
            //????????????????????????
            if(!hasLookingFor){
                checkOtherParticipantsHasViewThisChannel(meetingInfo, Pid, participantInfo.getParticipantId());
            }
        }
    }

    public void checkOtherParticipantsHasViewThisChannel(MeetingInfo meetingInfo, String cascadeChannelPid, String oldParticipantId){
        //?????????????????????????????????
        boolean hasViewChannel = false;
        Map<String, ParticipantInfo> localParticipant = meetingInfo.getLocalParticipant();
        ParticipantInfo casParticipantInfo = localParticipant.get(cascadeChannelPid);
        for (ParticipantInfo watchParticipantInfo : localParticipant.values()) {
            //???????????????????????????,????????????????????????
            if(oldParticipantId.equals(watchParticipantInfo.getParticipantId()) || cascadeChannelPid.equals(watchParticipantInfo.getParticipantId())){
                continue;
            }
            if (watchParticipantInfo.getOnline()==null||!watchParticipantInfo.getOnline()) {
                continue;
            }

            MultiPicInfo watchMultiPicInfo = watchParticipantInfo.getMultiPicInfo();
            if(watchMultiPicInfo == null || watchMultiPicInfo.getSubPicList() == null){
                continue;
            }
            for (SubPic item : watchMultiPicInfo.getSubPicList()) {
                //???????????????????????????
                if(!cascadeChannelPid.equals(item.getParticipantId())) {
                    continue;
                }
                //??????????????????????????????????????????
                if(!watchParticipantInfo.isCascadeParticipant()){
                    hasViewChannel = true;
                    break;
                }
                //????????????????????????
                if(watchParticipantInfo.getCascadeParticipantParameter().getGwId().equals(casParticipantInfo.getCascadeParticipantParameter().getGwId())){
                    break;
                }

                //??????????????????????????????
                CascadeChannelInfo cascadeChannelInfo = meetingInfo.getCascadeChannelInfoMap().get(watchParticipantInfo.getParticipantId());
                //????????????????????????????????????????????????Free???????????????????????????
                if(cascadeChannelInfo != null && !cascadeChannelInfo.ischeckFree()){
                    hasViewChannel = true;
                    break;
                }
            }
            if(hasViewChannel){
                break;
            }
        }
        //?????????????????????
        if(!hasViewChannel){
            //????????????????????????,????????????
            ParticipantInfo subParticipantInfo = participantInfoManagerService.getParticipant(meetingInfo.getId(), cascadeChannelPid);
            cascadeChannelPushService.freeToRemote(meetingInfo.getId(), subParticipantInfo);
        }
    }

    public void changeSource(JSONObject outputObj) {
        JSONArray changeList = outputObj.getJSONArray("changeList");
        if (changeList == null) {
            return;
        }
        String confId = outputObj.getString("conferenceId");
        if (confId == null) {
            return;
        }
        int type = outputObj.getIntValue("type");
        for (int i = 0; i < changeList.size(); i++) {
            JSONObject changeMsg = changeList.getJSONObject(i);
            MultiPicInfo multiPicInfo = changeMsg.getObject("multiPicInfo", MultiPicInfo.class);
            changeParticipantVideoSource(confId, multiPicInfo);
            String pid = changeMsg.getString("participantId");
            if(pid != null){
                ParticipantInfo participantInfo = participantInfoManagerService.getParticipant(confId, pid);
                if(participantInfo != null){
                    changeMsg.put("siteUri", participantInfo.getUri());
                }
            }
            changeMsg.put("multiPicInfo", multiPicInfo);
            try{
                if(type == 1){
                    String vdcMarkCascadeParticipant = changeMsg.getString("vdcMarkCascadeParticipant");
                    if(vdcMarkCascadeParticipant == null){
                        continue;
                    }
                    CascadeParticipantParameter cascadeParticipantParameter = CascadeParticipantParameter.valueOf(vdcMarkCascadeParticipant);
                    String nodeId = cascadeParticipantParameter.getGwId().getNodeId();
                    NodeBusinessType nodeBusinessType = null;
                    if("00000000000000001111111111111111".equals(nodeId)){
                        GwId gwId = routManageService.getWayByGwId(cascadeParticipantParameter.getGwId());
                        nodeBusinessType = nodeManageService.getNodeBusinessType(gwId.getNodeId());
                    }else{
                        nodeBusinessType = nodeManageService.getNodeBusinessType(cascadeParticipantParameter.getGwId().getNodeId());
                    }
                    if (NodeBusinessType.isWelinkOrCloudLink(nodeBusinessType)) {
                        changeMsg.put("isWelink",true);
                    }
                    String casChannelName = changeMsg.getString("casChannelName");
                    if (casChannelName == null){
//                        GwId realId = routManageService.getWayByGwId(cascadeParticipantParameter.getGwId());
                        GwId gwId = routManageService.getCompleteGwIdBy(cascadeParticipantParameter.getGwId());
                        if(gwId == null){
                            continue;
                        }
                        GwNode moGwNode = nodeManageService.getByNodeId(gwId.getNodeId());
                        casChannelName = moGwNode.getName();
                        casChannelName = casChannelName + "(" + (cascadeParticipantParameter.getIndex() + 1) + ")";
                        changeMsg.put("casChannelName", casChannelName);
                    }
                }
            }catch (Exception e){
                log.error("error msg: {}",e.getMessage());
                log.error("error stack: {}", (Object[]) e.getStackTrace());
            }
        }
    }

    /**
     * ??????????????????????????????
     *
     * @param conferenceId
     * @param multiPicInfo
     */
    private void changeParticipantVideoSource(String conferenceId, MultiPicInfo multiPicInfo) {
        if (multiPicInfo == null || CollectionUtils.isEmpty(multiPicInfo.getSubPicList())) {
            return;
        }
        MeetingInfo meetingInfo = meetingInfoManagerService.get(conferenceId);
        if(meetingInfo == null){
            return;
        }
        for (SubPic item : multiPicInfo.getSubPicList()) {
            //?????????????????????
            if(item == null){
                continue;
            }
            if(item.getStreamNumber() != null && item.getStreamNumber() == 1){
                continue;
            }
            if (StringUtils.isEmpty(item.getParticipantId())) {
                continue;
            }
            MultiPicInfo cascadeMultiPicInfo = meetingInfo.getCascadeMultiPicInfoMap().get(item.getParticipantId());
            if (cascadeMultiPicInfo == null || CollectionUtils.isEmpty(cascadeMultiPicInfo.getSubPicList())) {
                continue;
            }
            if (multiPicInfo.getPicNum() == 1) {
                multiPicInfo.setPicNum(cascadeMultiPicInfo.getPicNum());
                multiPicInfo.setMode(cascadeMultiPicInfo.getMode());
                multiPicInfo.setSubPicList(cascadeMultiPicInfo.getSubPicList());
                break;
            } else {
                if (cascadeMultiPicInfo.getPicNum() > 1) {
                    continue;
                } else {
                    SubPic realSubPic = cascadeMultiPicInfo.getSubPicList().get(0);
                    item.setName(realSubPic.getName());
                    item.setUri(realSubPic.getUri());
                    item.setParticipantId(realSubPic.getParticipantId());
                }
            }
        }
    }

    /**
     * ???????????????????????????
     *
     * @param participantId
     * @param oldMultiPicInfo
     * @param newMultiPicInfo
     * @return
     */
    private boolean videoSourceChangeDetection(String participantId, MultiPicInfo oldMultiPicInfo, MultiPicInfo
            newMultiPicInfo) {
        if (newMultiPicInfo == null) {
            return false;
        }
        if (oldMultiPicInfo == null) {
            return true;
        }
        if (oldMultiPicInfo != null && oldMultiPicInfo.equals(newMultiPicInfo)) {
            return false;
        }
        //???????????????????????????
        if (participantId.equals(newMultiPicInfo.getFirstParticipantId())) {
            if (oldMultiPicInfo==null||oldMultiPicInfo.getSubPicList() == null) {
                return false;
            }
            if (newMultiPicInfo.getPicNum() == 1) {
                return false;
            }
        }
        return true;
    }
}
