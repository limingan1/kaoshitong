package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

import java.util.List;

@Data
public class PeriodConferenceTime {
    /**
     * 周期单位
     */
    private String periodUnitType;

    /**
     * 每个周期单位间隔
     */
    private Integer durationPerPeriodUnit;

    /**
     * 周期会议开始时间(UTC 时间,格式为yyyy-MM-dd HH:mm:ss z)
     */
    private String startDate;

    /**
     * 周期会议结束时间(UTC 时间,格式为yyyy-MM-dd HH:mm:ss z)
     */
    private String endDate;

    /**
     * 针对按月模式有用， weekIndexInMonth 表示第个周(1~4)
     */
    private Integer weekIndexInMonthMode;

    /**
     * 以每月为单位，标识周几(1~7)
     */
    private Integer dayIndexInMonthMode;

    /**
     * 以每周为单位，标识周几(1~7)
     */
    private List<Number> dayLists;
}