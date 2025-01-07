package com.lzlown.iptv.config;

import android.util.Log;
import com.google.gson.JsonElement;
import com.lzlown.iptv.base.App;
import com.lzlown.iptv.bean.LiveSettingGroup;
import com.lzlown.iptv.bean.LiveSettingItem;
import com.lzlown.iptv.util.HawkConfig;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SettingConfig implements Config {
    private static volatile SettingConfig instance;
    private final List<LiveSettingGroup> liveSettingGroupList = new ArrayList<>();
    private final List<LiveSettingGroup> liveSettingGroupMoreList = new ArrayList<>();

    private SettingConfig() {

    }

    public static SettingConfig get() {
        if (instance == null) {
            synchronized (SettingConfig.class) {
                if (instance == null) {
                    instance = new SettingConfig();
                }
            }
        }
        return instance;
    }

    public List<LiveSettingGroup> getLiveSettingGroupList() {
        return liveSettingGroupList;
    }

    public List<LiveSettingGroup> getLiveSettingGroupMoreList() {
        return liveSettingGroupMoreList;
    }

    //右侧设置列表
    private void initLiveSettingGroupList() {
        ArrayList<ArrayList<String>> itemsArrayList = new ArrayList<>();
        ArrayList<String> groupNames = new ArrayList<>(Arrays.asList("节目单", "频道源", "画面比例", "更多设置"));
        ArrayList<String> epgItems = new ArrayList<>();
        ArrayList<String> sourceItems = new ArrayList<>();
        ArrayList<String> scaleItems = new ArrayList<>(Arrays.asList("默认", "16:9", "4:3", "填充", "原始", "裁剪"));
        ArrayList<String> personalSettingItems = new ArrayList<>(Arrays.asList("显示时间", "显示网速", "显示预告", "清理缓存"));
        itemsArrayList.add(epgItems);
        itemsArrayList.add(sourceItems);
        itemsArrayList.add(scaleItems);
        itemsArrayList.add(personalSettingItems);
        liveSettingGroupList.clear();
        for (int i = 0; i < groupNames.size(); i++) {
            LiveSettingGroup liveSettingGroup = new LiveSettingGroup();
            ArrayList<LiveSettingItem> liveSettingItemList = new ArrayList<>();
            liveSettingGroup.setGroupIndex(i);
            liveSettingGroup.setGroupName(groupNames.get(i));
            for (int j = 0; j < itemsArrayList.get(i).size(); j++) {
                LiveSettingItem liveSettingItem = new LiveSettingItem();
                liveSettingItem.setItemIndex(j);
                liveSettingItem.setItemName(itemsArrayList.get(i).get(j));
                liveSettingItemList.add(liveSettingItem);
            }
            liveSettingGroup.setLiveSettingItems(liveSettingItemList);
            liveSettingGroupList.add(liveSettingGroup);
        }
        liveSettingGroupList.get(3).getLiveSettingItems().get(0).setItemSelected(Hawk.get(HawkConfig.LIVE_SHOW_TIME, false));
        liveSettingGroupList.get(3).getLiveSettingItems().get(1).setItemSelected(Hawk.get(HawkConfig.LIVE_SHOW_SPEED, false));
        liveSettingGroupList.get(3).getLiveSettingItems().get(2).setItemSelected(Hawk.get(HawkConfig.LIVE_SHOW_EPG, false));
    }

    private ArrayList<String> uiTimeStrList = new ArrayList<>(Arrays.asList("5秒", "10秒", "15秒", "30秒", "60秒"));
    private ArrayList<Integer> uiTimeList = new ArrayList<>(Arrays.asList(5, 10, 15, 30, 60));
    private ArrayList<String> canTimeStrList = new ArrayList<>(Arrays.asList("5秒", "10秒", "15秒", "不更换"));
    private ArrayList<Integer> canTimeList = new ArrayList<>(Arrays.asList(5, 10, 15, -1));

    //右侧设置列表
    private void initLiveSettingGroupMoreList() {
        ArrayList<ArrayList<String>> itemsArrayList = new ArrayList<>();
        ArrayList<String> groupNames = new ArrayList<>(Arrays.asList("显示时间", "显示网速", "显示预告", "超时关闭界面", "超时更换频道", "清理缓存"));
        ArrayList<String> epgItems = new ArrayList<>();
        itemsArrayList.add(epgItems);
        itemsArrayList.add(epgItems);
        itemsArrayList.add(epgItems);
        itemsArrayList.add(uiTimeStrList);
        itemsArrayList.add(canTimeStrList);
        itemsArrayList.add(epgItems);
        liveSettingGroupMoreList.clear();
        for (int i = 0; i < groupNames.size(); i++) {
            LiveSettingGroup liveSettingGroup = new LiveSettingGroup();
            ArrayList<LiveSettingItem> liveSettingItemList = new ArrayList<>();
            liveSettingGroup.setGroupIndex(i);
            liveSettingGroup.setGroupName(groupNames.get(i));
            for (int j = 0; j < itemsArrayList.get(i).size(); j++) {
                LiveSettingItem liveSettingItem = new LiveSettingItem();
                liveSettingItem.setItemIndex(j);
                liveSettingItem.setItemName(itemsArrayList.get(i).get(j));
                liveSettingItemList.add(liveSettingItem);
            }
            liveSettingGroup.setLiveSettingItems(liveSettingItemList);
            liveSettingGroupMoreList.add(liveSettingGroup);
        }


        liveSettingGroupMoreList.get(0).setType(1);
        liveSettingGroupMoreList.get(1).setType(1);
        liveSettingGroupMoreList.get(2).setType(1);
        liveSettingGroupMoreList.get(3).setType(2);
        liveSettingGroupMoreList.get(4).setType(2);

        liveSettingGroupMoreList.get(0).setSelect(Hawk.get(HawkConfig.LIVE_SHOW_TIME, false));
        liveSettingGroupMoreList.get(1).setSelect(Hawk.get(HawkConfig.LIVE_SHOW_SPEED, false));
        liveSettingGroupMoreList.get(2).setSelect(Hawk.get(HawkConfig.LIVE_SHOW_EPG, false));
        liveSettingGroupMoreList.get(3).setVal(liveSettingGroupMoreList.get(3).getLiveSettingItems().get(getItemIndex(3)).getItemName());
        liveSettingGroupMoreList.get(4).setVal(liveSettingGroupMoreList.get(4).getLiveSettingItems().get(getItemIndex(4)).getItemName());


    }

    public void changeVal(int group, int pos) {
        String key = "setting";
        switch (group) {
            case 0:
                key = HawkConfig.LIVE_SHOW_TIME;
                break;
            case 1:
                key = HawkConfig.LIVE_SHOW_SPEED;
                break;
            case 2:
                key = HawkConfig.LIVE_SHOW_EPG;
                break;
            case 3:
                key = HawkConfig.LIVE_UI_SHOW_TIME;
                break;
            case 4:
                key = HawkConfig.LIVE_CONNECT_TIMEOUT;
                break;
        }
        Integer type = liveSettingGroupMoreList.get(group).getType();
        switch (type) {
            case 1:
                Boolean select = !Hawk.get(key, false);
                Hawk.put(key, select);
                liveSettingGroupMoreList.get(group).setSelect(select);
                break;
            case 2:
                if (key.equals(HawkConfig.LIVE_UI_SHOW_TIME)) {
                    Hawk.put(key, uiTimeList.get(pos));
                    App.LIVE_UI_SHOW_TIME = uiTimeList.get(pos) * 1000;
                } else if (key.equals(HawkConfig.LIVE_CONNECT_TIMEOUT)) {
                    Hawk.put(key, canTimeList.get(pos));
                    App.LIVE_CONNECT_TIMEOUT = canTimeList.get(pos) * 1000;
                }
                break;
            case 3:
                break;
        }

    }

    public int getItemIndex(int pos) {
        int val = -1;
        switch (pos) {
            case 3:
                val = Hawk.get(HawkConfig.LIVE_UI_SHOW_TIME, 5);
                for (int i = 0; i < uiTimeList.size(); i++) {
                    if (uiTimeList.get(i).equals(val)) {
                        return i;
                    }
                }
                break;
            case 4:
                val = Hawk.get(HawkConfig.LIVE_CONNECT_TIMEOUT, 5);
                for (int i = 0; i < canTimeList.size(); i++) {
                    if (canTimeList.get(i).equals(val)) {
                        return i;
                    }
                }
                break;
        }
        return -1;
    }

    @Override
    public void init(JsonElement jsonElement, AppConfig.LoadCallback callback) {
        initLiveSettingGroupList();
        initLiveSettingGroupMoreList();
        callback.success();
    }

    public void reSet() {
        initLiveSettingGroupList();
        initLiveSettingGroupMoreList();
    }
}
