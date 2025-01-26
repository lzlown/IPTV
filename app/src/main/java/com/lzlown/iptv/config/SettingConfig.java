package com.lzlown.iptv.config;

import com.google.gson.JsonElement;
import com.lzlown.iptv.base.App;
import com.lzlown.iptv.bean.LiveSettingGroup;
import com.lzlown.iptv.bean.LiveSettingItem;
import com.lzlown.iptv.util.HawkConfig;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingConfig implements Config {
    private static final String TAG = "SettingConfig";
    private static final SettingConfig instance = new SettingConfig();
    private final List<LiveSettingGroup> liveSettingGroupList = new ArrayList<>();
    private final List<LiveSettingGroup> liveSettingGroupMoreList = new ArrayList<>();

    public static final int BUTTON=0;
    public static final int SWITCH=1;
    public static final int SELECT=2;



    private SettingConfig() {

    }

    public static SettingConfig get() {
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
        itemsArrayList.add(epgItems);
        itemsArrayList.add(sourceItems);
        itemsArrayList.add(scaleItems);
        itemsArrayList.add(epgItems);
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
    }

    //右侧设置列表
    private void initLiveSettingGroupMoreList() {
        ArrayList<ArrayList<String>> itemsArrayList = new ArrayList<>();
        ArrayList<String> groupNames = new ArrayList<>(Arrays.asList("显示时间", "显示网速", "显示预告", "超时关闭界面", "超时更换频道","重载节目列表","重载节目预告", "恢复默认设置"));
        ArrayList<String> epgItems = new ArrayList<>();
        itemsArrayList.add(epgItems);
        itemsArrayList.add(epgItems);
        itemsArrayList.add(epgItems);
        itemsArrayList.add(new ArrayList<>(Arrays.asList("5秒&5", "10秒&10", "15秒&15", "30秒&30", "60秒&60")));
        itemsArrayList.add(new ArrayList<>(Arrays.asList("5秒&5", "10秒&10", "15秒&15", "30秒&30", "60秒&60")));
//        itemsArrayList.add(new ArrayList<>(Arrays.asList("默认&0","奈飞&1", "哆啦&2", "樱花&3")));
        itemsArrayList.add(epgItems);
        itemsArrayList.add(epgItems);
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


        liveSettingGroupMoreList.get(0).setType(SWITCH);
        liveSettingGroupMoreList.get(1).setType(SWITCH);
        liveSettingGroupMoreList.get(2).setType(SWITCH);
        liveSettingGroupMoreList.get(3).setType(SELECT);
        liveSettingGroupMoreList.get(4).setType(SELECT);
//        liveSettingGroupMoreList.get(5).setType(SELECT);

        liveSettingGroupMoreList.get(0).setSelect(Hawk.get(HawkConfig.LIVE_SHOW_TIME, false));
        liveSettingGroupMoreList.get(1).setSelect(Hawk.get(HawkConfig.LIVE_SHOW_SPEED, false));
        liveSettingGroupMoreList.get(2).setSelect(Hawk.get(HawkConfig.LIVE_SHOW_EPG, false));
        liveSettingGroupMoreList.get(3).setVal(getSettingItemName(3, getSelectItemIndex(3)));
        liveSettingGroupMoreList.get(4).setVal(getSettingItemName(4, getSelectItemIndex(4)));
//        liveSettingGroupMoreList.get(5).setVal(getSettingItemName(5, getSelectItemIndex(5)));
    }

    private String getSettingItemCacheKey(int itemIndex) {
        switch (itemIndex) {
            case 0:
                return HawkConfig.LIVE_SHOW_TIME;
            case 1:
                return HawkConfig.LIVE_SHOW_SPEED;
            case 2:
                return HawkConfig.LIVE_SHOW_EPG;
            case 3:
                return HawkConfig.LIVE_UI_SHOW_TIME;
            case 4:
                return HawkConfig.LIVE_CONNECT_TIMEOUT;
            case 5:
                return HawkConfig.THEME_SELECT;
        }
        return "";
    }

    private String getSettingItemVal(int groupIndex, int itemIndex) {
        LiveSettingGroup liveSettingGroup = liveSettingGroupMoreList.get(groupIndex);
        String itemName = liveSettingGroup.getLiveSettingItems().get(itemIndex).getItemName();
        String[] split = itemName.split("&");
        return split.length>1?split[1]:split[0];
    }

    public String getSettingItemName(int groupIndex, int itemIndex) {
        LiveSettingGroup liveSettingGroup = liveSettingGroupMoreList.get(groupIndex);
        String itemName = liveSettingGroup.getLiveSettingItems().get(itemIndex).getItemName();
        String[] split = itemName.split("&");
        return split[0];
    }

    public void selectSettingItemVal(int groupIndex, int itemIndex) {
        String key = getSettingItemCacheKey(groupIndex);
        if (key.isEmpty()) return;
        Integer type = liveSettingGroupMoreList.get(groupIndex).getType();
        switch (type) {
            //按钮
            case 0:
                break;
            //开关 Switch
            case 1:
                Boolean select = !Hawk.get(key, false);
                Hawk.put(key, select);
                liveSettingGroupMoreList.get(groupIndex).setSelect(select);
                break;
            //选择 选择子项
            case 2:
                String settingItemVal = getSettingItemVal(groupIndex, itemIndex);
                int val = Integer.parseInt(settingItemVal);
                Hawk.put(key, val);
                if (key.equals(HawkConfig.LIVE_UI_SHOW_TIME)) {
                    App.LIVE_UI_SHOW_TIME = val * 1000;
                } else if (key.equals(HawkConfig.LIVE_CONNECT_TIMEOUT)) {
                    App.LIVE_CONNECT_TIMEOUT = val * 1000;
                }
                break;
        }

    }

    public int getSelectItemIndex(int groupIndex) {
        String key = getSettingItemCacheKey(groupIndex);
        if (key.isEmpty()) return -1;
        int val = Hawk.get(key, 0);
        ArrayList<LiveSettingItem> liveSettingItems = liveSettingGroupMoreList.get(groupIndex).getLiveSettingItems();
        for (int i = 0; i <liveSettingItems.size(); i++) {
            if (liveSettingItems.get(i).getItemName().split("&")[1].equals(String.valueOf(val))) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void init(JsonElement jsonElement, AppConfig.LoadCallback callback) {
        initLiveSettingGroupList();
        initLiveSettingGroupMoreList();
        callback.success();
    }
}
