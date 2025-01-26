package com.lzlown.iptv.bean;

import java.util.ArrayList;

public class LiveSettingGroup {
    private int groupIndex;
    private String groupName;
    private ArrayList<LiveSettingItem> liveSettingItems;
    private Integer type=0;
    private Boolean select=false;
    private String val="";

    public int getGroupIndex() {
        return groupIndex;
    }

    public void setGroupIndex(int groupIndex) {
        this.groupIndex = groupIndex;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Boolean getSelect() {
        return select;
    }

    public void setSelect(Boolean select) {
        this.select = select;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public ArrayList<LiveSettingItem> getLiveSettingItems() {
        return liveSettingItems;
    }

    public void setLiveSettingItems(ArrayList<LiveSettingItem> liveSettingItems) {
        this.liveSettingItems = liveSettingItems;
    }
}
