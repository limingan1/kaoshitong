package com.suntek.vdm.gw.common.pojo.response;

import lombok.Data;

@Data
public class Pageable {
    /**
     * 排序信息
     */
    private Sort sort;

    /**
     * 偏移量
     */
    private Integer offset;

    /**
     * 每页条数
     */
    private Integer pageSize;

    /**
     * 页数
     */
    private Integer pageNumber;

    /**
     * 是否未分页
     */
    private Boolean unpaged;

    /**
     * 是否分页
     */
    private Boolean paged;

    public Pageable() {
    }

    public Pageable(Sort sort, Integer offset, Integer pageSize, Integer pageNumber, Boolean unpaged, Boolean paged) {
        this.sort = sort;
        this.offset = offset;
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
        this.unpaged = unpaged;
        this.paged = paged;
    }
}