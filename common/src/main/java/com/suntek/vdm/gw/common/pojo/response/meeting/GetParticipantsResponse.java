package com.suntek.vdm.gw.common.pojo.response.meeting;

import com.suntek.vdm.gw.common.pojo.ParticipantDetail;
import com.suntek.vdm.gw.common.pojo.response.Pageable;
import com.suntek.vdm.gw.common.pojo.response.Sort;
import lombok.Data;

import java.util.List;

@Data
public class GetParticipantsResponse  {
    /**
     * 内容
     */
    private List<ParticipantDetail> content;


    /**
     * 分页信息
     */
    private Pageable pageable;

    /**
     * 排序信息
     */
    private Sort sort;

    /**
     * 是否为最后一页
     */
    private Boolean last;

    /**
     * 总页数
     */
    private Integer totalPages;

    /**
     * 总条数
     */
    private Integer totalElements;

    /**
     * 当前页码
     */
    private Integer number;

    /**
     * 当前页条数
     */
    private Integer numberOfElements;

    /**
     * 是否为第一页
     */
    private Boolean first;

    /**
     * 是否为空
     */
    private Boolean empty;

    /**
     * 个数
     */
    private Integer size;


    public  boolean exist(){
        if (content!=null&&content.size()>0){
            return  true;
        }else{
            return  false;
        }
    }
}
