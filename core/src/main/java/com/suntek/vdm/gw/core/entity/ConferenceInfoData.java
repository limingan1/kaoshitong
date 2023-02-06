package com.suntek.vdm.gw.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@TableName(value = "v3_vdm_conference_info")
public class ConferenceInfoData {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    private String conferenceId;

    private String type;

    private String participantId;
}
