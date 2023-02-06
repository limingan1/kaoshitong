package com.suntek.vdm.gw.common.pojo.response;

import lombok.Data;

@Data
public class Sort {
    /**
     * 是否排序
     */
    private Boolean sorted;

    /**
     * 是否未排序
     */
    private Boolean unsorted;

    /**
     * 是否为空
     */
    private Boolean empty;

    public Sort() {
    }

    public Sort(Boolean sorted, Boolean unsorted, Boolean empty) {
        this.sorted = sorted;
        this.unsorted = unsorted;
        this.empty = empty;
    }
}