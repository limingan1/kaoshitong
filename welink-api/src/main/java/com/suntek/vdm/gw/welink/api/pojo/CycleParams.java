package com.suntek.vdm.gw.welink.api.pojo;

import lombok.Data;

import java.util.List;

@Data
public class CycleParams {
    /**
     * 周期会议的开始日期，格 式：YYYY-MM-DD。
     * 开始日期不能早于当前日 期
     */
    private String startDate;

    /**
     * 周期会议的结束日期，格 式：YYYY-MM-DD。
     * 开始日期和结束日期间的 时间间隔最长不能超过1 年。开始日期和结束日期 之间最多允许50个子会 议，若超过50个子会议， 会自动调整结束日期。
     */
    private String endDate;

    /**
     * 周期类型。
     * ● “Day”：天。
     * ● “Week”：星期。 ● “Month”：月。
     */
    private String cycle;

    /**
     * ● “cycle”选择了
     * “Day”，表示每几天 召开一次，取值范围 [1,60]。
     * ● “cycle”选择了
     * “Week”，表示每几 周召开一次，取值范 围[1,5]。
     * ● “cycle”选择了
     * “Month”，Interval 表示隔几月，取值范 围[1,12]。
     */
    private Integer Interval;

/**
 *周期内的会议召开点。仅 当按周和月时有效。
 例如：
 ● “cycle”选择了
 “Week”，poInt中 填入了两个元素1和
 3，则表示每个周一和 周三召开会议，0表示 周日。
 ● “cycle”选择了
 “Month”，poInt中 填入了12和20则表示 每个月的12号和20号 召开会议，取值范围 为[1,31]，若当月没有 该值，则为月末。
 */
    private List<Integer>  point;

    /**
     * 支持用户指定提前会议通 知的天数N，预订人收到 整个周期会议的通知，所 有与会人在每个子会议召 开时间的前N天收到会议 通知（包括日历）。 天 数N的输入根据间隔期进 行自动调整，如果按日每 隔2天召开，则N自动变 为2，如果为按周每2周的 周一、周二，则N自动变 为14。 约束：暂不考虑 夏令时处理。
     * maximum: 30
     * minimum: 1
     * default: 1
     */
    private Integer preRemindDays;
}