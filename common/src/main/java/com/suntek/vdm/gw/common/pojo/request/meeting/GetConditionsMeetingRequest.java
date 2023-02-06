package com.suntek.vdm.gw.common.pojo.request.meeting;

import lombok.Data;

@Data
public class GetConditionsMeetingRequest {
    /**
     * 按会议主题、预订者、接入号模糊过滤（可选）
     */
    private String keyword;
    /**
     * 组织路径（可选）
     */
    private String path;
    /**
     * 过滤组织（可选）
     */
    private Organization organization;
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

    /**
     * 级联会议号 smc2.0
     */
    private String casConfId;

    private String smcAccessCode;


    public void setCasConfId(String casConfId) {
        if(casConfId.contains("**")){
            casConfId = casConfId.split("\\*\\*")[0];
        }
        this.casConfId = casConfId;
        this.keyword = casConfId;
    }
}
