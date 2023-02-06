package com.suntek.vdm.gw.welink.api.pojo;

import com.suntek.vdm.gw.common.pojo.request.ScheduleConfBrief;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Data
public class ConferenceInfo {
    /**
     * 会议ID。长度限制为32个 字符。
     */
    private String conferenceID;

    /**
     * 会议主题。长度限制为 128个字符。
     */
    private String subject;

    /**
     * 会议方数。
     */
    private Integer size;

    /**
     * 时区参考。
     */
    private String timeZoneID;

    /**
     * 会议起始时间 (YYYY-MM- DD HH:MM )。
     */
    private String startTime;

    /**
     * 会议结束时间 (YYYY-MM- DD HH:MM )。
     */
    private String endTime;

    /**
     * 会议的媒体类型。
     * 由1个或多个枚举String组 成，多个枚举时，每个枚 举值之间通过”,”逗号分 隔。
     * ● “Voice”：语音。 ● “Video”：标清视
     * 频。
     * ● “HDVideo”：高清视 频（与“Video”互 斥，如果同时选择 “Video”、
     * “HDVideo”，则系统 默认选择
     * “Video”）。
     * ● “Telepresence”：智 真(与“HDVideo”、 “Video”互斥，如果 同时选择，系统使用 “Telepresence”)。 （预留字段）
     * ● “Data”：多媒体。
     */
    private String mediaTypes;

    /**
     * (目前只会返回Created和
     * Schedule状态， 如果会议 已经召开返回Created状 态，否则返回Schedule状 态)
     * ● “Schedule”：预定状 态。
     * ● “Creating”：正在创 建状态。
     * ● “Created”：会议已 经被创建，并正在召 开。
     * ● “Destroyed”：会议 已经关闭。
     */
    private String conferenceState;

    /**
     * 会议语言。
     */
    private String language;

    /**
     * 会议接入码。
     */
    private String accessNumber;

    /**
     * 会议密码条目。预订者返 回主持人密码和来宾密 码。
     * ● 主持人查询时返回主持 人密码。
     * ● 来宾查询时返回来宾密 码。
     */
    private List<PasswordEntry> passwordEntry;

    /**
     * 会议预订者userUUID。
     */
    private String userUUID;

    /**
     * 会议预订者帐号名称。长
     * 度最大限制为96个字符。
     */
    private String scheduserName;

    /**
     * 会议视频模式类型。 ● FREE：自由模式
     * ● FIXED：固定画面广播 模式。广播使用该枚举 值。
     * ● ROLLCALL：点名模式 ● BROADCAST：广播模
     * 式
     */
    private String confVideoMode;

    /**
     * ● 0 : 普通会议。
     */
    private Integer conferenceType;

    /**
     * 会议类型：
     * ● FUTURE
     * ● IMMEDIATELY
     * ● CYCLE
     */
    private String confType;

    /**
     * 周期会议的参数。当会议 是周期会议的时候携带该 参数。
     * 该参数包括周期会议的开 始日期、结束日期、会议 的周期和周期中的开会时 间点。
     */
    private CycleParams cycleParams;

    /**
     * 是否入会自动静音。 ● 0 : 不自动静音。
     * ● 1 : 自动静音。
     */
    private Integer isAutoMute;

    /**
     * 是否自动开启录音。 ● 0 : 不自动启动。
     * ● 1 : 自动启动。
     */
    private Integer isAutoRecord;

    /**
     * 主持人会议链接地址。
     */
    private String chairJoinUri;

    /**
     * 普通与会者会议链接地 址。最大长度1024
     */
    private String guestJoinUri;

    /**
     * 旁听者会议链接地址。最 大长度1024（预留字段）
     */
    private String audienceJoinUri;

    /**
     * 录播类型。
     * ● 0: 禁用 。
     * ● 1: 直播 。
     * ● 2: 录播 。
     * ● 3: 直播+录播。
     */
    private Integer recordType;

    /**
     * 辅流直播地址。
     */
    private String auxAddress;

    /**
     * 主流直播地址。
     */
    private String liveAddress;

    /**
     * 是否录制辅流。
     * ● 0：否。
     * ● 1：是。
     */
    private Integer recordAuxStream;

    /**
     * 录播鉴权方式，在录播类 型为:录播、直播+录播时 有效。 取值如下：
     * ● 0：老的鉴权方式，url 中携带token鉴权。
     * ● 1：企业内会议用户鉴 权。
     * ● 2：会议内会议用户鉴 权。
     */
    private Integer recordAuthType;

    /**
     * 直播地址。（配置直播房 间时会返回）
     */
    private String liveUrl;

    /**
     * 会议其他配置信息。
     */
    private ConfConfigInfo confConfiginfo;

    /**
     * 是否使用云会议室召开预
     * 约会议。
     * ● 0：不使用云会议室。 ● 1：使用云会议室。
     * 界面显示会议ID需要使 用
     * “vmrConferenceID” 作为会议ID；查询会议 详情、登录会控、一键 入会等会议业务操作依 然使用
     * “conferenceID”字
     * 段。
     */
    private Integer vmrFlag;

    /**
     * 仅历史会议返回值有效。 True:有录播文件； False:没有录播文件 default: false
     */
    private Boolean isHasRecordFile;

    /**
     * 云会议室id，如果
     * “vmrFlag ”为“1”，则 该字段不为空。
     */
    private String vmrConferenceID;

    /**
     * 会议的UUID。
     */
    private String confUUID;

    /**
     * 与会方信息。硬件终端/与 会人最多各显示20条记 录。
     */
    private List<PartAttendee> partAttendeeInfo;

    /**
     * 硬终端个数。
     */
    private Integer terminlCount;

    /**
     * 普通终端个数。
     */
    private Integer normalCount;

    /**
     * 会议预定者的企业名称。 最大长度96。
     */
    private String deptName;

    /**
     * 云会议室的ID
     */
    private String vmrID;

    public ScheduleConfBrief toScheduleConfBrief(String areaCode) {
        ScheduleConfBrief scheduleConfBrief = new ScheduleConfBrief();
        scheduleConfBrief.setId(conferenceID);
        scheduleConfBrief.setAccessCode(areaCode+conferenceID);
        scheduleConfBrief.setSubject(subject);
        scheduleConfBrief.setAccountName(scheduserName);
        scheduleConfBrief.setScheduleStartTime(startTime);
        scheduleConfBrief.setOrganizationName(deptName);
        scheduleConfBrief.setType(confType);
        scheduleConfBrief.setActive(true);
        scheduleConfBrief.setStage("ONLINE");
        scheduleConfBrief.setUsername(scheduserName);
        scheduleConfBrief.setDuration(calculateDuration(startTime, endTime,"yyyy-MM-dd HH:mm"));
        return scheduleConfBrief;
    }

    private Integer calculateDuration(String startTime,String endTime,String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            Date start = sdf.parse(startTime);
            Date end = sdf.parse(endTime);
            long different = end.getTime() - start.getTime();
            long days = different / (1000 * 60 * 60 * 24);
            long hours = (different - days * (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
            long minutes = (different - days * (1000 * 60 * 60 * 24) - hours * (1000 * 60 * 60)) / (1000 * 60);
            return (int) (minutes + days * (60 * 24) + hours * (60));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}