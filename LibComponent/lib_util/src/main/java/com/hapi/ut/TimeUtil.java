/*
 * Copyright (c) 2016  athou（cai353974361@163.com）.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hapi.ut;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public final class TimeUtil {

    public enum Format {
        Y_M_D_H_M_S("yyyy-MM-dd HH:mm:ss"),
        Y_M_D_H_M_S_SLASH("yyyy/MM/dd HH:mm:ss"),
        Y_M_D_H_M_S_CN("yyyy年MM月dd日 HH时mm分ss秒"),
        Y_M_D_H_M("yyyy-MM-dd HH:mm"),
        Y_M_D_H_M_SLASH("yyyy/MM/dd HH:mm"),
        Y_M_D_H_M_CN("yyyy年MM月dd日 HH时mm分"),
        Y_M_D("yyyy-MM-dd"),
        Y_M_D_SLASH("yyyy/MM/dd"),
        Y_M_D_CN("yyyy年MM月dd日"),
        Y_M("yyyy-MM"),
        Y_M_SLASH("yyyy/MM"),
        Y_M_CN("yyyy年MM月"),
        H_M_S("HH:mm:ss"),
        H_M("HH:mm"),
        Y_M_D_a_h_M("yyyy-MM-dd aa hh:mm"),
        a_h_M("aa hh:mm"),
        None("");

        private String format;

        Format(String format) {
            this.format = format;
        }

        public String getFormat() {
            return format;
        }
    }

    public static final int SECONDS_PER_MINUTE = 60;
    public static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * 60;
    public static final int SECONDS_PER_DAY = 24 * SECONDS_PER_HOUR;

    public static final long MILLIS_PER_SECOND = 1000L;
    public static final long MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;
    public static final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;
    public static final long MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR;

    /**
     * 判断是否为24小时格式
     *
     * @param context
     * @return
     */
    public static boolean is24Hour(Context context) {
        return DateFormat.is24HourFormat(context);
    }

    /**
     * 判断时间是否过期
     *
     * @param startTime
     * @param expriseTime
     * @return true: 日期可用，未过期 ; false: 日期不可用, 已过期
     */
    public static boolean timeIsValid(long startTime, long expriseTime) {
        if (System.currentTimeMillis() < startTime + expriseTime) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 默认为 "yyyy-MM-dd HH:mm:ss"
     *
     * @return
     */
    public static String getDateString() {
        return getDateString(new Date(), Format.Y_M_D_H_M_S.getFormat());
    }

    /**
     * 默认为 "yyyy-MM-dd HH:mm:ss"
     *
     * @return
     */
    public static String getDateString(long millTime) {
        return getDateString(millTime, Format.Y_M_D_H_M_S.getFormat());
    }

    /**
     * 默认为 "yyyy-MM-dd HH:mm:ss"
     *
     * @return
     */
    public static String getDateString(long millTime, String format) {
        return getDateString(new Date(millTime), format);
    }

    /**
     * 默认为 "yyyy-MM-dd HH:mm:ss"
     *
     * @param date
     * @return
     */
    public static String getDateString(Date date) {
        return getDateString(date, Format.Y_M_D_H_M_S.getFormat());
    }

    /**
     */
    public static String getDateString(String formate) {
        return getDateString(new Date(), formate);
    }

    /**
     * 默认为 "yyyy-MM-dd HH:mm:ss"
     *
     * @param date
     * @param formate 可为空
     * @return
     */
    public static String getDateString(Date date, String formate) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat dateFormat = null;
        if (TextUtils.isEmpty(formate)) {
            dateFormat = new SimpleDateFormat(Format.Y_M_D_H_M_S.getFormat());
        } else {
            dateFormat = new SimpleDateFormat(formate);
        }
        return dateFormat.format(date);
    }

    /**
     * 获取指定时区的时间
     *
     * @param date     当前日期
     * @param timezone 时区
     * @param formate  日期格式，可为空，若为空，格式默认为"yyyy-MM-dd HH:mm:ss"
     * @return
     */
    public static String getDateWithTimezone(Date date, int timezone, String formate) {
        TimeZone timeZone = TimeZone.getTimeZone("GMT" + (timezone < 0 ? timezone : "+" + timezone));
        return getDateWithTimezone(date, timeZone, formate);
    }

    /**
     * 获取指定时区的时间
     *
     * @param timeZoneOffset 时区偏移量
     * @param formate        日期格式，可为空，若为空，格式默认为"yyyy-MM-dd HH:mm:ss"
     */
    public static String getDateWithTimezone(float timeZoneOffset, String formate) {
        if (timeZoneOffset > 13 || timeZoneOffset < -12) {
            timeZoneOffset = 0;
        }
        int newTime = (int) (timeZoneOffset * 60 * 60 * 1000);
        TimeZone timeZone;
        String[] ids = TimeZone.getAvailableIDs(newTime);
        if (ids.length == 0) {
            timeZone = TimeZone.getDefault();
        } else {
            timeZone = new SimpleTimeZone(newTime, ids[0]);
        }
        return getDateWithTimezone(new Date(), timeZone, formate);
    }

    /**
     * 获取指定时区的时间
     *
     * @param date     当前日期
     * @param timezone 时区
     * @param formate  日期格式，可为空，若为空，格式默认为"yyyy-MM-dd HH:mm:ss"
     * @return
     */
    public static String getDateWithTimezone(Date date, TimeZone timezone, String formate) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat dateFormat = null;
        if (TextUtils.isEmpty(formate)) {
            dateFormat = new SimpleDateFormat(Format.Y_M_D_H_M_S.getFormat());
        } else {
            dateFormat = new SimpleDateFormat(formate);
        }
        dateFormat.setTimeZone(timezone);
        return dateFormat.format(date);
    }

    /**
     * 根据已有日期String转换指定的日期格式
     *
     * @param dateStr     日期字符串
     * @param fromFormate 原日期格式
     * @param toFormate   转化后的日期格式
     * @return
     */
    public static String reformateDateStr(String dateStr, String fromFormate, String toFormate) {
        if (StrUtil.isEmpty(dateStr) || StrUtil.isEmpty(fromFormate) || StrUtil.isEmpty(toFormate)) {
            return null;
        }
        Date date = getDateFromString(dateStr, fromFormate);
        return getDateString(date, toFormate);
    }

    /**
     * Get the date form the given dateString with the given Formate. If the
     * formate is null, will use the default formate
     * "yyyy-MM-dd HH:mm:ss"
     *
     * @param dateString
     * @param formate
     * @return
     */
    public static Date getDateFromString(String dateString, String formate) {
        SimpleDateFormat dateFormat = null;
        if (StrUtil.isEmpty(formate)) {
            dateFormat = new SimpleDateFormat(Format.Y_M_D_H_M_S.getFormat());
        } else {
            dateFormat = new SimpleDateFormat(formate);
        }

        Date date = null;
        try {
            date = dateFormat.parse(dateString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 获取2个日期的天数差 yyyy-MM-dd hh:mm:ss
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public static int getDaySpace(String startTime, String endTime) {
        return getDaySpace(startTime, endTime, Format.Y_M_D_H_M_S.getFormat());
    }

    /**
     * 获取2个日期的天数差
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public static int getDaySpace(String startTime, String endTime, String format) {
        return getDaySpace(getDateFromString(startTime, format), getDateFromString(endTime, format));
    }

    /**
     * 获取2个日期的天数差
     *
     * @param startDate
     * @param endDate
     */
    public static int getDaySpace(Date startDate, Date endDate) {
        return getDaySpace(startDate.getTime(), endDate.getTime());
    }

    /**
     * 获取2个日期的天数差
     *
     * @param startMills
     * @param endMills
     * @return
     */
    public static int getDaySpace(long startMills, long endMills) {
        Calendar startCal = Calendar.getInstance();
        //将ms部分取整 1480581171324 -> 1480581171000
        startCal.setTimeInMillis(startMills / 1000 * 1000);
        startCal.set(Calendar.MILLISECOND, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        Calendar endCal = Calendar.getInstance();
        endCal.setTimeInMillis(endMills / 1000 * 1000);
        endCal.set(Calendar.MILLISECOND, 0);
        endCal.set(Calendar.SECOND, 0);
        endCal.set(Calendar.MINUTE, 0);
        endCal.set(Calendar.HOUR_OF_DAY, 0);

        int days = 0;
        if (startCal.after(endCal)) {
            while (startCal.after(endCal)) {
                days++;
                endCal.add(Calendar.DATE, 1);
            }
        } else if (startCal.before(endCal)) {
            while (startCal.before(endCal)) {
                days--;
                endCal.add(Calendar.DATE, -1);
            }
        }
        startCal.clear();
        endCal.clear();

        startCal = null;
        endCal = null;
        return days;
    }

    /**
     * 获取2个日期的月份差
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static int getMonthSpace(Date startDate, Date endDate) {
        GregorianCalendar cal1 = new GregorianCalendar();
        GregorianCalendar cal2 = new GregorianCalendar();
        cal1.setTime(startDate);
        cal2.setTime(endDate);
        int year1 = cal1.get(Calendar.YEAR);
        int month1 = cal1.get(Calendar.MONTH);

        int year2 = cal2.get(Calendar.YEAR);
        int month2 = cal2.get(Calendar.MONTH);

        int monthCount1 = year1 * 12 + month1;
        int monthCount2 = year2 * 12 + month2;
        return monthCount2 - monthCount1;
    }

    /**
     * 拼接时间段，100s -> 00:01:40
     */
    public static String getHourAndMinuteAndSecondTime(int second) {
        int hours = second / 60 / 60;
        int minute = second / 60 % 60;
        int sec = second % 60;

        StringBuffer sb = new StringBuffer();
        if (hours < 10) {
            sb.append("0" + hours + "");
        } else {
            sb.append(hours + "");
        }
        sb.append(":");
        if (minute < 10) {
            sb.append("0" + minute + "");
        } else {
            sb.append(minute + "");
        }
        sb.append(":");
        if (sec < 10) {
            sb.append("0" + sec);
        } else {
            sb.append(sec + "");
        }
        return sb.toString();
    }

    /**
     * 拼接时间段  100s -> 01:40
     */
    public static String getMinuteAndSecendTime(int second) {
        int minute = second / 60;
        int sec = second % 60;
        StringBuffer sb = new StringBuffer();
        if (minute < 10) {
            sb.append("0" + minute + "");
        } else {
            sb.append(minute + "");
        }
        sb.append(":");
        if (sec < 10) {
            sb.append("0" + sec + "");
        } else {
            sb.append(sec + "");
        }
        return sb.toString();
    }

    /**
     * 根据指定格式获取当前时间
     *
     * @param symbols
     * @return mm-dd-yyyy
     */
    public static String getCurrentDate(String symbols) {
        Calendar cale = Calendar.getInstance(TimeZone.getDefault());
        int year = cale.get(Calendar.YEAR);
        int month = cale.get(Calendar.MONTH);
        int day = cale.get(Calendar.DAY_OF_MONTH);
        return month + symbols + day + symbols + year;
    }

    /**
     * 根据指定格式获取当前完整时间
     *
     * @param symbols
     * @return mm-dd-yyyy
     */
    public static String getCurrentFullDate(String symbols) {
        Calendar cale = Calendar.getInstance(TimeZone.getDefault());
        int year = cale.get(Calendar.YEAR);
        int month = cale.get(Calendar.MONTH);
        int day = cale.get(Calendar.DAY_OF_MONTH);
        return month + symbols + day + symbols + year;
    }

    /**
     * 获取系统时间秒数
     */
    public static long getCurrentTimeSecond() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 根据毫秒数获取天数
     *
     * @return
     */
    public static int getDaysFromMillis(long millis) {
        return (int) (millis / MILLIS_PER_DAY);
    }

    /**
     * 根据毫秒数获取小时数
     *
     * @return
     */
    public static int getHoursFromMillis(long millis) {
        return (int) (millis / MILLIS_PER_HOUR);
    }

    /**
     * 根据毫秒数获取分钟数
     *
     * @return
     */
    public static int getMinutsFromMillis(long millis) {
        return (int) (millis / MILLIS_PER_MINUTE);
    }

    /**
     * 根据当前年份和月份判断这个月的天数
     *
     * @param year
     * @param month
     * @return
     */
    public static int getDayNum(int year, int month) {
        int day;
        // 闰年
        if (year % 4 == 0 && year % 100 != 0) {
            if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
                day = 31;
            } else if (month == 2) {
                day = 29;
            } else {
                day = 30;
            }
        } else { // 平年
            if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
                day = 31;
            } else if (month == 2) {
                day = 28;
            } else {
                day = 30;
            }
        }
        return day;
    }

    /**
     * 判断2个日期是否同一天
     *
     * @param day1
     * @param day2
     * @return
     */
    public static boolean isSameDay(Date day1, Date day2) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String ds1 = sdf.format(day1);
        String ds2 = sdf.format(day2);
        if (ds1.equals(ds2)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断两个日期是否在同一周
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isSameWeekDates(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        int subYear = cal1.get(Calendar.YEAR) - cal2.get(Calendar.YEAR);
        if (0 == subYear) {
            if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR))
                return true;
        } else if (1 == subYear && 11 == cal2.get(Calendar.MONTH)) {
            // 如果12月的最后一周横跨来年第一周的话则最后一周即算做来年的第一周
            return cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR);
        } else if (-1 == subYear && 11 == cal1.get(Calendar.MONTH)) {
            return cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR);
        }
        return false;
    }

    /**
     * 判断两个日期是否在同一周
     *
     * @param startMills
     * @param endMills
     * @return
     */
    public static boolean isSameWeek(long startMills, long endMills) {
        return getDaySpace(startMills, endMills) <= 7;
    }

    /**
     * 指定时间毫秒数与当前时间是否相差超过1个小时
     *
     * @param timeMills
     * @return
     */
    public static boolean isMoreThanHour(long timeMills) {
        long from = new Date(timeMills).getTime();
        long now = System.currentTimeMillis();
        int hours = (int) ((now - from) / (1000 * 60 * 60));
        return hours >= 1;
    }

    /**
     * 根据用户生日计算年龄
     */
    public static int getAgeByBirthday(Date birthday) {
        return getAgeByBirthday(birthday, new Date());
    }

    /**
     * 根据用户生日计算年龄
     */
    public static int getAgeByBirthday(Date birthday, Date today) {
        if (birthday.after(today)) {
            throw new IllegalArgumentException(
                    "The birthDay is before Now.It's unbelievable!");
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);

        int yearNow = cal.get(Calendar.YEAR);
        int monthNow = cal.get(Calendar.MONTH) + 1;
        int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH);

        cal.setTime(birthday);
        int yearBirth = cal.get(Calendar.YEAR);
        int monthBirth = cal.get(Calendar.MONTH) + 1;
        int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);

        int age = yearNow - yearBirth;

        if (monthNow <= monthBirth) {
            if (monthNow == monthBirth) {
                // monthNow==monthBirth
                if (dayOfMonthNow < dayOfMonthBirth) {
                    age--;
                }
            } else {
                // monthNow>monthBirth
                age--;
            }
        }
        return age;
    }

    /**
     * 描述：标准化日期时间类型的数据，不足两位的补0.
     *
     * @param dateTime 预格式的时间字符串，如:2012-3-2 12:2:20
     * @return String 格式化好的时间字符串，如:2012-03-20 12:02:20
     */
    public static String dateTimeFormat(String dateTime) {
        StringBuilder sb = new StringBuilder();
        try {
            if (StrUtil.isEmpty(dateTime)) {
                return null;
            }
            String[] dateAndTime = dateTime.split(" ");
            if (dateAndTime.length > 0) {
                for (String str : dateAndTime) {
                    if (str.indexOf("-") != -1) {
                        String[] date = str.split("-");
                        for (int i = 0; i < date.length; i++) {
                            String str1 = date[i];
                            sb.append(StrUtil.strFormatTwoPlace(str1));
                            if (i < date.length - 1) {
                                sb.append("-");
                            }
                        }
                    } else if (str.indexOf(":") != -1) {
                        sb.append(" ");
                        String[] date = str.split(":");
                        for (int i = 0; i < date.length; i++) {
                            String str1 = date[i];
                            sb.append(StrUtil.strFormatTwoPlace(str1));
                            if (i < date.length - 1) {
                                sb.append(":");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return sb.toString();
    }

    /**
     * 获取某年第一天日期
     *
     * @param year 年份
     * @return Date
     */
    public static Date getFirstDayOfYear(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        Date currYearFirst = calendar.getTime();
        return currYearFirst;
    }

    /**
     * 获取某年最后一天日期
     *
     * @param year 年份
     * @return Date
     */
    public static Date getLastDayOfYear(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.roll(Calendar.DAY_OF_YEAR, -1);
        Date currYearLast = calendar.getTime();
        return currYearLast;
    }

    /**
     * 获取某年某月第一天日期
     *
     * @param year 年份
     * @return Date
     */
    public static Date getFirstDayOfMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getMinimum(Calendar.DATE));
        Date currYearFirst = calendar.getTime();
        return currYearFirst;
    }

    /**
     * 获取某年某月最后一天日期
     *
     * @param year 年份
     * @return Date
     */
    public static Date getLastDayOfMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DATE));
        Date currYearLast = calendar.getTime();
        return currYearLast;
    }

    /**
     * Returns the number of milliseconds that have passed on the specified day.
     *
     * @param hour
     * @param minute
     * @param second
     * @return
     */
    public static long millisecondsOfDay(int hour, int minute, int second) {
        long ret = 0;
        ret += hour * MILLIS_PER_HOUR;
        ret += minute * MILLIS_PER_MINUTE;
        ret += second * MILLIS_PER_SECOND;
        return ret;
    }

    public static List<Date> convertDateListBySecondTime(List<Long> list) {
        List<Date> dates = new ArrayList<Date>();
        if (list != null) {
            for (Long time : list) {
                dates.add(new Date(time * 1000));
            }
        }
        return dates;
    }

    public static List<Long> convertSecondTimeListByDate(List<Date> list) {
        List<Long> dates = new ArrayList<Long>();
        if (list != null) {
            for (Date date : list) {
                dates.add(date.getTime() / 1000);
            }
        }
        return dates;
    }

    /**
     * Given a date string list with the given formate.
     *
     * @param list
     * @return
     */
    public static List<Date> convertDateListByStringTime(List<String> list, String formate) {
        List<Date> dates = new ArrayList<Date>();
        if (list != null) {
            for (String time : list) {
                Date date = getDateFromString(time, formate);
                if (date != null) {
                    dates.add(date);
                }
            }
        }
        return dates;
    }

    /**
     * Convert the date list to String list with the given formate.
     *
     * @param list
     * @param formate
     * @return
     */
    public static List<String> convertStringTimeListByDate(List<Date> list, String formate) {
        List<String> dates = new ArrayList<String>();
        if (list != null) {
            for (Date date : list) {
                dates.add(getDateString(date, formate));
            }
        }
        return dates;
    }
}
