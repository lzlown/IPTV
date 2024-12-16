package com.lzlown.iptv.config;

import com.google.gson.JsonElement;
import com.lzlown.iptv.bean.LiveSettingGroup;
import com.lzlown.iptv.bean.LiveSettingItem;
import com.lzlown.iptv.util.HawkConfig;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingConfig implements Config {
    private static volatile SettingConfig instance;
    private final List<LiveSettingGroup> liveSettingGroupList = new ArrayList<>();

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

    //右侧设置列表
    private void initLiveSettingGroupList() {
        ArrayList<ArrayList<String>> itemsArrayList = new ArrayList<>();
        ArrayList<String> groupNames = new ArrayList<>(Arrays.asList("画质线路", "画面比例", "偏好设置"));
        ArrayList<String> sourceItems = new ArrayList<>();
        ArrayList<String> scaleItems = new ArrayList<>(Arrays.asList("默认", "16:9", "4:3", "填充", "原始", "裁剪"));
        ArrayList<String> personalSettingItems = new ArrayList<>(Arrays.asList("显示时间", "显示网速", "显示预告", "清理缓存"));
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
        liveSettingGroupList.get(2).getLiveSettingItems().get(0).setItemSelected(Hawk.get(HawkConfig.LIVE_SHOW_TIME, false));
        liveSettingGroupList.get(2).getLiveSettingItems().get(1).setItemSelected(Hawk.get(HawkConfig.LIVE_SHOW_SPEED, false));
        liveSettingGroupList.get(2).getLiveSettingItems().get(2).setItemSelected(Hawk.get(HawkConfig.LIVE_SHOW_EPG, false));
    }

    @Override
    public void init(JsonElement jsonElement, AppConfig.LoadCallback callback) {
        initLiveSettingGroupList();
        callback.success();
    }

    public void reSet(){
        initLiveSettingGroupList();
    }
}
