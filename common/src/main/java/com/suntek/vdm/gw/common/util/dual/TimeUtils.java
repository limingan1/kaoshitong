package com.suntek.vdm.gw.common.util.dual;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间格式转换
 */
@Slf4j
public class TimeUtils {


    /**
     * 字符串时间格式转换成时间戳
     *
     * @throws ParseException
     */
    public static String getTimeStr(String time) {
        String timeStr = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = simpleDateFormat.parse(time);
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
        }
        timeStr = String.valueOf(date.getTime());
        return timeStr;
    }

    /**
     * 时间戳转换成字符串类的时间格式
     *
     * @return
     */
    public static String getTime(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String d = format.format(time);
        return d;
    }

    /**
     * 判断两个时间是不是相隔24个小时 date2必须要大于date1
     *
     * @return
     */
    public static boolean isOver24Hours(String date1, String date2) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        boolean result = false;
        try {
            Date d1 = df.parse(date2);
            Date d2 = df.parse(date1);
            // 这样得到的差值是微秒级别
            long diff = d1.getTime() - d2.getTime();
            long seconds = diff / 1000;
            long get24Hours = 24 * 60 * 60;
            if (seconds > get24Hours) {
                result = true;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * 取当前时间
     *
     * @return
     */
    public static String getCurrentTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String d = format.format(new Date());
        return d;
    }

    public static String addDateByDay(String date, int days) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d1;
        long diff = 0;
        try {
            d1 = df.parse(date);
            diff = d1.getTime() + days * 24 * 60 * 60 * 1000L;
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
        }
        return getTime(diff);
    }

    public static String getOneDayAgo() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String d = format.format(calendar.getTime());
        return d;
    }

    /**
     * 在当前时间上增加hour小时后的时间
     *
     * @param hour 正数表示当前时间之后的hour小时时间，负数表示当前时间之前
     * @return
     */
    public static String getTimeAddHour(int hour) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, hour);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String d = format.format(calendar.getTime());
        return d;
    }

    /**
     * 获取年份
     *
     * @return
     */
    public static String getYear() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) + "";
    }

    /**
     * 获取月份
     *
     * @return
     */
    public static String getMonth() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String d = format.format(calendar.getTime());
        return d.substring(5, 7);
    }

    /**
     * 获取日期
     *
     * @return
     */
    public static String getDay() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String d = format.format(calendar.getTime());
        return d.substring(8);
    }

    /**
     * 获取当前天
     *
     * @return
     */
    public static String getCurrentDay() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(calendar.getTime());
    }

    /**
     * 获取微信上展示的时间 (3月15日 19:12)
     *
     * @param createTime YYYY-MM-DD HH:MI:SS
     * @return
     */
    public static String getDisplayTime(String createTime) {
        if (createTime == null) {
            return null;
        }
        if (createTime.length() >= 16) {
            String displayTime = createTime.substring(5, 16);
            return displayTime;
        } else {
            return createTime;
        }
    }

    /**
     * 改变日期格式
     *
     * @return
     */
    public static String changeFormat(String date, String pattern) {
        if (StringUtils.isEmpty(date)) {
            return null;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        try {
            return simpleDateFormat.format(simpleDateFormat.parse(date));
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /***
     *
     * @param date
     * @return
     */
    public static String changeFormatDefault(String date) {
        return changeFormat(date, "yyyy-MM-dd");
    }


    /**
     * 把2018-11-23T10:47:00Z转为时间戳
     *
     * @param time 时间字符串
     * @return 时间戳
     */
    public static long getTimeForTZ(String time) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            return formatter.parse(time).getTime();
        } catch (Exception pe) {
        }
        return 0;
    }


    /**
     * 获取UTC本机时间
     * @return
     */
    public static String getCurrentUTC() {
        String dateStr = null;
        try {
            Date date = null;
            String months = "", days = "", hours = "", sec = "", minutes = "";
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            StringBuilder utcTimeBuffer = new StringBuilder();
            Calendar cal = Calendar.getInstance();
            int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
            int dstOffset = cal.get(Calendar.DST_OFFSET);
            cal.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset));
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            int second = cal.get(Calendar.SECOND);
            months = month < 10 ? "0" + month : String.valueOf(month);
            minutes = minute < 10 ? "0" + minute : String.valueOf(minute);
            days = day < 10 ? "0" + day : String.valueOf(day);
            hours = hour < 10 ? "0" + hour : String.valueOf(hour);
            sec = second < 10 ? "0" + second : String.valueOf(second);
            utcTimeBuffer.append(year).append("-").append(months).append("-").append(days);
            utcTimeBuffer.append("T").append(hours).append(":").append(minutes).append(":").append(sec).append("Z");
            date = format.parse(utcTimeBuffer.toString());

            dateStr = format.format(date);
        } catch (Exception e) { }
        return dateStr;
    }
}
