package com.suntek.vdm.gw.conf.service.impl;

import com.suntek.vdm.gw.common.enums.CascadeChannelStatus;
import com.suntek.vdm.gw.common.pojo.AllocateCasChannelInfo;
import com.suntek.vdm.gw.common.pojo.CascadeChannelInfo;
import com.suntek.vdm.gw.common.util.SystemConfiguration;
import com.suntek.vdm.gw.conf.pojo.MeetingInfo;
import com.suntek.vdm.gw.conf.pojo.ParticipantInfo;
import com.suntek.vdm.gw.conf.service.CascadeChannelManageService;
import com.suntek.vdm.gw.conf.service.MeetingInfoManagerService;
import com.suntek.vdm.gw.conf.service.ParticipantInfoManagerService;
import com.suntek.vdm.gw.common.enums.CascadeParticipantDirection;
import com.suntek.vdm.gw.common.enums.MeetingControlType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CascadeChannelManageServiceImpl implements CascadeChannelManageService {
    @Autowired
    private MeetingInfoManagerService meetingInfoManagerService;
    @Autowired
    private ParticipantInfoManagerService participantInfoManagerService;

    public Map<String, CascadeChannelInfo> getAllCascadeChannelInfo(String conferenceId) {
        Map<String, CascadeChannelInfo> cascadeChannelInfoMap = meetingInfoManagerService.get(conferenceId).getCascadeChannelInfoMap();
        return cascadeChannelInfoMap;
    }

    public Map<String, CascadeChannelInfo> getCascadeChannelInfo(String id) {
        return getCascadeChannelInfo(id, null, null, null);
    }

    public Map<String, CascadeChannelInfo> getCascadeChannelInfo(String id, CascadeParticipantDirection direction) {
        return getCascadeChannelInfo(id, direction, null, null);
    }

    public Map<String, CascadeChannelInfo> getCascadeChannelInfo(String id, CascadeParticipantDirection direction, String childConfId) {
        return getCascadeChannelInfo(id, direction, childConfId, null);
    }

    public CascadeChannelInfo getCascadeChannelOne(String id, CascadeParticipantDirection direction, String childConfId, Integer index) {
        Map<String, CascadeChannelInfo> localParticipant = getCascadeChannelInfo(id, direction, childConfId, index);
        if (localParticipant.size() > 0) {
            for (CascadeChannelInfo item : localParticipant.values()) {
                return item;
            }
        }
        return null;
    }

    public CascadeChannelInfo getCascadeChannelMain(String id, CascadeParticipantDirection direction, String childConfId) {
        return getCascadeChannelOne(id, direction, childConfId, 0);
    }

    public Map<String, CascadeChannelInfo> getCascadeChannelInfo(String conferenceId, CascadeParticipantDirection direction, String childConferenceId, Integer index) {
        log.info("[级联通道] 获取 id:{} direction:{} childId:{} index:{}", conferenceId, direction.name(), childConferenceId, index);
        Map<String, ParticipantInfo> localCasParticipantFilter = participantInfoManagerService.getLocalCasParticipant(conferenceId, direction, childConferenceId, index);
        Map<String, CascadeChannelInfo> cascadeChannelInfoMapFilter = new ConcurrentHashMap<>();
        if (localCasParticipantFilter != null) {
            Map<String, CascadeChannelInfo> cascadeChannelInfoMap = getAllCascadeChannelInfo(conferenceId);
            for (String key : localCasParticipantFilter.keySet()) {
                cascadeChannelInfoMapFilter.put(key, cascadeChannelInfoMap.get(key));
            }
        }
        return cascadeChannelInfoMapFilter;
    }

    //临时代码
    public String getFormatDate(long times) {
        Date date = new Date(times);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(date);
        return dateString;
    }


    @Override
    public AllocateCasChannelInfo allocateCasChannel(String conferenceId, CascadeParticipantDirection direction, String childConferenceId, MeetingControlType meetingControlType, String watchParticipantId) {
        AllocateCasChannelInfo allocateCasChannelInfo = new AllocateCasChannelInfo();
        log.info("[allocateCasChannel] id:{} direction:{} childId:{} meetingControlType:{}", conferenceId, direction.name(), childConferenceId, meetingControlType.name());
        Map<String, CascadeChannelInfo> cascadeChannelInfoMapFilter = getCascadeChannelInfo(conferenceId, direction, childConferenceId);
        if (cascadeChannelInfoMapFilter == null) {
            return null;
        }
        if (cascadeChannelInfoMapFilter.size() == 1) {
            //兼容单通道
            allocateCasChannelInfo.setCascadeChannelInfo(cascadeChannelInfoMapFilter.values().stream().filter(x -> x.getBaseInfo().getIndex() == 0).findFirst().get());
            return allocateCasChannelInfo;
        }
        //级联通道打印 方便查询日志
        log.info("[allocateCasChannel] all channel status:");
        for (CascadeChannelInfo cascadeChannelInfo : cascadeChannelInfoMapFilter.values()) {
            log.info("[allocateCasChannel] type:{} status:{} index:{} useTime:{} pid:{} viewedPid:{}",
                    cascadeChannelInfo.getMeetingControlType(), cascadeChannelInfo.getCascadeChannelStatus(), cascadeChannelInfo.getIndex(), getFormatDate(cascadeChannelInfo.getUseTime()), cascadeChannelInfo.getParticipantId(),cascadeChannelInfo.getViewedPid());
        }

        MeetingInfo meetingInfo = meetingInfoManagerService.get(conferenceId);
        //region 优先复用
        //index由高到低排序(主通道排在最后面)   优先使用从通道
        List<CascadeChannelInfo> cascadeChannelInfoListSortedByIndex = cascadeChannelInfoMapFilter.values().stream().sorted(Comparator.comparing(CascadeChannelInfo::getIndex).reversed()).collect(Collectors.toList());
        for (CascadeChannelInfo item : cascadeChannelInfoListSortedByIndex) {
            ParticipantInfo participantInfo = meetingInfo.getAllParticipantMap().get(item.getParticipantId());
            if (participantInfo.getMultiPicInfo() != null && watchParticipantId.equals(participantInfo.getMultiPicInfo().getFirstParticipantId())) {
                //主通道处于广播状态不复用
                if (item.isMain() && MeetingControlType.BROADCASTER.equals(item.getMeetingControlType())) {
                    continue;
                }
                //如果是用来广播不用主通道
                if (item.isMain() && MeetingControlType.BROADCASTER.equals(meetingControlType)) {
                    continue;
                }
                CascadeChannelInfo cascadeChannelInfo = cascadeChannelInfoMapFilter.get(item.getParticipantId());
                if(!watchParticipantId.equals(cascadeChannelInfo.getViewedPid())){
                    continue;
                }
                cascadeChannelInfo.use(meetingControlType, watchParticipantId);
                allocateCasChannelInfo.setCascadeChannelInfo(cascadeChannelInfo);
                log.info("[allocateCasChannel][reusing] pid:{} index:{}", cascadeChannelInfo.getParticipantId(), cascadeChannelInfo.getBaseInfo().getIndex());
                allocateCasChannelInfo.setReuse(true);
                return allocateCasChannelInfo;
            }
        }
        //endregion

        //region 找空闲的
        for (CascadeChannelInfo item : cascadeChannelInfoListSortedByIndex) {
            if (item.getCascadeChannelStatus().equals(CascadeChannelStatus.FREE)) {
                if (item.getBaseInfo().isMain()) {
                    if (meetingControlType.equals(MeetingControlType.BROADCASTER)) {
                        log.info("[allocateCasChannel][use] main channel，spokesman or broadcast not allow used");
                        continue;
                    } else {
                        CascadeChannelInfo cascadeChannelInfo = cascadeChannelInfoMapFilter.get(item.getParticipantId());
                        cascadeChannelInfo.use(meetingControlType, watchParticipantId);
                        log.info("[allocateCasChannel][use]  pid:{} index:{}", cascadeChannelInfo.getParticipantId(), cascadeChannelInfo.getBaseInfo().getIndex());
                        allocateCasChannelInfo.setCascadeChannelInfo(cascadeChannelInfo);
                        return allocateCasChannelInfo;
                    }
                } else {
                    CascadeChannelInfo cascadeChannelInfo = cascadeChannelInfoMapFilter.get(item.getParticipantId());
                    cascadeChannelInfo.use(meetingControlType, watchParticipantId);
                    log.info("[allocateCasChannel][use] pid:{} index:{}", cascadeChannelInfo.getParticipantId(), cascadeChannelInfo.getBaseInfo().getIndex());
                    allocateCasChannelInfo.setCascadeChannelInfo(cascadeChannelInfo);
                    return allocateCasChannelInfo;
                }
            }
        }
        //endregion
        //region 根据使用时间抢占
        //先根据主从通道排序排序(主通道最后) 然后根据使用时间排序由早到晚排序
//        List<CascadeChannelInfo> cascadeChannelInfoListSortedByUseTime = cascadeChannelInfoMapFilter.values().stream().sorted(Comparator.comparing(CascadeChannelInfo::isMain).thenComparing(CascadeChannelInfo::getUseTime)).collect(Collectors.toList());
        List<CascadeChannelInfo> cascadeChannelInfoListSortedByUseTime = cascadeChannelInfoMapFilter.values().stream().sorted(Comparator.comparing(CascadeChannelInfo::getUseTime)).collect(Collectors.toList());
        //获取到主通道
        CascadeChannelInfo cascadeChannelInfoMain = cascadeChannelInfoListSortedByUseTime.stream().filter(x -> x.isMain()).findFirst().get();
        for (CascadeChannelInfo item : cascadeChannelInfoListSortedByUseTime) {
            if (item.getBaseInfo().isMain()) {
                if((!StringUtils.isEmpty(meetingInfo.getConferenceState().getBroadcastId())
                        && !item.getParticipantId().equals(meetingInfo.getConferenceState().getBroadcastId()))
                        || (!StringUtils.isEmpty(meetingInfo.getConferenceState().getSpokesmanId())
                        && !item.getParticipantId().equals(meetingInfo.getConferenceState().getSpokesmanId()))

                        || ((!StringUtils.isEmpty(meetingInfo.getConferenceState().getBroadcastId()) || !StringUtils.isEmpty(meetingInfo.getConferenceState().getSpokesmanId()))
                        && SystemConfiguration.smcVersionIsV2()
                        && (StringUtils.isEmpty(meetingInfo.getConferenceState().getChairmanId()) || !item.getParticipantId().equals(meetingInfo.getConferenceState().getChairmanId())))
                ){
                    log.info("conference status: {}",meetingInfo.getConferenceState());
                    log.info("[allocateCasChannel][seize] main channel，spokesman or broadcast not allow used");
                    continue;
                }

//                if (MeetingControlType.BROADCASTER.equals(item.getMeetingControlType())) {
//                    log.info("[级联通道][分配][抢占] 主通道，但是分配的是广播点名，不允许分配主通道");
//                    continue;
//                } else {
//                    if (MeetingControlType.BROADCASTER.equals(item.getMeetingControlType())) {
//                        log.info("[级联通道][分配][抢占] 主通道是广播状态不分配");
//                        continue;
//                    }
                    CascadeChannelInfo cascadeChannelInfo = cascadeChannelInfoMapFilter.get(item.getParticipantId());
                    cascadeChannelInfo.use(meetingControlType, watchParticipantId);
                    log.info("[allocateCasChannel][seize] pid:{} index:{}", cascadeChannelInfo.getParticipantId(), cascadeChannelInfo.getBaseInfo().getIndex());
                    allocateCasChannelInfo.setCascadeChannelInfo(cascadeChannelInfo);
                    return allocateCasChannelInfo;
//                }
            } else {
                //广播不抢占主通道
                if (!meetingControlType.equals(MeetingControlType.BROADCASTER)) {
                    //主通道不是广播的话 要判断使用时间
                    if (!MeetingControlType.BROADCASTER.equals(cascadeChannelInfoMain.getMeetingControlType()) && cascadeChannelInfoMain.getUseTime() < item.getUseTime()) {
                        continue;
                    }
                }
                CascadeChannelInfo cascadeChannelInfo = cascadeChannelInfoMapFilter.get(item.getParticipantId());
                cascadeChannelInfo.use(meetingControlType, watchParticipantId);
                log.info("[allocateCasChannel][seize] pid:{} index:{}", cascadeChannelInfo.getParticipantId(), cascadeChannelInfo.getBaseInfo().getIndex());
                allocateCasChannelInfo.setCascadeChannelInfo(cascadeChannelInfo);
                return allocateCasChannelInfo;
            }
        }
        //endregion

        return allocateCasChannelInfo;
    }


    public CascadeChannelInfo allocateCasChannel(String conferenceId, CascadeParticipantDirection direction, String childConferenceId, MeetingControlType meetingControlType, Integer index) {
        Map<String, CascadeChannelInfo> cascadeChannelInfoMapFilter = getCascadeChannelInfo(conferenceId, direction, childConferenceId);
        if (cascadeChannelInfoMapFilter == null) {
            return null;
        }
        return cascadeChannelInfoMapFilter.values().stream().filter(x -> x.getBaseInfo().getIndex() == index).findFirst().get();
    }
}
