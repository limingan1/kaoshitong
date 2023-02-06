package com.suntek.vdm.gw.smc.request.meeting.templates;

import lombok.Data;

@Data
public class GetTemplatesListRequest {
    /**
     *按会议主题过滤(可选)
     */
    private String keyword;

    /**
     *组织路径（可选）
     */
    private String path;

    /**
     *组织Id（可选）
     */
    private String organizationId;

    /**
     *是否活动会议（可选）
     */
    private Boolean active;
}
