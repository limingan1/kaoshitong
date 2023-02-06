package com.suntek.vdm.gw.common.api.request;

import com.suntek.vdm.gw.common.enums.MeetingControlType;
import com.suntek.vdm.gw.common.pojo.MultiPicInfo;
import lombok.Data;

@Data
public class MeetingControlRequest {
    /**
     * 设置主席(会场Id)
     */
    private String chairman;

    /**
     * 广播会场(会场Id，广播会议多画面传值00000000-0000-0000-0000-000000000000")
     */
    private String broadcaster;


    /**
     * 点名(会场Id)
     */
    private String spokesman;

    /**
     * 是否是被主会场点名
     */
    private Boolean isRolled;

    /**
     * 锁定演示(会场Id，锁定会议演示传值00000000-0000-0000-0000-000000000000")
     */
    private String lockPresenter;

    /**
     * 发送演示(会场Id)
     */
    private String presenter;
    /**
     * 发送演示(会场Id)仅在取消会场演示使用。
     */
    private String id;

    /**
     * 一键呼叫未入会会场(true)
     */
    private Boolean isOnline;

    /**
     * 锁定会议(锁定：true/取消：false)
     */
    private Boolean isLock;

    /**
     * 一键静音(静音：true/取消：false)
     */
    private Boolean isMute;

    /**
     * 一键关闭扬声器(关闭扬声器：true/打开扬声器： false)
     */
    private Boolean isQuiet;

    /**
     * 声控切换(打开：true/关闭：false)
     */
    private Boolean isVoiceActive;

    /**
     * 设置会议多画面
     */
    private MultiPicInfo multiPicInfo;

    /**
     * 录播会控
     */
    private String recordOpType;

    /**
     * 录播视频源
     */
    private MultiPicInfo recordSource;

    /**
     * 设置会议模式
     */
    private String mode;

    /**
     * AI字幕控制
     */
    private Integer subtitlesOpType;



    public void assignValueAccordingToType(MeetingControlType meetingControlType, String pid) {
        switch (meetingControlType) {
            case CHAIRMAN: {
                this.setChairman(pid);
                break;
            }
            case BROADCASTER: {
                this.setBroadcaster(pid);
                break;
            }
            case SPOKESMAN: {
                this.setSpokesman(pid);
                break;
            }
            case PRESENTER: {
                this.setPresenter(pid);
                break;
            }
            case LOCKPRESENTER: {
                this.setLockPresenter(pid);
                break;
            }
        }
    }


    public String getByType(MeetingControlType meetingControlType) {
        switch (meetingControlType) {
            case CHAIRMAN: {
                return this.chairman;
            }
            case BROADCASTER: {
                return this.broadcaster;
            }
            case SPOKESMAN: {
                return this.spokesman;
            }
            case PRESENTER: {
                return this.presenter;
            }
            case LOCKPRESENTER: {
                return this.lockPresenter;
            }
        }
        return null;
    }
}
