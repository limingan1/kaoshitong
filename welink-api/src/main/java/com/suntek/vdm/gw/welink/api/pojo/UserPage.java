package com.suntek.vdm.gw.welink.api.pojo;

import lombok.Data;

import java.util.List;

@Data
public class UserPage {
    /**
     * 页面起始页。
     */
    private Integer pageIndex;

    /**
     * 页面大小。
     */
    private Integer pageSize;

    /**
     * 总数量。
     */
    private Integer totalCount;

/**
 *查询结果。
 */
    private List<UserDTO>  data;
}