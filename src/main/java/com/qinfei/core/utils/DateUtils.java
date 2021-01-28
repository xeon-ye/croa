package com.qinfei.core.utils;

import java.lang.management.ManagementFactory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.http.client.utils.DateUtils.formatDate;

/**
 * 文件名：DateUtils.java 日期处理相关工具类 版本信息：V1.0
 *
 * @author GZW
 */
@SuppressWarnings("ALL")
public class DateUtils {
    /**
     * 日志对象
     */
    private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);
    /**
     * 定义常量
     **/
    private static final String DATE_JFP = "yyyyMM";
    public static final String DATE_FULL = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_SMALL = "yyyy-MM-dd";
    public static final String DATE_KEY = "yyMMddHHmmss";
    private static final String YYYYMMDDHHMMSSSSS = "yyyyMMddHHmmssSSS";
    /**
     * yyyy-MM-dd HH:mm:ss
     */
    public static final String DEFAULT = "yyyy-MM-dd";

    /**
     * /** 使用预设格式提取字符串日期
     *
     * @param strDate 日期字符串
     * @return
     */
    private static Date parse(String strDate) {
        return parse(strDate, DATE_FULL);
    }

    /**
     * 使用用户格式提取字符串日期
     *
     * @param strDate 日期字符串
     * @param pattern 日期格式
     * @return
     */
    public static Date parse(String strDate, String pattern) {
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        try {
            return df.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 使用用户格式提取字符串日期
     *
     * @param date    日期
     * @param pattern 日期格式
     * @return
     */
    public static String format(Date date, String pattern) {
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        return df.format(date);
    }

    /**
     * 使用用户格式提取字符串日期
     *
     * @param date 日期
     * @return
     */
    public static String format(Date date) {
        SimpleDateFormat df = new SimpleDateFormat(DEFAULT);
        return df.format(date);
    }

    /**
     * 两个时间比较
     *
     * @param date
     * @return
     */
    public static int compareDateWithNow(Date date) {
        Date date2 = new Date();
        return date.compareTo(date2);
    }

    /**
     * 两个时间比较(时间戳比较)
     *
     * @param date
     * @return
     */
    public static int compareDateWithNow(long date) {
        long date2 = dateToUnixTimestamp();
        if (date > date2) {
            return 1;
        } else if (date < date2) {
            return -1;
        } else {
            return 0;
        }
    }

    /**
     * 获取当前时间
     *
     * @return
     */
    public static Date now() {
        return new Date();
    }

    /**
     * 获取系统当前时间
     *
     * @return
     */
    public static String getNowTime() {
        SimpleDateFormat df = new SimpleDateFormat(DATE_FULL);
        return df.format(new Date());
    }

    /**
     * 获取指定格式的时间字符串 yyyyMMddHHmmssSSS 年月日时分秒毫秒
     *
     * @return
     */
    public static String getStr() {
        SimpleDateFormat df = new SimpleDateFormat(YYYYMMDDHHMMSSSSS);
        return df.format(new Date());
    }

    /**
     * 获取系统当前时间
     *
     * @return
     */
    public static String getNowTime(String type) {
        SimpleDateFormat df = new SimpleDateFormat(type);
        return df.format(new Date());
    }

    /**
     * 获取系统当前计费期
     *
     * @return
     */
    public static String getJFPTime() {
        SimpleDateFormat df = new SimpleDateFormat(DATE_JFP);
        return df.format(new Date());
    }

    /**
     * 将指定的日期转换成Unix时间戳
     *
     * @param date 需要转换的日期 yyyy-MM-dd HH:mm:ss
     * @return long 时间戳
     */
    public static long dateToUnixTimestamp(String date) {
        long timestamp = 0;
        try {
            timestamp = new SimpleDateFormat(DATE_FULL).parse(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timestamp;
    }

    /**
     * 将指定的日期转换成Unix时间戳
     *
     * @param date 需要转换的日期 yyyy-MM-dd
     * @return long 时间戳
     */
    public static long dateToUnixTimestamp(String date, String dateFormat) {
        long timestamp = 0;
        try {
            timestamp = new SimpleDateFormat(dateFormat).parse(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timestamp;
    }

    /**
     * 将当前日期转换成Unix时间戳
     *
     * @return long 时间戳
     */
    private static long dateToUnixTimestamp() {
        return new Date().getTime();
    }

    /**
     * 将Unix时间戳转换成日期
     *
     * @param timestamp 时间戳
     * @return String 日期字符串
     */
    public static String unixTimestampToDate(long timestamp) {
        SimpleDateFormat sd = new SimpleDateFormat(DATE_FULL);
        sd.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        return sd.format(new Date(timestamp));
    }

    /**
     * 字符串转换为对应日期(可能会报错异常)
     *
     * @param source
     * @param pattern
     * @return
     */
    public static Date getDate(String source, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Date date = null;
        try {
            date = sdf.parse(source);
        } catch (ParseException e) {
            logger.error("字符串转换日期异常", e);
        }
        return date;
    }
    // ----------------判断-----------------------------------------------

    /**
     * 是否是润年
     *
     * @param yearNum
     * @return
     */
    public static boolean isLeapYear(int yearNum) {
        if ((yearNum % 4 == 0) && (yearNum % 100 != 0))
            return true;
        else return yearNum % 400 == 0;
    }

    /**
     * 判断是否是日期
     *
     * @param date
     * @return
     */
    private static boolean isDate(String date) {
        // 判断年月日的正则表达式，接受输入格式为2010-12-24，可接受平年闰年的日期
        String regex = "(([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8]))))|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))-02-29)";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(date).matches();
    }

    /**
     * 验证是不是生日
     *
     * @param birthday
     * @return
     */
    public static boolean verifyBirthDay(String birthday) {
        if (TextUtils.isEmpty(birthday))
            return false;
        if (!birthday.contains("-"))
            return false;
        String[] arr = birthday.split("-");
        if (null == arr || arr.length != 3 || arr[0].length() != 4 || arr[1].length() != 2 || arr[2].length() != 2)
            return false;
        int year = getYear(new Date());
        int birthYear = Integer.parseInt(arr[0]);
        if (birthYear <= 1900 || birthYear > year)
            return false;
        String curDate = format(new Date(), DEFAULT);
        if (birthday.compareTo(curDate) > 0)
            return false;
        return isDate(birthday);
    }

    /**
     * 时间相减
     *
     * @param strDateBegin
     * @param strDateEnd
     * @param iType
     * @return
     */
    public static int getDiffDate(String strDateBegin, String strDateEnd, int iType) {
        Calendar calBegin = Calendar.getInstance();
        calBegin.setTime(parse(strDateBegin, DEFAULT));
        Calendar calEnd = Calendar.getInstance();
        calBegin.setTime(parse(strDateEnd, DEFAULT));
        long lBegin = calBegin.getTimeInMillis();
        long lEnd = calEnd.getTimeInMillis();
        if (iType == Calendar.SECOND)
            return (int) ((lEnd - lBegin) / 1000L);
        if (iType == Calendar.MINUTE)
            return (int) ((lEnd - lBegin) / 60000L);
        if (iType == Calendar.HOUR)
            return (int) ((lEnd - lBegin) / 3600000L);
        if (iType == Calendar.DAY_OF_MONTH) {
            return (int) ((lEnd - lBegin) / 86400000L);
        }
        return -1;
    }

    /**
     * 计算年份 在指定时间内加减年份
     *
     * @param date 指定时间
     * @param year 加减年份 加为正数 1 减为负数-1
     * @return 返回计算后的时间
     */
    public static Date calYear(Date date, int year) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.YEAR, year);
        return c.getTime();
    }

    /**
     * 计算年份 在当前时间内加减年份
     *
     * @param year 加减年份 加为正数 1 减为负数-1
     * @return 返回计算后的时间
     */
    public static Date calYear(int year) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.YEAR, year);
        return c.getTime();
    }

    /**
     * 计算月份 在指定时间内加减月份
     *
     * @param date  指定时间
     * @param month 加减月份 加为正数 1 减为负数-1
     * @return 返回计算后的时间
     */
    public static Date calMonth(Date date, int month) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.MONTH, month);
        return c.getTime();
    }

    /**
     * 计算月份 在当前时间内加减月份
     *
     * @param month 加减月份 加为正数 1 减为负数-1
     * @return 返回计算后的时间
     */
    public static Date calMonth(int month) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.MONTH, month);
        return c.getTime();
    }

    /**
     * 计算天数 在指定时间内加减月天数
     *
     * @param date 指定时间
     * @param day  加减天数 加为正数 1 减为负数-1
     * @return 返回计算后的时间
     */
    public static Date calDay(Date date, int day) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, day);
        return c.getTime();
    }

    /**
     * 计算天数 在当前时间内加减天数
     *
     * @param day 加减天数 加为正数 1 减为负数-1
     * @return 返回计算后的时间
     */
    public static Date calDay(int day) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DAY_OF_MONTH, day);
        return c.getTime();
    }

    /**
     * 时间差
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static int getDiffDays(Date startDate, Date endDate) {
        int days = 0;
        if (startDate.after(endDate)) {
            Date temp = startDate;
            startDate = endDate;
            endDate = temp;
        }
        days = (int) (endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24);
        return days;
    }

    /**
     * 当前日期的后几天
     *
     * @param date
     * @param n
     * @return
     */
    public static Date getAfterHour(Date date, int n) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.HOUR, n);
        return c.getTime();
    }

    /**
     * 当前日期的后几天
     *
     * @param date
     * @param n
     * @return
     */
    public static Date getAfterDay(Date date, int n) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, n);
        return c.getTime();
    }

    /**
     * 获取年份
     *
     * @param date
     * @return
     */
    private static int getYear(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.YEAR);
    }

    /**
     * 获取月份
     *
     * @param date
     * @return
     */
    public static int getMonth(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.MONTH) + 1;
    }

    /**
     * 获取日
     *
     * @param date
     * @return
     */
    public static int getDay(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 获取星期
     *
     * @param date
     * @return
     */
    public static int getWeek(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.DAY_OF_WEEK);
    }

    /**
     * 获取时间
     *
     * @param date
     * @return
     */
    public static int getHour(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * 获取分种
     *
     * @param date
     * @return
     */
    public static int getMinute(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.MINUTE);
    }

    /**
     * 获取秒
     *
     * @param date
     * @return
     */
    public static int getSecond(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.SECOND);
    }

    // --------------获取星期几---------------------------------------------------

    /**
     * 获取星期几
     *
     * @param strDate
     * @return
     */
    public static String getWeekDayName(String strDate) {
        String[] mName = {"日", "一", "二", "三", "四", "五", "六"};
        Date date = parse(strDate);
        int week = getWeek(date);
        return "星期" + mName[week];
    }

    public static String getWeekDayName(Date date) {
        String[] mName = {"日", "一", "二", "三", "四", "五", "六"};
        int week = getWeek(date);
        return "星期" + mName[week];
    }

    /**
     * 一年中的星期几
     *
     * @return
     */
    public static int getWeekNumOfYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }

    public static int getWeekNumOfYear(String date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parse(date, DEFAULT));
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }

    /**
     * 获取本周星期一的日期
     *
     * @param yearNum
     * @param weekNum
     * @return
     * @throws ParseException
     */
    public static String getYearWeekFirstDay(int yearNum, int weekNum) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, yearNum);
        cal.set(Calendar.WEEK_OF_YEAR, weekNum);
        cal.set(Calendar.DAY_OF_WEEK, 2);
        String tempYear = Integer.toString(yearNum);
        String tempMonth = Integer.toString(cal.get(Calendar.MONTH) + 1);
        String tempDay = Integer.toString(cal.get(Calendar.DAY_OF_MONTH) - 1);
        return tempYear + "-" + tempMonth + "-" + tempDay;
    }

    /**
     * 获取本周星期天的日期
     *
     * @param yearNum
     * @param weekNum
     * @return
     * @throws ParseException
     */
    public static String getYearWeekEndDay(int yearNum, int weekNum) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, yearNum);
        cal.set(Calendar.WEEK_OF_YEAR, weekNum + 1);
        cal.set(Calendar.DAY_OF_WEEK, 1);

        String tempYear = Integer.toString(yearNum);
        String tempMonth = Integer.toString(cal.get(Calendar.MONTH) + 1);
        String tempDay = Integer.toString(cal.get(Calendar.DAY_OF_MONTH) - 1);
        return tempYear + "-" + tempMonth + "-" + tempDay;
    }

    // --------------获取天数---------------------------------------------------

    /**
     * 获取某年某月的第一天
     *
     * @param yearNum
     * @param monthNum
     * @return
     */
    public static Date getYearMonthFirstDay(int yearNum, int monthNum) {
        Calendar cal = Calendar.getInstance();
        cal.set(yearNum, monthNum - 1, 1, 0, 0, 0);
        cal.set(14, 0);
        return cal.getTime();
    }

    /**
     * 获取某年下个月的第一天
     *
     * @param yearNum
     * @param monthNum
     * @return
     */
    public static Date getNextYearMonthFirstDay(int yearNum, int monthNum) {
        Calendar cal = Calendar.getInstance();
        cal.set(yearNum, monthNum, 1, 0, 0, 0);
        cal.set(14, 0);
        return cal.getTime();
    }

    /**
     * 获取某年某月的最后一天
     *
     * @param yearNum
     * @param monthNum
     * @return
     */
    public static Date getYearMonthEndDay(int yearNum, int monthNum) {
        Calendar cal = Calendar.getInstance();
        cal.set(yearNum, monthNum, 0, 0, 0, 0);
        cal.set(14, 0);
        return cal.getTime();
    }

    /**
     * 获取某月的第一天
     *
     * @param date
     * @return
     */
    public static Date getYearMonthFirstDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(5, 1);
        cal.set(11, 0);
        cal.set(12, 0);
        cal.set(13, 0);
        cal.set(14, 0);
        return cal.getTime();
    }

    /**
     * 获取下一年的第一天
     *
     * @param date
     * @return
     */
    public static Date getNextYearMonthFirstDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(2, 1);
        cal.set(5, 1);
        cal.set(11, 0);
        cal.set(12, 0);
        cal.set(13, 0);
        cal.set(14, 0);
        return cal.getTime();
    }

    /**
     * 获取当前月的最后一天
     *
     * @param date
     * @return
     */
    public static Date getYearMonthEndDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(5, cal.getActualMaximum(5));
        cal.set(11, 0);
        cal.set(12, 0);
        cal.set(13, 0);
        cal.set(14, 0);
        return cal.getTime();
    }

    /**
     * 获取当年的第一天
     *
     * @param yearNum
     * @return
     */
    public static Date getYearFirstDay(int yearNum) {
        Calendar cal = Calendar.getInstance();
        cal.set(yearNum, 0, 1, 0, 0, 0);
        cal.set(14, 0);
        return cal.getTime();
    }

    /**
     * 获取下一年的第一天
     *
     * @param yearNum
     * @return
     */
    public static Date getNextYearFirstDay(int yearNum) {
        Calendar cal = Calendar.getInstance();
        cal.set(yearNum, 12, 1, 0, 0, 0);
        cal.set(14, 0);
        return cal.getTime();
    }

    /**
     * 获取当年的最后一天
     *
     * @param yearNum
     * @return
     */
    public static Date getYearEndDay(int yearNum) {
        Calendar cal = Calendar.getInstance();
        cal.set(yearNum, 12, 0, 0, 0, 0);
        cal.set(14, 0);
        return cal.getTime();
    }

    /**
     * 获取当前星期
     *
     * @param strDate
     * @param weekNum
     * @return
     */
    public static String getWeek(String strDate, int weekNum) {
        Calendar c = Calendar.getInstance();
        c.setTime(parse(strDate));
        if (weekNum == 1)
            c.set(7, 2);
        else if (weekNum == 2)
            c.set(7, 3);
        else if (weekNum == 3)
            c.set(7, 4);
        else if (weekNum == 4)
            c.set(7, 5);
        else if (weekNum == 5)
            c.set(7, 6);
        else if (weekNum == 6)
            c.set(7, 7);
        else if (weekNum == 0)
            c.set(7, 1);
        return format(c.getTime());
    }

    public static Date getWeek(Date date, int weekNum) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        if (weekNum == 1)
            c.set(7, 2);
        else if (weekNum == 2)
            c.set(7, 3);
        else if (weekNum == 3)
            c.set(7, 4);
        else if (weekNum == 4)
            c.set(7, 5);
        else if (weekNum == 5)
            c.set(7, 6);
        else if (weekNum == 6)
            c.set(7, 7);
        else if (weekNum == 0)
            c.set(7, 1);
        return c.getTime();
    }

    /**
     * 下个月日期
     *
     * @param date
     * @return
     */
    public static Date getNextMonday(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        do
            c.add(Calendar.DAY_OF_MONTH, 1);
        while (c.get(Calendar.DAY_OF_WEEK) != 2);
        return c.getTime();
    }

    /**
     * 获得某一日期的前一天
     */
    private static Date getPreviousDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, -1);

        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return getSqlDate(calendar.getTime());
    }

    /**
     * 获得某年某月最后一天的日期
     */
    public static Date getLastDayOfMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DATE, 1);
        return getPreviousDate(getSqlDate(calendar.getTime()));
    }

    /**
     * 获取一个月的天数
     *
     * @param year
     * @param month
     * @return
     */
    public static int getDaysInMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);// Java月份才0开始算
        return cal.getActualMaximum(Calendar.DATE);
    }

    // ----------------根据用户生日计算年龄-------------------------------------------------

    /**
     * 根据用户生日计算年龄
     */
    public static int getAgeByBirthday(Date birthday) {
        Calendar cal = Calendar.getInstance();
        if (cal.before(birthday)) {
            throw new IllegalArgumentException("The birthDay is before Now.It's unbelievable!");
        }
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
                if (dayOfMonthNow < dayOfMonthBirth) {
                    age--;
                }
            } else {
                age--;
            }
        }
        return age;
    }

    /**
     * 由java.util.Date到java.sql.Date的类型转换
     */
    private static Date getSqlDate(java.util.Date date) {
        return new Date(date.getTime());
    }

    /**
     * 获取一年前的时间
     *
     * @return
     */
    public static Date getLastYearDate() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.YEAR, -1);
        return c.getTime();
    }

	/**
	 * 获取当前季度
	 *
	 */
	public static Integer getQuarter() {
		Calendar c = Calendar.getInstance();
		int month = c.get(c.MONTH) + 1;
		int quarter = 0;
		if (month >= 1 && month <= 3) {
			quarter = 1;
		} else if (month >= 4 && month <= 6) {
			quarter = 2;
		} else if (month >= 7 && month <= 9) {
			quarter = 3;
		} else {
			quarter = 4;
		}
		return quarter;
	}

	/**
	 * 获取某季度的第一天和最后一天
	 */
	public static String[] getCurrQuarter(int num) {
		String[] s = new String[2];
		String str = "";
		// 设置本年的季
		Calendar quarterCalendar = null;
		switch (num) {
			case 1: // 本年到现在经过了一个季度，在加上前4个季度
				quarterCalendar = Calendar.getInstance();
				quarterCalendar.set(Calendar.MONTH, 3);
				quarterCalendar.set(Calendar.DATE, 1);
				quarterCalendar.add(Calendar.DATE, -1);
				str = format(quarterCalendar.getTime(), "yyyy-MM-dd");
				s[0] = str.substring(0, str.length() - 5) + "01-01";
				s[1] = str;
				break;
			case 2: // 本年到现在经过了二个季度，在加上前三个季度
				quarterCalendar = Calendar.getInstance();
				quarterCalendar.set(Calendar.MONTH, 6);
				quarterCalendar.set(Calendar.DATE, 1);
				quarterCalendar.add(Calendar.DATE, -1);
				str = format(quarterCalendar.getTime(), "yyyy-MM-dd");
				s[0] = str.substring(0, str.length() - 5) + "04-01";
				s[1] = str;
				break;
			case 3:// 本年到现在经过了三个季度，在加上前二个季度
				quarterCalendar = Calendar.getInstance();
				quarterCalendar.set(Calendar.MONTH, 9);
				quarterCalendar.set(Calendar.DATE, 1);
				quarterCalendar.add(Calendar.DATE, -1);
				str = format(quarterCalendar.getTime(), "yyyy-MM-dd");
				s[0] = str.substring(0, str.length() - 5) + "07-01";
				s[1] = str;
				break;
			case 4:// 本年到现在经过了四个季度，在加上前一个季度
				quarterCalendar = Calendar.getInstance();
				str = format(quarterCalendar.getTime(), "yyyy-MM-dd");
				s[0] = str.substring(0, str.length() - 5) + "10-01";
				s[1] = str.substring(0, str.length() - 5) + "12-31";
				break;
		}
		return s;
	}

	/**
	 * 获取某季度的第一天
	 * @param num
	 * @return
	 */
	public static String getCurrQuarterFirstDay(int num) {
		String quarterFirstDay ="";
		String str = "";
		// 设置本年的季
		Calendar quarterCalendar = null;
		switch (num) {
			case 1: // 本年到现在经过了一个季度，在加上前4个季度
				quarterCalendar = Calendar.getInstance();
				quarterCalendar.set(Calendar.MONTH, 3);
				quarterCalendar.set(Calendar.DATE, 1);
				quarterCalendar.add(Calendar.DATE, -1);
				str = format(quarterCalendar.getTime(), "yyyy-MM-dd");
				quarterFirstDay = str.substring(0, str.length() - 5) + "01-01";
				break;
			case 2: // 本年到现在经过了二个季度，在加上前三个季度
				quarterCalendar = Calendar.getInstance();
				quarterCalendar.set(Calendar.MONTH, 6);
				quarterCalendar.set(Calendar.DATE, 1);
				quarterCalendar.add(Calendar.DATE, -1);
				str = format(quarterCalendar.getTime(), "yyyy-MM-dd");
				quarterFirstDay = str.substring(0, str.length() - 5) + "04-01";
				break;
			case 3:// 本年到现在经过了三个季度，在加上前二个季度
				quarterCalendar = Calendar.getInstance();
				quarterCalendar.set(Calendar.MONTH, 9);
				quarterCalendar.set(Calendar.DATE, 1);
				quarterCalendar.add(Calendar.DATE, -1);
				str = format(quarterCalendar.getTime(), "yyyy-MM-dd");
				quarterFirstDay = str.substring(0, str.length() - 5) + "07-01";
				break;
			case 4:// 本年到现在经过了四个季度，在加上前一个季度
				quarterCalendar = Calendar.getInstance();
				str = format(quarterCalendar.getTime(), "yyyy-MM-dd");
				quarterFirstDay = str.substring(0, str.length() - 5) + "10-01";
				break;
		}
		return quarterFirstDay;
	}

	/**
	 * 获取某个季度的最后一天
	 * @param num
	 * @return
	 */
	public static String getCurrQuarterEndDay(int num) {
		String quarterEndDay = "";
		String str = "";
		// 设置本年的季
		Calendar quarterCalendar = null;
		switch (num) {
			case 1: // 本年到现在经过了一个季度，在加上前4个季度
				quarterCalendar = Calendar.getInstance();
				quarterCalendar.set(Calendar.MONTH, 3);
				quarterCalendar.set(Calendar.DATE, 1);
				quarterCalendar.add(Calendar.DATE, -1);
				str = format(quarterCalendar.getTime(), "yyyy-MM-dd");
				quarterEndDay = str;
				break;
			case 2: // 本年到现在经过了二个季度，在加上前三个季度
				quarterCalendar = Calendar.getInstance();
				quarterCalendar.set(Calendar.MONTH, 6);
				quarterCalendar.set(Calendar.DATE, 1);
				quarterCalendar.add(Calendar.DATE, -1);
				str = format(quarterCalendar.getTime(), "yyyy-MM-dd");
				quarterEndDay = str;
				break;
			case 3:// 本年到现在经过了三个季度，在加上前二个季度
				quarterCalendar = Calendar.getInstance();
				quarterCalendar.set(Calendar.MONTH, 9);
				quarterCalendar.set(Calendar.DATE, 1);
				quarterCalendar.add(Calendar.DATE, -1);
				str = format(quarterCalendar.getTime(), "yyyy-MM-dd");
				quarterEndDay = str;
				break;
			case 4:// 本年到现在经过了四个季度，在加上前一个季度
				quarterCalendar = Calendar.getInstance();
				str = format(quarterCalendar.getTime(), "yyyy-MM-dd");
				quarterEndDay = str.substring(0, str.length() - 5) + "12-31";
				break;
		}
		return quarterEndDay;
	}

    public static String getYearAndMonthStr(String day) {
        try {
            StringBuffer sb = new StringBuffer();
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(day);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.setFirstDayOfWeek(Calendar.MONDAY);
            Integer month = calendar.get(Calendar.MONTH)+1;
            String year =  day.substring(0, 4);
            if(month<10){
                sb.append(year).append("-0").append(month);
            }else{
                sb.append(year).append("-").append(month);
            }
            return sb.toString() ;
        } catch (ParseException pe) {
            return "";
        }
    }
    public static String getYearAndMonthStr2(Date date) {
        StringBuffer sb = new StringBuffer();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        Integer year = calendar.get(Calendar.YEAR);
        Integer month = calendar.get(Calendar.MONTH)+1;
        if(month<10){
            sb.append(year).append("-0").append(month);
        }else{
            sb.append(year).append("-").append(month);
        }
        return sb.toString() ;
    }
    /**
     * 获取服务器启动时间
     */
    public static Date getServerStartDate() {
        long time = ManagementFactory.getRuntimeMXBean().getStartTime();
        return new Date(time);
    }
    /**
     * 计算两个时间差
     */
    public static String getDatePoor(Date endDate, Date nowDate) {
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        // long ns = 1000;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - nowDate.getTime();
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        // 计算差多少秒//输出结果
        // long sec = diff % nd % nh % nm / ns;
        return day + "天" + hour + "小时" + min + "分钟";
    }
}
