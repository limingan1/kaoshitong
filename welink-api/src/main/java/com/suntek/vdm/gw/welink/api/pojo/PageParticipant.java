package com.suntek.vdm.gw.welink.api.pojo;

import lombok.Data;

import java.util.List;

@Data
public class PageParticipant {
    /**
     * 每页的记录数。
     */
    private Integer limit;

    /**
     * 总记录数。
     */
    private Integer count;

    /**
     * 记录数偏移，这一页之前 共有多少条。
     */
    private Integer offset;

    /**
     * 与会者信息。
     */
    private List<ParticipantInfo> data;
}