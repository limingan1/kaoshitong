package com.suntek.vdm.gw.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Data
@Slf4j
@TableName(value = "v3_vdm_meeting_template")
public class MeetingTemplateData {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    private String templateId;

    private String child;

    private Integer cascadeNum;

    private Integer type;

    private Date createTime;

    private String vmrNumber;
}
