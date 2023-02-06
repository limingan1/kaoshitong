package com.suntek.vdm.gw.welink.api.pojo;

import lombok.Data;

import java.util.List;

@Data
public class RestBulkDelAttendReqBody {
    /**
     * 待删除列表
     */
    private List<DelAttendInfo> bulkDelAttendInfo;
}