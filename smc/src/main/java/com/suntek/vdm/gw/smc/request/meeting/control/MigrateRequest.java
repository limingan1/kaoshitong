package com.suntek.vdm.gw.smc.request.meeting.control;

import com.suntek.vdm.gw.smc.pojo.MigrateInfo;
import lombok.Data;

import java.util.List;

@Data
public class MigrateRequest {
    /**
     * 目标会议Id(36字符)（请求参数体两参数只可填其一）
     */
  private  String targetConferenceId;

    /**
     * 会场迁移信息参数（请求参数体两参数只可填其一）
     */
  private List<MigrateInfo> migrateInfos;
}
