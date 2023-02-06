package com.suntek.vdm.gw.welink.api.pojo;

import lombok.Data;

import java.util.List;

@Data
public class RestBulkHangUpReqBody {
/**
 *待批量挂断会场的列 表。
 */
    private List<String>  bulkHangUpParticipants;
}