package com.lzlown.iptv.bean;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class LiveEpgItem {
    private String start;
    private String end;
    private String title;
    public Date startdateTime;
    public Date enddateTime;

    public LiveEpgItem(String start, String end, String title) {
        this.start = start;
        this.end = end;
        this.title = title;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        SimpleDateFormat userSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        userSimpleDateFormat.setTimeZone(TimeZone.getDefault());
        startdateTime = userSimpleDateFormat.parse(simpleDateFormat.format(new Date()) + " " + start + ":00 GMT+8:00", new ParsePosition(0));
        enddateTime = userSimpleDateFormat.parse(simpleDateFormat.format(new Date()) + " " + end + ":00 GMT+8:00", new ParsePosition(0));
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
