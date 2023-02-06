package com.suntek.vdm.gw.common.pojo;

import com.suntek.vdm.gw.common.enums.MeetingControlType;
import lombok.Data;

@Data
public class ConferenceState {
    /**
     * 会议Id
     */
    private String conferenceId;

    /**
     * 全体静音
     */
    private Boolean mute;

    /**
     * 全体闭音
     */
    private Boolean quiet;

    /**
     * 主席会场Id
     */
    private String chairmanId;

    /**
     * 当前正在广播会场Id(广播会议多画面传"00000000-0000-0000-0000-000000000000")
     */
    private String broadcastId;

    /**
     * 发言会场Id
     */
    private String spokesmanId;

    /**
     * 演示会场Id
     */
    private String presenterId;

    /**
     * 锁定演示会场Id(锁定会议演示传"00000000-0000-0000-0000-000000000000")
     */
    private String lockPresenterId;

    /**
     * 是否正在播放AI字幕
     */
    private String subtitleStatus;

    /**
     * 录播状态
     */
    private int recordStatus;

    /**
     * 会议多画面信息
     */
    private MultiPicInfo multiPicInfo;

    /**
     * 录播视频源
     */
    private MultiPicInfo recordSource;

    /**
     * 是否开启声控切换
     */
    private Boolean enableVoiceActive;

    /**
     * 当前发言会场
     */
    private String currentSpokesmanId;

    /**
     * 是否锁定会议
     */
    private boolean lock;

    /**
     * 是否导播
     */
    private boolean directing;

    /**
     * 举手会场数量
     */
    private int handUpNum;

    /**
     * 是否是本地多画面
     */
    private boolean local;

    /**
     * 多画面轮询状态
     */
    private String multiPicPollStatus;

    /**
     * 定时广播状态
     */
    private String broadcastPollStatus;

    /**
     * 主席轮询状态
     */
    private String chairmanPollStatus;

    /**
     * 全局录制状态
     */
    private boolean globalRecordStatus;

    /**
     * 直播推流状态
     */
    private String pushStreamStatus;

    private long chairmanFlagTime = 0L;



    public String getByType(MeetingControlType meetingControlType) {
        switch (meetingControlType) {
            case CHAIRMAN: {
                return this.chairmanId;
            }
            case BROADCASTER: {
                return this.broadcastId;
            }
            case SPOKESMAN: {
                return this.spokesmanId;
            }
            case PRESENTER: {
                return this.presenterId;
            }
            case LOCKPRESENTER: {
                return this.lockPresenterId;
            }
        }
        return null;
    }
}