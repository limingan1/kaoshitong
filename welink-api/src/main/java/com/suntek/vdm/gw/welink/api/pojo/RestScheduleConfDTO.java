package com.suntek.vdm.gw.welink.api.pojo;

import lombok.Data;

import java.util.List;

@Data
public class RestScheduleConfDTO {
    /**
     * 0 : 普通会议。
     * 1：周期会议，此时
     * “cycleParams”必须填写。
     * default: 0
     */
    private Integer conferenceType;

    /**
     * 会议开始时间（UTC时间）。
     * 说明
     * 创建预约会议时，如果没有指定开 始时间或填空串，则表示会议马上 开始。
     * 格式：yyyy-MM-dd HH:mm
     */
    private String startTime;

    /**
     * 会议持续时长，单位分钟，最大 值为1440，最短15。default: 30
     */
    private Integer length;

    /**
     * 会议主题。长度限制为128个字 符。
     */
    private String subject;

    /**
     * 会议的媒体类型。
     * 由1个或多个枚举String组成，多 个枚举时，每个枚举值之间通过 “，”逗号分隔，枚举值如下： ● "Voice”：语音。
     * ● "Video”：标清视频。
     * ● "HDVideo”：高清视频（与 Video 互斥，如果同时选择 Video 、HDVideo，则系统默 认选择Video）。
     * ● "Telepresence”：智真(与 HDVideo、Video 互斥，如果 同时选择，系统使用 Telepresence)。（预留字
     * 段）
     * ● "Data ”：多媒体（AS根据系 统配置决定是否自动添加 Data）。
     */
    private String mediaTypes;

    /**
     * 软终端创建即时会议时在当前字 段带临时群组ID，由服务器在邀 请其他与会者时在或者 conference-info 头域中携带。 长度限制为31个字符。
     */
    private String groupuri;

    /**
     * 预定会议时，指定的与会者列 表。
     * 该与会者列表可以用于发送会议 通知、会议提醒、会议开始时候 进行自动邀请。
     */
    private List<Attendee> attendees;

    /**
     * 周期会议的参数，当会议是周期 会议的时候该参数必须填写，否 则服务器忽略该参数。
     * 该参数包括周期会议的开始日 期、结束日期、会议的周期和周 期中的开会时间点。
     */
    private CycleParams cycleParams;

    /**
     * 会议是否自动启动录制，在录播 类型为：录播、录播+直播时才 生效。
     * ● 1：自动启动录制。
     * ● 0：不自动启动录制。 default: 0
     */
    private Integer isAutoRecord;

    /**
     * 会议媒体加密模式。默认值由企 业级的配置填充
     * ● 0：自适应加密。 ● 1 : 强制加密。 ● 2 : 不加密。
     */
    private Integer encryptMode;

    /**
     * 会议的默认语言，默认值由会议 AS定义。
     * 对于系统支持的语言，按照 RFC3066规范传递。
     * ● zh-CN：简体中文。 ● en-US：美国英文。
     */
    private String language;

    /**
     * 开始时间的时区信息。时区信 息，参考时区映射关系。
     * maxLength: 77
     * minLength: 1
     */
    private String timeZoneID;

    /**
     * 录播类型。
     * ● 0: 禁用 。 ● 1: 直播 。 ● 2: 录播。
     * ● 3: 直播+录播。
     * default:0
     */
    private Integer recordType;

    /**
     * 主流直播地址，最大不超过255 个字符，在录播类型为：直播、 录播+直播时才生效。
     */
    private String liveAddress;

    /**
     * 辅流直播地址，最大不超过255 个字符，在录播类型为：直播、 录播+直播时才生效。
     */
    private String auxAddress;

    /**
     * 是否录制辅流，在录播类型为： 录播、录播+直播时才生效。
     * ● 0：否。 ● 1：是。
     */
    private Integer recordAuxStream;

    /**
     * 会议其他配置信息。
     */
    private ConfConfigInfo confConfiginfo;

    /**
     * 录播鉴权方式，在录播类型为:录 播、直播+录播时有效。 取值如 下：
     * ● 0：老的鉴权方式，url中携带 token鉴权。
     * ● 1：企业内会议用户鉴权。 ● 2：会议内会议用户鉴权。
     */
    private Integer recordAuthType;

    /**
     * 是否使用VMR召开预约会议。
     * ● 0：不使用云会议室。 ● 1：使用云会议室。 default: 0
     */
    private Integer vmrFlag;

    /**
     * 用于识别用户开会时绑定的VMR 会议室。最大长度不超过512个 字符。
     * ● 不为空，则用ID查询云会议 室信息。
     * ● 为空，则查用户所有云会议 室，如果有个人云会议室， 用个人云会议室ID；没有个 人云会议室，取最小云会议 室ID。
     */
    private String vmrID;
}