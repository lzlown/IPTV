package com.lzlown.iptv.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.lzlown.iptv.bean.*;
import com.lzlown.iptv.util.HawkConfig;
import com.lzlown.iptv.util.TimeUtil;
import com.lzlown.iptv.util.live.TxtSubscribe;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;
import com.orhanobut.hawk.Hawk;

import java.util.*;

public class ApiConfig {
    private static ApiConfig instance;
    private List<LiveChannelGroup> liveChannelGroupList = new ArrayList<>();
    private static final Map<String, Map<String, LiveEpg>> liveEpgMap = new HashMap();
    private HashMap<String, List<IjkOption>> ijkOptions = new HashMap<>();
    private HashMap<String, List<String>> vlcOptions = new HashMap<>();
    private final String userAgent = "okhttp/3.15";
    private final String requestAccept = "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9";
//    private ExecutorService executorService= Executors.newFixedThreadPool(5);

    //EPG 地址
    private String epgAllUrl;
    //直播源 地址
    private String liveUrl;

    private ApiConfig() {

    }

    //HTTP请求的回调
    public interface LoadCallback {
        void success();

        void error(String msg);
    }

    private List<IjkOption> defaultIJK() {
        List<IjkOption> list = new ArrayList<>();
        list.add(new IjkOption(4, "opensles", "0"));
        list.add(new IjkOption(4, "framedrop", "5"));
        list.add(new IjkOption(4, "start-on-prepared", "1"));
        list.add(new IjkOption(1, "http-detect-rangeupport", "0"));
        list.add(new IjkOption(2, "skip_loop_filter", "0"));
        list.add(new IjkOption(4, "reconnect", "5"));
        list.add(new IjkOption(4, "fast", "1"));

        list.add(new IjkOption(4, "mediacodec", "1"));
        list.add(new IjkOption(4, "mediacodec-all-videos", "1"));
        list.add(new IjkOption(4, "mediacodec-auto-rotate", "1"));
        list.add(new IjkOption(4, "mediacodec-handle-resolution-change", "1"));
        list.add(new IjkOption(4, "mediacodec-hevc", "1"));
        list.add(new IjkOption(4, "mediacodec-avc", "1"));
        return list;
    }

    private List<String> defaultVLC() {
        List<String> list = new ArrayList<>();
        list.add("network-caching=300");
        list.add("live-caching=200");
        list.add("file-caching=200");
        list.add("clock-jitter=5000");
        list.add("avcodec-hw=any");
        return list;
    }

    public static ApiConfig get() {
        if (instance == null) {
            synchronized (ApiConfig.class) {
                if (instance == null) {
                    instance = new ApiConfig();
                }
            }
        }
        return instance;
    }

    public HashMap<String, List<IjkOption>> getIjkOptions() {
        return ijkOptions;
    }

    public HashMap<String, List<String>> getVlcOptions() {
        return vlcOptions;
    }

    public List<LiveChannelGroup> getChannelGroupList() {
        return liveChannelGroupList;
    }

    private void loadIjkOptions(JsonElement jsonElement) {
        try {
            JsonArray ijk_options = jsonElement.getAsJsonObject().getAsJsonArray("ijk");
            for (JsonElement option : ijk_options) {
                String group = option.getAsJsonObject().get("group").getAsString();
                List<IjkOption> optionList = ApiConfig.this.ijkOptions.get(group);
                if (null == optionList) {
                    optionList = new ArrayList<>();
                }
                JsonArray rules = option.getAsJsonObject().getAsJsonArray("options");
                for (JsonElement item : rules) {
                    int category = item.getAsJsonObject().get("category").getAsInt();
                    String name = item.getAsJsonObject().get("name").getAsString();
                    String value = item.getAsJsonObject().get("value").getAsString();
                    IjkOption ijkOption = new IjkOption(category, name, value);
                    optionList.add(ijkOption);
                }
                ApiConfig.this.ijkOptions.put(group, optionList);
            }
        } catch (Exception ignored) {
        }

    }

    private void loadVlcOptions(JsonElement jsonElement) {
        try {
            JsonArray vlc_options = jsonElement.getAsJsonObject().getAsJsonArray("vlc");
            for (JsonElement option : vlc_options) {
                String group = option.getAsJsonObject().get("group").getAsString();
                List<String> optionList = ApiConfig.this.vlcOptions.get(group);
                if (null == optionList) {
                    optionList = new ArrayList<>();
                }
                String options = option.getAsJsonObject().get("options").getAsString();
                String[] split = options.split(",");
                optionList.addAll(Arrays.asList(split));
                ApiConfig.this.vlcOptions.put(group, optionList);
            }
        } catch (Exception e) {
        }

    }

    //获取配置文件
    private void getCfg(LoadCallback callback) {
        OkGo.<String>get(Hawk.get(HawkConfig.API_URL))
                .headers("User-Agent", userAgent)
                .headers("Accept", requestAccept)
                .execute(new AbsCallback<String>() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        ijkOptions = new HashMap<>();
                        ijkOptions.put("default", defaultIJK());
                        vlcOptions = new HashMap<>();
                        vlcOptions.put("default", defaultVLC());
                        try {
                            JsonElement jsonElement = JsonParser.parseString(response.getRawResponse().body().string());
                            liveUrl = jsonElement.getAsJsonObject().get("live").getAsString();
                            epgAllUrl = jsonElement.getAsJsonObject().get("epgAll").getAsString();
                            loadIjkOptions(jsonElement);
                            loadVlcOptions(jsonElement);
                            callback.success();
                        } catch (Exception e) {
                            e.printStackTrace();
                            callback.error("配置文件解析失败");
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        callback.error("配置文件获取失败");
                    }

                    public String convertResponse(okhttp3.Response response) throws Throwable {
                        return "";
                    }
                });
    }

    //获取直播源
    private void getLive(String liveUrl, LoadCallback callback) {
        OkGo.<String>get(liveUrl)
                .headers("User-Agent", userAgent)
                .headers("Accept", requestAccept)
                .execute(new AbsCallback<String>() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        liveChannelGroupList = new ArrayList<>();
                        try {
                            LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>> tvMap = new LinkedHashMap<>();
                            TxtSubscribe.parse(tvMap, response.getRawResponse().body().string());
                            Integer sum = 0;
                            for (Map.Entry entry : tvMap.entrySet()) {
                                LinkedHashMap<String, ArrayList<String>> item = (LinkedHashMap<String, ArrayList<String>>) entry.getValue();
                                LiveChannelGroup liveChannelGroup = new LiveChannelGroup();
                                liveChannelGroup.setGroupIndex(liveChannelGroupList.size());
                                liveChannelGroup.setGroupName(entry.getKey().toString());
                                ArrayList<LiveChannelItem> liveChannelItems = new ArrayList<>();
                                for (Map.Entry entry2 : item.entrySet()) {
                                    sum++;
                                    LiveChannelItem liveChannelItem = new LiveChannelItem();
                                    String[] split = entry2.getKey().toString().split("&");
                                    liveChannelItem.setChannelName(split[0]);
                                    if (split.length > 1) {
                                        liveChannelItem.setChannelCh(split[1]);
                                    }
                                    liveChannelItem.setChannelIndex(liveChannelItems.size());
                                    liveChannelItem.setChannelNum(sum);
                                    liveChannelItem.setChannelUrls((ArrayList<String>) entry2.getValue());
                                    ArrayList<String> strings2 = new ArrayList<>();
                                    for (int i = 0; i < ((ArrayList<?>) entry2.getValue()).size(); i++) {
                                        strings2.add("源" + i);
                                    }
                                    liveChannelItem.setChannelSourceNames(strings2);
                                    liveChannelItems.add(liveChannelItem);
                                }
                                liveChannelGroup.setLiveChannels(liveChannelItems);
                                liveChannelGroupList.add(liveChannelGroup);
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

    //获取EPG
    private void getEpgAll(LoadCallback callback, String time) {
        Set<String> epgKeys = liveEpgMap.keySet();
        List<String> dates = Arrays.asList(TimeUtil.getTimeBef(), TimeUtil.getTime(), TimeUtil.getTimeNext());
        for (String key : epgKeys) {
            if (!dates.contains(key)) {
                liveEpgMap.remove(key);
            }
        }
        Map<String, LiveEpg> epgMap = new HashMap<>();
        liveEpgMap.put(time, epgMap);
        OkGo.<String>get(epgAllUrl)
                .headers("User-Agent", userAgent)
                .headers("Accept", requestAccept)
                .execute(new AbsCallback<String>() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JsonElement jsonElement = JsonParser.parseString(response.getRawResponse().body().string());
                            JsonArray jsonArray = jsonElement.getAsJsonObject().getAsJsonArray(time);
                            for (JsonElement element : jsonArray) {
                                String cc = element.getAsJsonObject().get("cc").getAsString();
                                JsonArray asJsonArray = element.getAsJsonObject().get("epg").getAsJsonArray();
                                LiveEpg liveEpg = new LiveEpg();
                                liveEpg.setName(cc);
                                ArrayList<LiveEpgItem> liveEpgItems = new ArrayList<>();
                                for (JsonElement obj : asJsonArray) {
                                    String start = obj.getAsJsonObject().get("start").getAsString();
                                    String end = obj.getAsJsonObject().get("end").getAsString();
                                    String title = obj.getAsJsonObject().get("title").getAsString();
                                    LiveEpgItem liveEpgItem = new LiveEpgItem(start, end, title);
                                    liveEpgItems.add(liveEpgItem);
                                }
                                liveEpg.setEpgItems(liveEpgItems);
                                epgMap.put(cc, liveEpg);
                            }
                            callback.success();
                        } catch (Exception e) {
                            e.printStackTrace();
                            callback.error("EPG解析失败");
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        callback.error("EPG获取失败");
                    }

                    public String convertResponse(okhttp3.Response response) throws Throwable {
                        return "";
                    }
                });
    }

    //左列表使用
    public LiveEpgItem getLiveEpgItem(String key) {
        LiveEpg liveEpg = getLiveEpg(key, TimeUtil.getTime());
        if (null != liveEpg) {
            try {
                List<LiveEpgItem> arrayList = liveEpg.getEpgItems();
                if (arrayList != null && !arrayList.isEmpty()) {
                    int size = arrayList.size() - 1;
                    while (size >= 0) {
                        if (new Date().compareTo(((LiveEpgItem) arrayList.get(size)).startdateTime) >= 0) {
                            return arrayList.get(size);
                        } else {
                            size--;
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return new LiveEpgItem("", "", "暂无预告");
    }

    //中间使用
    public Map<String, LiveEpgItem> getLiveEpgItemForMap(String key) {
        Map<String, LiveEpgItem> map = new HashMap<>();
        LiveEpg liveEpg = getLiveEpg(key, TimeUtil.getTime());
        if (null != liveEpg) {
            List<LiveEpgItem> arrayList = liveEpg.getEpgItems();
            if (arrayList != null && !arrayList.isEmpty()) {
                for (int i = 0; i < arrayList.size(); i++) {
                    if (new Date().compareTo(((LiveEpgItem) arrayList.get(i)).startdateTime) >= 0) {
                        map.put("c", arrayList.get(i));
                        if (i < arrayList.size() - 1) {
                            map.put("n", arrayList.get(i + 1));
                        } else {
                            map.put("n", getLiveEpgItemNext(key));
                        }
                    }
                }
            }
        }
        return map;
    }

    private LiveEpgItem getLiveEpgItemNext(String key) {
        LiveEpg liveEpg = getLiveEpg(key, TimeUtil.getTimeNext());
        if (null != liveEpg) {
            List<LiveEpgItem> arrayList = liveEpg.getEpgItems();
            if (arrayList != null && !arrayList.isEmpty()) {
                return arrayList.get(0);
            }
        }
        return new LiveEpgItem("", "", "暂无预告");
    }

    private LiveEpg getLiveEpg(String key, String time) {
        if (!liveEpgMap.containsKey(time)) {
            synchronized (this) {
                if (!liveEpgMap.containsKey(time)) {
                    getEpgAll(new LoadCallback() {
                        @Override
                        public void success() {

                        }

                        @Override
                        public void error(String msg) {
                            liveEpgMap.put(time, new HashMap<>());
                        }
                    }, time);
                }
            }
        }
        Map<String, LiveEpg> epgMap = liveEpgMap.get(time);
        if (null != epgMap) {
            LiveEpg liveEpg = epgMap.get(key);
            if (null != liveEpg) {
                return liveEpg;
            }
        }
        return null;
    }

    //初始化 逻辑
    public void loadData(LoadCallback callback) {
        getCfg(new LoadCallback() {
            @Override
            public void success() {
                getLive(liveUrl, new LoadCallback() {
                    @Override
                    public void success() {
                        if (Hawk.get(HawkConfig.LIVE_SHOW_EPG, false)) {
                            getEpgAll(new LoadCallback() {
                                @Override
                                public void success() {
                                    callback.success();
                                }

                                @Override
                                public void error(String msg) {
                                    callback.success();
                                }
                            }, TimeUtil.getTime());
                        } else {
                            callback.success();
                        }
                    }

                    @Override
                    public void error(String msg) {
                        callback.error(msg);
                    }
                });
            }

            @Override
            public void error(String msg) {
                callback.error(msg);
            }
        });
    }
}
