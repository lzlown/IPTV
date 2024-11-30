package com.lzlown.iptv.config;

import com.google.gson.JsonElement;
import com.lzlown.iptv.bean.LiveChannelGroup;
import com.lzlown.iptv.bean.LiveChannelItem;
import com.lzlown.iptv.bean.LiveChannelItemSource;
import com.lzlown.iptv.util.live.TxtSubscribe;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LiveConfig implements Config {
    private static volatile LiveConfig instance;
    private List<LiveChannelGroup> liveChannelGroupList = new ArrayList<>();
    private List<LiveChannelItem> liveChannelList = new ArrayList<>();

    private LiveConfig() {

    }

    public static LiveConfig get() {
        if (instance == null) {
            synchronized (LiveConfig.class) {
                if (instance == null) {
                    instance = new LiveConfig();
                }
            }
        }
        return instance;
    }

    public List<LiveChannelGroup> getLiveChannelGroupList() {
        return liveChannelGroupList;
    }

    public List<LiveChannelItem> getLiveChannelList() {
        return liveChannelList;
    }

    @Override
    public void init(JsonElement jsonElement, AppConfig.LoadCallback callback) {
        String live = jsonElement.getAsJsonObject().get("live").getAsString();
        AppConfig.get().getOkGo(live).execute(new AbsCallback<String>() {
            @Override
            public void onSuccess(Response<String> response) {
                liveChannelGroupList = new ArrayList<>();
                try {
                    LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>> tvMap = new LinkedHashMap<>();
                    TxtSubscribe.parse(tvMap, response.getRawResponse().body().string());
                    int sum = 0;
                    for (Map.Entry entry : tvMap.entrySet()) {
                        LinkedHashMap<String, ArrayList<String>> item = (LinkedHashMap<String, ArrayList<String>>) entry.getValue();
                        LiveChannelGroup liveChannelGroup = new LiveChannelGroup();
                        liveChannelGroup.setGroupIndex(liveChannelGroupList.size());
                        liveChannelGroup.setGroupName(entry.getKey().toString());
                        ArrayList<LiveChannelItem> liveChannelItems = new ArrayList<>();
                        for (Map.Entry entry2 : item.entrySet()) {
                            sum++;
                            LiveChannelItem liveChannelItem = new LiveChannelItem();
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
}
