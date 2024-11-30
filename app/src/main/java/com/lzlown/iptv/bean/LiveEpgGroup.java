package com.lzlown.iptv.bean;

import java.util.List;

public class LiveEpgGroup {
    private String name;
    private List<LiveEpgItem> epgItems;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<LiveEpgItem> getEpgItems() {
        return epgItems;
    }

    public void setEpgItems(List<LiveEpgItem> epgItems) {
        this.epgItems = epgItems;
    }
}
