package com.suntek.vdm.gw.smc.request.meeting.management;


public class GetCountMeetingRequest {
    /**
     * 按会议主题、预订者、接入号模糊过滤（可选）
     */
    private String keyword;
    /**
     * 组织路径（可选）
     */
    private String path;
    /**
     * 组织Id（可选）
     */
    private String organizationId;
    /**
     * 过滤是否活动会议（可选）
     */
    private Boolean active;
    /**
     * 过滤开始时间(UTC时间,格 式为yyyy-MM-dd HH:mm:ss z)（可选）
     */
    private String startTime;
    /**
     * 过滤结束时间(UTC时间,格 式为yyyy-MM-dd HH:mm:ss z)（可选）
     */
    private String endTime;
}
