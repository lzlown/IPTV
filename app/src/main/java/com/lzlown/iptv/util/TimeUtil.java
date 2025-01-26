package com.lzlown.iptv.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtil {
    public static SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat epgFormat = new SimpleDateFormat("yyyy-MM-ddHH:mm");

    public static String getTime() {
        return timeFormat.format(new Date());
    }

    public static Date getTime(String date) {
        try {
            return timeFormat.parse(date);
        } catch (ParseException e) {
            return new Date();
        }
    }

    public static Date getEpgTime(String date) {
        try {
            return epgFormat.parse(date);
        } catch (ParseException e) {
            return new Date();
        }
    }

    public static String getTime(int day) {
        Date today = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(today);
        c.add(Calendar.DAY_OF_MONTH, day);
        Date tomorrow = c.getTime();
        return timeFormat.format(tomorrow);
    }

    public static Date getTimeToDate(int day) {
        Date today = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(today);
        c.add(Calendar.DAY_OF_MONTH, day);
        return c.getTime();
    }

    public static String getTimeS(String time) {
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss");
        Date today = null;
        try {
            today = f.parse(time);
            Calendar c = Calendar.getInstance();
            c.setTime(today);
            c.add(Calendar.HOUR_OF_DAY, -8);
            Date tomorrow = c.getTime();
            return f.format(tomorrow);
        } catch (ParseException e) {

        }
        return f.format(new Date());
    }

    public static String getTimeS(String time, int m) {
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss");
        Date today = null;
        try {
            today = f.parse(time);
            Calendar c = Calendar.getInstance();
            c.setTime(today);
            c.add(Calendar.MILLISECOND, m);
            Date tomorrow = c.getTime();
            return f.format(tomorrow);
        } catch (ParseException e) {

        }
        return f.format(new Date());
    }

    public static long getTime(String startTime, String endTime) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long eTime = 0;
        try {
            eTime = df.parse(endTime).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long sTime = 0;
        try {
            sTime = df.parse(startTime).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return (eTime - sTime) / 1000;
    }

    public static String getWeek() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        String[] daysOfWeek = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        return daysOfWeek[dayOfWeek - 1];
    }

}
