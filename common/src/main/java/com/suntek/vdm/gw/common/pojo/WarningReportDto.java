package com.suntek.vdm.gw.common.pojo;

import lombok.Data;

@Data
public class WarningReportDto {
    private String omcId; // 主键
    private String omcName; //告警名称
    private Integer omcLevel; //级别： 0 - 重要 ；1 - 一般
    /**
     * 类型
     * 0:通信告警
     * 1:设备告警
     * 2:业务质量告警
     * 3:环境告警
     * 4:业务配置告警
     */
    private Integer omcType; //告警类型
    private String omcSource; //来源
    private Integer isDelete; // 是否删除

    private WarningType warningType;

    public WarningReportDto() {
    }

    public WarningReportDto(ErrorResponse errorResponse) {

    }

    public WarningReportDto( String omcName, Integer omcLevel, Integer omcType, String omcSource, Integer isDelete) {
        this.omcName = omcName;
        this.omcLevel = omcLevel;
        this.omcType = omcType;
        this.omcSource = omcSource;
        this.isDelete = isDelete;
    }
}
