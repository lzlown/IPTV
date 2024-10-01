package com.lzlown.iptv.api;

import android.util.Log;
//import com.alibaba.fastjson2.JSONArray;
//import com.alibaba.fastjson2.JSONObject;
import com.google.gson.*;
import com.lzlown.iptv.base.App;
import com.lzlown.iptv.bean.*;
import com.lzlown.iptv.util.HawkConfig;
import com.lzlown.iptv.util.StringUtils;
import com.lzlown.iptv.util.TimeUtil;
import com.lzlown.iptv.util.live.TxtSubscribe;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.GetRequest;
import com.orhanobut.hawk.GsonParser;
import com.orhanobut.hawk.Hawk;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class ApiConfig {
    private static ApiConfig instance;
    private List<LiveChannelGroup> liveChannelGroupList = new ArrayList<>();
    private List<LiveChannelItem> liveChannelList = new ArrayList<>();
    private final Map<String, Map<String, LiveEpg>> liveEpgMap = new HashMap<>();
    private final Map<String, Map<String, LiveEpg>> defaultliveEpgMap = new HashMap<>();
    private final HashMap<String, List<IjkOption>> ijkOptions = new HashMap<>();
    private String date;
    private final List<LiveEpgDate> epgDateList = new ArrayList<>();
    private final LiveEpgItem defaultLiveEpgItem = new LiveEpgItem(TimeUtil.getTime(), "00:00", "23:59", "暂无预告", 0);

    //EPG 地址
    private String epgUrl;
    //直播源 地址
    private String liveUrl;

    private ApiConfig() {
    }

    private GetRequest<String> getOkGo(String url) {
        return OkGo.<String>get(url)
                .headers("User-Agent", App.userAgent)
                .headers("Accept", App.requestAccept)
                .headers(App.auth_key, App.auth_value);
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
        list.add(new IjkOption(1, "fflags", "fastseek"));
        list.add(new IjkOption(4, "enable-accurate-seek", "1"));


        list.add(new IjkOption(4, "mediacodec", "1"));
        list.add(new IjkOption(4, "mediacodec-all-videos", "1"));
        list.add(new IjkOption(4, "mediacodec-auto-rotate", "1"));
        list.add(new IjkOption(4, "mediacodec-handle-resolution-change", "1"));
        return list;
    }

    private void initEpgDate() {
        epgDateList.clear();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        SimpleDateFormat datePresentFormat = new SimpleDateFormat("MM-dd");
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        for (int i = 0; i < 9; i++) {
            Date dateIns = calendar.getTime();
            LiveEpgDate epgDate = new LiveEpgDate();
            epgDate.setIndex(i);
            epgDate.setDatePresented(datePresentFormat.format(dateIns));
            epgDate.setDateParamVal(dateIns);
            epgDateList.add(epgDate);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
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

    public List<LiveChannelGroup> getChannelGroupList() {
        return liveChannelGroupList;
    }

    public List<LiveChannelItem> getLiveChannelList() {
        return liveChannelList;
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
        } catch (Exception e) {
        }

    }

    //获取配置文件
    private void getCfg(LoadCallback callback) {
        getOkGo(Hawk.get(HawkConfig.API_URL)).execute(new AbsCallback<String>() {
            @Override
            public void onSuccess(Response<String> response) {
                ijkOptions.put("default", defaultIJK());
                try {
                    JsonElement jsonElement = JsonParser.parseString(response.getRawResponse().body().string());
                    liveUrl = jsonElement.getAsJsonObject().get("live").getAsString();
                    epgUrl = jsonElement.getAsJsonObject().get("epg").getAsString();
                    loadIjkOptions(jsonElement);
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
        getOkGo(liveUrl).execute(new AbsCallback<String>() {
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

    //获取EPG
    private void getEpg(LoadCallback callback, String time) {
        GetRequest<String> okGo = getOkGo(epgUrl);
        if (StringUtils.isNotEmpty(time)) {
            liveEpgMap.put(time, new HashMap<>());
            okGo.params("date", time);
        }
        okGo.execute(new AbsCallback<String>() {
            @Override
            public void onSuccess(Response<String> response) {
                try {
                    JsonElement jsonElement = JsonParser.parseString(response.getRawResponse().body().string());
                    Set<String> keys = jsonElement.getAsJsonObject().keySet();
                    for (String key : keys) {
                        Map<String, LiveEpg> epgMap = new HashMap<>();
                        JsonArray asJsonArray = jsonElement.getAsJsonObject().getAsJsonArray(key);
                        for (int i = 0; i < asJsonArray.size(); i++) {
                            JsonObject asJsonObject = asJsonArray.get(i).getAsJsonObject();
                            String cc = asJsonObject.get("cc").getAsString();
                            JsonArray epgs = asJsonObject.getAsJsonArray("epg");
                            LiveEpg liveEpg = new LiveEpg();
                            liveEpg.setName(cc);
                            ArrayList<LiveEpgItem> liveEpgItems = new ArrayList<>();
                            int num = 0;
                            for (int i1 = 0; i1 < epgs.size(); i1++) {
                                JsonObject epg = epgs.get(i1).getAsJsonObject();
                                String start = epg.get("start").getAsString();
                                String end = epg.get("end").getAsString();
                                String title = epg.get("title").getAsString();
                                LiveEpgItem liveEpgItem = new LiveEpgItem(key, start, end, title, num);
                                liveEpgItems.add(liveEpgItem);
                                num++;
                            }
                            liveEpg.setEpgItems(liveEpgItems);
                            epgMap.put(cc, liveEpg);
                        }
                        liveEpgMap.put(key, epgMap);
                    }
                    callback.success();
                } catch (Exception e) {
                    callback.error("EPG解析失败");
                }
            }

            @Override
            public void onError(Response<String> response) {
                callback.error("EPG获取失败");
            }

            public String convertResponse(okhttp3.Response response) throws Throwable {
                return "";
            }
        });
    }

    private LiveEpg getLiveEpg(String key, String time) {
        if (!liveEpgMap.containsKey(time)) {
            synchronized (this) {
                if (!liveEpgMap.containsKey(time)) {
                    getEpg(new LoadCallback() {
                        @Override
                        public void success() {

                        }

                        @Override
                        public void error(String msg) {

                        }
                    }, time);
                }
            }
        }
        Map<String, LiveEpg> epgMap = liveEpgMap.get(time);
        if (null != epgMap) {
            return epgMap.get(key);
        }
        return null;
    }

    //左列表使用
    public LiveEpgItem getLiveEpgItem(LiveChannelItem item) {
        String channelCh = item.getChannelCh();
        if (StringUtils.isNotEmpty(channelCh)) {
            String time = TimeUtil.getTime();
            LiveEpg liveEpg = getLiveEpg(channelCh, time);
            if (liveEpg != null && liveEpg.getEpgItems() != null && !liveEpg.getEpgItems().isEmpty()) {
                List<LiveEpgItem> arrayList = liveEpg.getEpgItems();
                int size = arrayList.size() - 1;
                while (size >= 0) {
                    if (new Date().compareTo(TimeUtil.getEpgTime(time + ((LiveEpgItem) arrayList.get(size)).start)) >= 0) {
                        return arrayList.get(size);
                    } else {
                        size--;
                    }
                }
            }
        }
        return defaultLiveEpgItem;
    }

    //中间使用
    public Map<String, LiveEpgItem> getLiveEpgItemForMap(LiveChannelItem item) {
        String key = item.getChannelCh();
        Map<String, LiveEpgItem> map = new HashMap<>();
        map.put("c", defaultLiveEpgItem);
        map.put("n", defaultLiveEpgItem);
        if (StringUtils.isNotEmpty(key)) {
            String time = TimeUtil.getTime();
            LiveEpg liveEpg = getLiveEpg(key, TimeUtil.getTime());
            if (liveEpg != null && liveEpg.getEpgItems() != null && !liveEpg.getEpgItems().isEmpty()) {
                List<LiveEpgItem> arrayList = liveEpg.getEpgItems();
                for (int i = 0; i < arrayList.size(); i++) {
                    if (new Date().compareTo(TimeUtil.getEpgTime(time + ((LiveEpgItem) arrayList.get(i)).start)) >= 0) {
                        map.put("c", arrayList.get(i));
                        if (i < arrayList.size() - 1) {
                            map.put("n", arrayList.get(i + 1));
                        } else {
                            LiveEpg liveEpg1 = getLiveEpg(key, TimeUtil.getTime(1));
                            if (null != liveEpg1) {
                                List<LiveEpgItem> arrayList1 = liveEpg1.getEpgItems();
                                if (arrayList1 != null && !arrayList1.isEmpty()) {
                                    map.put("n", arrayList1.get(0));
                                }
                            }
                        }
                    }
                }
            }
        }
        return map;
    }

    //回放列表使用
    public LiveEpg getLiveEpg(LiveChannelItem item, String time) {
        String channelCh = item.getChannelCh();
        if (StringUtils.isNotEmpty(channelCh)) {
            LiveEpg liveEpg = getLiveEpg(channelCh, time);
            if (liveEpg != null && liveEpg.getEpgItems() != null && !liveEpg.getEpgItems().isEmpty()) {
                return liveEpg;
            }
        } else {
            if (StringUtils.isNotEmpty(item.getSocUrls())) {
                Map<String, LiveEpg> stringLiveEpgMap = defaultliveEpgMap.get(time);
                if (stringLiveEpgMap != null) {
                    LiveEpg liveEpg1 = stringLiveEpgMap.get(item.getChannelName());
                    if (liveEpg1 != null && liveEpg1.getEpgItems() != null && !liveEpg1.getEpgItems().isEmpty()) {
                        return liveEpg1;
                    }
                }
                stringLiveEpgMap = new HashMap<>();
                List<LiveEpgItem> epgItems = new ArrayList<>();
                for (int i = 0; i < 23; i++) {
                    String start = String.format("%02d:00", i);
                    String end = String.format("%02d:00", i + 1);
                    epgItems.add(new LiveEpgItem(date, start, end, item.getChannelName(), i));
                }
                epgItems.add(new LiveEpgItem(date, "23:00", "23:59", item.getChannelName(), 23));
                LiveEpg liveEpg = new LiveEpg();
                liveEpg.setName(item.getChannelName());
                liveEpg.setEpgItems(epgItems);
                stringLiveEpgMap.put(item.getChannelName(), liveEpg);
                defaultliveEpgMap.put(time, stringLiveEpgMap);
                return liveEpg;
            }
        }
        return null;
    }

    //EPG日期
    public List<LiveEpgDate> getEpgDateList() {
        if (!TimeUtil.getTime().equals(date)) {
            initEpgDate();
            date = TimeUtil.getTime();
        }
        return epgDateList;
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
                            getEpg(new LoadCallback() {
                                @Override
                                public void success() {
                                    callback.success();
                                }

                                @Override
                                public void error(String msg) {
                                    callback.success();
                                }
                            }, null);
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
