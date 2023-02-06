package com.suntek.vdm.gw.smc.response.meeting.templates;

import com.suntek.vdm.gw.common.pojo.response.Pageable;
import com.suntek.vdm.gw.common.pojo.response.Sort;
import lombok.Data;

import java.util.List;

@Data
public class GetTemplatesParticipantsResponse {
    /**
     *会议模板会场列表
     */
    private List<GetTemplatesResponse> content;

    /**
     *最后一页
     */
    private Boolean last;

    /**
     *总数
     */
    private Number totalElements;

    /**
     *总页数
     */
    private Number totalPages;

    /**
     *分页个数
     */
    private Number size;

    /**
     *当前页
     */
    private Number number;

    /**
     *排序
     */
    private Sort sort;

    /**
     *当前页条数
     */
    private Number numberOfElements;

    /**
     *第一页
     */
    private Boolean first;

    /**
     *是否为空
     */
    private Boolean empty;

    /**
     *分页信息
     */
    private Pageable pageable;
}
