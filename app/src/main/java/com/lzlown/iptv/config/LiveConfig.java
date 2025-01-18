package com.lzlown.iptv.config;

import com.google.gson.JsonElement;
import com.lzlown.iptv.bean.LiveChannelGroup;
import com.lzlown.iptv.bean.LiveChannelItem;
import com.lzlown.iptv.bean.LiveChannelItemSource;
import com.lzlown.iptv.util.HawkConfig;
import com.lzlown.iptv.util.live.TxtSubscribe;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LiveConfig implements Config {
    private static final LiveConfig instance = new LiveConfig();
    private String liveUrl="";
    private final List<LiveChannelGroup> liveChannelGroupList = new ArrayList<>();
    private final List<LiveChannelItem> liveChannelList = new ArrayList<>();
    private LiveChannelItem currentLiveChannelItem = null;

    private LiveConfig() {

    }

    public static LiveConfig get() {
        return instance;
    }

    public List<LiveChannelGroup> getLiveChannelGroupList() {
        return liveChannelGroupList;
    }

    public List<LiveChannelItem> getLiveChannelList() {
        return liveChannelList;
    }

    public LiveChannelItem getCurrentLiveChannelItem() {
        return currentLiveChannelItem;
    }

    public void setCurrentLiveChannelItem(LiveChannelItem currentLiveChannelItem) {
        this.currentLiveChannelItem = currentLiveChannelItem;
    }

    @Override
    public void init(JsonElement jsonElement, AppConfig.LoadCallback callback) {
        liveUrl = jsonElement.getAsJsonObject().get("live").getAsString();
        AppConfig.get().getOkGo(liveUrl).execute(new AbsCallback<String>() {
            @Override
            public void onSuccess(Response<String> response) {
                liveChannelGroupList.clear();
                try {
                    LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>> tvMap = new LinkedHashMap<>();
                    TxtSubscribe.parse(tvMap, response.getRawResponse().body().string());
                    int sum = 0;
                    int groupIndex = -1;
                    for (Map.Entry entry : tvMap.entrySet()) {
                        LinkedHashMap<String, ArrayList<String>> item = (LinkedHashMap<String, ArrayList<String>>) entry.getValue();
                        LiveChannelGroup liveChannelGroup = new LiveChannelGroup();
                        liveChannelGroup.setGroupIndex(liveChannelGroupList.size());
                        liveChannelGroup.setGroupName(entry.getKey().toString());
                        groupIndex++;
                        ArrayList<LiveChannelItem> liveChannelItems = new ArrayList<>();
                        for (Map.Entry entry2 : item.entrySet()) {
                            sum++;
                            LiveChannelItem liveChannelItem = new LiveChannelItem();
                            liveChannelItem.setChannelGroupIndex(groupIndex);
                            liveChannelItem.setChannelIndex(liveChannelItems.size());
                            liveChannelItem.setChannelNum(sum);
                            liveChannelItem.setChannelName(entry2.getKey().toString());
                            List<LiveChannelItemSource> sources = new ArrayList<>();
                            for (int i = 0; i < ((ArrayList<?>) entry2.getValue()).size(); i++) {
                                LiveChannelItemSource liveChannelItemSource = new LiveChannelItemSource();
                                String url = ((ArrayList<?>) entry2.getValue()).get(i).toString();
                                String[] split = url.split("#");
                                liveChannelItemSource.setUrl(split[1]);
                                String[] splitsoc = split[0].split("&");
                                if (splitsoc.length > 2) {
                                    liveChannelItemSource.setBackUrl(splitsoc[2]);
                                }
                                if (splitsoc.length > 1) {
                                    liveChannelItemSource.setCc(splitsoc[0]);
                                    liveChannelItemSource.setName(splitsoc[1]);
                                } else {
                                    liveChannelItemSource.setName(splitsoc[0]);
                                }
                                sources.add(liveChannelItemSource);
                            }
                            liveChannelItem.setLiveChannelItemSources(sources);
                            liveChannelItems.add(liveChannelItem);
                        }
                        liveChannelGroup.setLiveChannels(liveChannelItems);
                        liveChannelGroupList.add(liveChannelGroup);
                    }
                    liveChannelList.clear();
                    for (LiveChannelGroup liveChannelGroup : liveChannelGroupList) {
                        liveChannelList.addAll(liveChannelGroup.getLiveChannels());
                    }
                    callback.success();
                } catch (Throwable th) {
                    th.printStackTrace();
                    callback.error("直播源解析失败");
                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                callback.error("直播源获取失败");
            }

            public String convertResponse(okhttp3.Response response) throws Throwable {
                return "";
            }
        });
    }

    public ArrayList<LiveChannelItem> getLiveChannels(int groupIndex) {
        return LiveConfig.get().getLiveChannelGroupList().get(groupIndex).getLiveChannels();
    }

    public Integer[] getNextChannel(int direction) {
        int channelGroupIndex = currentLiveChannelItem.getChannelGroupIndex();
        int liveChannelIndex = currentLiveChannelItem.getChannelIndex();
        if (direction > 0) {
            liveChannelIndex++;
            if (liveChannelIndex >= getLiveChannels(channelGroupIndex).size()) {
                liveChannelIndex = 0;
                if (Hawk.get(HawkConfig.LIVE_CROSS_GROUP, true)) {
                    do {
                        channelGroupIndex++;
                        if (channelGroupIndex >= LiveConfig.get().getLiveChannelGroupList().size()) {
                            channelGroupIndex = 0;
                        }
                    } while (channelGroupIndex == currentLiveChannelItem.getChannelGroupIndex() && channelGroupIndex != 0);
                }
            }
        } else {
            liveChannelIndex--;
            if (liveChannelIndex < 0) {
                if (Hawk.get(HawkConfig.LIVE_CROSS_GROUP, true)) {
                    do {
                        channelGroupIndex--;
                        if (channelGroupIndex < 0)
                            channelGroupIndex = LiveConfig.get().getLiveChannelGroupList().size() - 1;
                    } while (channelGroupIndex == currentLiveChannelItem.getChannelGroupIndex());
                }
                liveChannelIndex = getLiveChannels(channelGroupIndex).size() - 1;
            }
        }
        Integer[] groupChannelIndex = new Integer[2];
        groupChannelIndex[0] = channelGroupIndex;
        groupChannelIndex[1] = liveChannelIndex;
        return groupChannelIndex;
    }

    public void init() {
        currentLiveChannelItem = null;
    }

    public void reLoad() {
        AppConfig.get().getOkGo(liveUrl).execute(new AbsCallback<String>() {
            @Override
            public void onSuccess(Response<String> response) {
                liveChannelGroupList.clear();
                try {
                    LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>> tvMap = new LinkedHashMap<>();
                    TxtSubscribe.parse(tvMap, response.getRawResponse().body().string());
                    int sum = 0;
                    int groupIndex = -1;
                    for (Map.Entry entry : tvMap.entrySet()) {
                        LinkedHashMap<String, ArrayList<String>> item = (LinkedHashMap<String, ArrayList<String>>) entry.getValue();
                        LiveChannelGroup liveChannelGroup = new LiveChannelGroup();
                        liveChannelGroup.setGroupIndex(liveChannelGroupList.size());
                        liveChannelGroup.setGroupName(entry.getKey().toString());
                        groupIndex++;
                        ArrayList<LiveChannelItem> liveChannelItems = new ArrayList<>();
                        for (Map.Entry entry2 : item.entrySet()) {
                            sum++;
                            LiveChannelItem liveChannelItem = new LiveChannelItem();
                            liveChannelItem.setChannelGroupIndex(groupIndex);
                            liveChannelItem.setChannelIndex(liveChannelItems.size());
                            liveChannelItem.setChannelNum(sum);
                            liveChannelItem.setChannelName(entry2.getKey().toString());
                            List<LiveChannelItemSource> sources = new ArrayList<>();
                            for (int i = 0; i < ((ArrayList<?>) entry2.getValue()).size(); i++) {
                                LiveChannelItemSource liveChannelItemSource = new LiveChannelItemSource();
                                String url = ((ArrayList<?>) entry2.getValue()).get(i).toString();
                                String[] split = url.split("#");
                                liveChannelItemSource.setUrl(split[1]);
                                String[] splitsoc = split[0].split("&");
                                if (splitsoc.length > 2) {
                                    liveChannelItemSource.setBackUrl(splitsoc[2]);
                                }
                                if (splitsoc.length > 1) {
                                    liveChannelItemSource.setCc(splitsoc[0]);
                                    liveChannelItemSource.setName(splitsoc[1]);
                                } else {
                                    liveChannelItemSource.setName(splitsoc[0]);
                                }
                                sources.add(liveChannelItemSource);
                            }
                            liveChannelItem.setLiveChannelItemSources(sources);
                            liveChannelItems.add(liveChannelItem);
                        }
                        liveChannelGroup.setLiveChannels(liveChannelItems);
                        liveChannelGroupList.add(liveChannelGroup);
                    }
                    liveChannelList.clear();
                    for (LiveChannelGroup liveChannelGroup : liveChannelGroupList) {
                        liveChannelList.addAll(liveChannelGroup.getLiveChannels());
                    }
                } catch (Throwable ignored) {

                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
            }

            public String convertResponse(okhttp3.Response response) throws Throwable {
                return "";
            }
        });
    }
}
