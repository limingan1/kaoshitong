package com.suntek.vdm.gw.common.enums;

public enum MeetingControlType {
    /**
     * 设置主席(会场Id)
     */
    CHAIRMAN,
    /**
     * 广播会场(会场Id，广播会议多画面传值00000000-0000-0000-0000-000000000000")
     */
    BROADCASTER,
    /**
     * 点名(会场Id)
     */
    SPOKESMAN,
    /**
     * 锁定演示(会场Id，锁定会议演示传值00000000-0000-0000-0000-000000000000")
     */
    LOCKPRESENTER,
    /**
     * 发送演示(会场Id)
     */
    PRESENTER,
    /**
     * 观看
     */
    WATCH;
}
