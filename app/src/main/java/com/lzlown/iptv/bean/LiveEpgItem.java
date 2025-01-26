package com.lzlown.iptv.bean;

public class LiveEpgItem {
    public int index;
    public String start;
    public String end;
    public String title;
    public String currentEpgDate;

    public LiveEpgItem(String Date,String start, String end, String title, Integer num) {
        currentEpgDate = Date;
        this.start = start;
        this.end = end;
        this.title = title;
        this.index = num;
    }
}
