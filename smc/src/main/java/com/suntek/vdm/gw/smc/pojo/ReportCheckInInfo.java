package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

import java.util.List;

@Data
public class ReportCheckInInfo {
    /**
     * 签到信息列表
     */
    private List<CheckInInfo> checkInfoList;
}