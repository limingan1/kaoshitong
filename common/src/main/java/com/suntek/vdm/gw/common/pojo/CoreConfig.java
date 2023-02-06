package com.suntek.vdm.gw.common.pojo;

public class CoreConfig {
    /**
     * token过期时间 单位秒
     */
    public static final int TOKEN_TIME_OUT = 3 * 60;
    /**
     * 内部订阅用户标识
     */
    public static final String INTERNAL_SUBSCRIBE_USER = "Admin";
    /**
     * 内部订阅用户Token  外部拦截器进行拦截 仅限内部调用
     */
    public static final String INTERNAL_USER_TOKEN = "Admin";
    /**
     * 会议订阅通知状态：取消会议
     */
    public static final String CONFERENCE_CANCEL = "CANCEL";
    /**
     * 会议订阅通知状态：创建会议
     */
    public static final String CONFERENCE_ONLINE = "ONLINE";

    public static final String PARTICIPANT_SIGN = "@";

    public static final String CONFERENCE_PARTICIPANT_ID = "00000000-0000-0000-0000-000000000000";
    /**
     * 默认加密编码
     */
    public static final String EN_TYPE = "512";
    /**
     *安全版本
     */
    public static final String SECURITY_VERSION = "V1";
    /**
     *日志打印分割线
     */
    public static final String splitLine = "------------";


}
