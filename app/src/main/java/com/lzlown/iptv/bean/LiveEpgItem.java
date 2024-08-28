package com.lzlown.iptv.bean;

public class LiveEpgItem {
    public int index;
    public String start;
    public String end;
    public String title;
    public String currentEpgDate = null;
    public LiveEpgItem(String Date,String start, String end, String title, Integer num) {
        currentEpgDate = Date;
        this.start = start;
        this.end = end;
        this.title = title;
        this.index = num;
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
