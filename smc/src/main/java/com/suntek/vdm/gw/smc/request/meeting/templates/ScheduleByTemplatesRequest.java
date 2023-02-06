package com.suntek.vdm.gw.smc.request.meeting.templates;

import com.suntek.vdm.gw.smc.pojo.PeriodConferenceTime;
import lombok.Data;

@Data
public class ScheduleByTemplatesRequest {
    /**
     * 会议主题(1~64字符)
     */
    private String subject;

    /**
     * 会议时长
     */
    private Integer duration;

    /**
     * 会议开始时间(UTC时间, 格 式 为 yyyy-MM-dd HH:mm:ss z)
     */
    private String scheduleStartTime;

    /**
     * 会议时间类型
     */
    private String conferenceTimeType;

    /**
     * 周期会议时间参数
     */
    private PeriodConferenceTime periodConferenceTime;
}
