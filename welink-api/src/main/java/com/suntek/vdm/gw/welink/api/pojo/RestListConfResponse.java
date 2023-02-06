package com.suntek.vdm.gw.welink.api.pojo;

import lombok.Data;

import java.util.List;

@Data
public class RestListConfResponse {
    /**
     * 第几条。
     */
    private Integer offset;

    /**
     * 每页的记录数。
     */
    private Integer limit;

    /**
     * 总记录数。
     */
    private Integer count;

    /**
     * 会议信息列表。
     */
    private List<ConferenceInfo> data;
}