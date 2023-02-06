package com.suntek.vdm.gw.smc.response.meeting.management;

import com.suntek.vdm.gw.common.pojo.response.Pageable;
import com.suntek.vdm.gw.common.pojo.response.Sort;

import lombok.Data;

import java.util.List;

@Data
public class GetMeetingParticipantsResponse  {

    /**
     * 会议模板与会者列表
     */
    private List<GetTemplateParticipantResponse> content;

    /**
     * 总数
     */
    private Integer totalElements;

    /**
     * 总页数
     */
    private Integer totalPages;

    /**
     * 分页个数
     */
    private Integer size;

    /**
     * 当前页
     */
    private Integer number;

    /**
     * 最后一页
     */
    private Boolean last;

    /**
     * 排序
     */
    private Sort sort;

    /**
     * 当前页条数
     */
    private Integer numberOfElements;

    /**
     * 第一页
     */
    private Boolean first;

    /**
     * 是否为空
     */
    private Boolean empty;

    /**
     * 分页信息
     */
    private Pageable pageable;

    @Data
    class GetTemplateParticipantResponse {
        /**
         * 会议模板与会者Id(36字符)
         */
        private String id;

        /**
         * 标识
         */
        private String uri;

        /**
         * 与会者名称(1~128字符)
         */
        private String name;

        /**
         * 协议类型
         */
        private Integer ipProtocolType;

        /**
         * 会场连接模式
         */
        private String dialMode;

        /**
         * 速率
         */
        private Integer rate;

        /**
         * 是否语音会场
         */
        private Boolean voice;

        /**
         * 音频协议
         */
        private Integer audioProtocol;

        /**
         * 视频协议
         */
        private Integer videoProtocol;

        /**
         * 视频能力
         */
        private Integer videoResolution;

        /**
         * 数据会议协议
         */
        private Integer dataConfProtocol;

        /**
         * 服务区Id
         */
        private String serviceZoneId;

        /**
         * 服务区名称
         */
        private String serviceZoneName;
    }
}
