package com.lzlown.iptv.bean;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class LiveEpgItem {
    public int index;
    public String start;
    public String end;
    public String title;
    public Date startdateTime;
    public Date enddateTime;
    public String originStart;
    public String originEnd;
    public Date epgDate;
    public String currentEpgDate = null;
    SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd");
    public LiveEpgItem(Date Date,String start, String end, String title, Integer num) {
        epgDate = Date;
        currentEpgDate = timeFormat.format(epgDate);
        this.start = start;
        this.end = end;
        this.title = title;
        this.originStart = start;
        this.originEnd = end;
        this.index = num;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        SimpleDateFormat userSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        userSimpleDateFormat.setTimeZone(TimeZone.getDefault());
        startdateTime = userSimpleDateFormat.parse(simpleDateFormat.format(epgDate) + " " + start + ":00 GMT+8:00", new ParsePosition(0));
        enddateTime = userSimpleDateFormat.parse(simpleDateFormat.format(epgDate) + " " + end + ":00 GMT+8:00", new ParsePosition(0));
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
