package com.lzlown.iptv.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtil {
    public static SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd");

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
        Date today  = null;
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
    public static String getTimeS(String time,int m) {
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss");
        Date today  = null;
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

}
