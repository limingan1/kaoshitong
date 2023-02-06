package com.suntek.vdm.gw.common.pojo;


import com.suntek.vdm.gw.common.enums.CascadeChannelStatus;
import com.suntek.vdm.gw.common.enums.MeetingControlType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;


@Data
@Slf4j
public class CascadeChannelInfo {
    private String participantId;
    private MeetingControlType meetingControlType;
    private CascadeChannelStatus cascadeChannelStatus;
    private CascadeParticipantParameter baseInfo;
    private long useTime;
    private String viewedPid;


    public int getIndex() {
        return baseInfo.getIndex();
    }

    public boolean isMain() {
        return baseInfo.isMain();
    }

    public CascadeChannelInfo(String participantId, CascadeParticipantParameter baseInfo) {
        this.participantId = participantId;
        this.cascadeChannelStatus = CascadeChannelStatus.FREE;
        this.baseInfo = baseInfo;
        this.meetingControlType = null;
        this.useTime = 0;
    }

    /**
     * 使用级联通道
     *
     * @param meetingControlType
     */
    public void use(MeetingControlType meetingControlType, String participandId) {
        log.info("cascade channel use pid:{} index:{} direction:{} type:{}", this.participantId, this.baseInfo.getIndex(), this.baseInfo.getDirection().name(),meetingControlType);
        this.cascadeChannelStatus = CascadeChannelStatus.USE;
        this.meetingControlType = meetingControlType;
        this.useTime = System.currentTimeMillis();
        this.viewedPid = participandId;
    }


    /**
     * 释放级联通道
     *
     * @param
     */
    public void free() {
        log.info("cascade channel free pid:{} index:{} direction:{}", this.participantId, this.baseInfo.getIndex(), this.baseInfo.getDirection().name());
        this.meetingControlType = null;
        this.cascadeChannelStatus = CascadeChannelStatus.FREE;
    }


    /**
     * 检测级联通道是否可用
     *
     * @param meetingControlType
     * @return
     */
    public boolean checkFree(MeetingControlType meetingControlType) {
        switch (this.cascadeChannelStatus) {
            case USE: {
                return false;
            }
            case FREE: {
                return true;
            }
        }
        return false;
    }

    /**
     * 检测级联通道是否空闲
     *
     * @return
     */
    public boolean ischeckFree() {
        switch (this.cascadeChannelStatus) {
            case USE: {
                return false;
            }
            case FREE: {
                return true;
            }
        }
        return false;
    }
}
