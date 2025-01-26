package com.lzlown.iptv.config;

import android.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lzlown.iptv.base.App;
import com.lzlown.iptv.bean.*;
import com.lzlown.iptv.util.StringUtils;
import com.lzlown.iptv.util.TimeUtil;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.GetRequest;

import java.text.SimpleDateFormat;
import java.util.*;

public class EpgConfig implements Config {
    private static final EpgConfig instance = new EpgConfig();
    private final Map<String, Map<String, LiveEpgGroup>> liveEpgMap = new HashMap<>();
    private final Map<String, Map<String, LiveEpgGroup>> defaultliveEpgMap = new HashMap<>();
    private String date;
    private final List<LiveEpgDate> epgDateList = new ArrayList<>();
    private final LiveEpgItem defaultLiveEpgItem = new LiveEpgItem(TimeUtil.getTime(), "00:00", "23:59", "暂无预告", 0);
    private String epgUrl;

    SimpleDateFormat df =  new SimpleDateFormat("HH:mm");
    private LiveChannelItem epgSelectedChannel = null;
    private LiveChannelItem epgBackChannel = null;
    private LiveEpgItem selectedEpgItem;
    private String backUrl;

    private EpgConfig() {

    }

    public static EpgConfig get() {
        return instance;
    }

    @Override
    public void init(JsonElement jsonElement, AppConfig.LoadCallback callback) {
        epgUrl = jsonElement.getAsJsonObject().get("epg").getAsString();
        getEpg(callback, null);
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


    //获取EPG
    private void getEpg(AppConfig.LoadCallback callback, String time) {
        GetRequest<String> okGo = AppConfig.get().getOkGo(epgUrl);
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
                        Map<String, LiveEpgGroup> epgMap = new HashMap<>();
                        JsonArray asJsonArray = jsonElement.getAsJsonObject().getAsJsonArray(key);
                        for (int i = 0; i < asJsonArray.size(); i++) {
                            JsonObject asJsonObject = asJsonArray.get(i).getAsJsonObject();
                            String cc = asJsonObject.get("cc").getAsString();
                            JsonArray epgs = asJsonObject.getAsJsonArray("epg");
                            LiveEpgGroup liveEpgGroup = new LiveEpgGroup();
                            liveEpgGroup.setName(cc);
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
                            liveEpgGroup.setEpgItems(liveEpgItems);
                            epgMap.put(cc, liveEpgGroup);
                        }
                        liveEpgMap.put(key, epgMap);
                    }
                    callback.success();
                } catch (Exception e) {
                    callback.success();
                }
            }

            @Override
            public void onError(Response<String> response) {
                callback.success();
            }

            public String convertResponse(okhttp3.Response response) throws Throwable {
                return "";
            }
        });
    }

    private LiveEpgGroup getLiveEpg(String key, String time) {
        if (!liveEpgMap.containsKey(time)) {
            synchronized (this) {
                if (!liveEpgMap.containsKey(time)) {
                    getEpg(new AppConfig.LoadCallback() {
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
        Map<String, LiveEpgGroup> epgMap = liveEpgMap.get(time);
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
            LiveEpgGroup liveEpgGroup = getLiveEpg(channelCh, time);
            if (liveEpgGroup != null && liveEpgGroup.getEpgItems() != null && !liveEpgGroup.getEpgItems().isEmpty()) {
                List<LiveEpgItem> arrayList = liveEpgGroup.getEpgItems();
                int size = arrayList.size() - 1;
                Date cDate = new Date();
                while (size >= 0) {
                    if (cDate.compareTo(TimeUtil.getEpgTime(time + ((LiveEpgItem) arrayList.get(size)).start)) >= 0) {
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
            LiveEpgGroup liveEpgGroup = getLiveEpg(key, TimeUtil.getTime());
            if (liveEpgGroup != null && liveEpgGroup.getEpgItems() != null && !liveEpgGroup.getEpgItems().isEmpty()) {
                List<LiveEpgItem> arrayList = liveEpgGroup.getEpgItems();
                for (int i = 0; i < arrayList.size(); i++) {
                    if (new Date().compareTo(TimeUtil.getEpgTime(time + ((LiveEpgItem) arrayList.get(i)).start)) >= 0) {
                        map.put("c", arrayList.get(i));
                        if (i < arrayList.size() - 1) {
                            map.put("n", arrayList.get(i + 1));
                        } else {
                            LiveEpgGroup liveEpgGroup1 = getLiveEpg(key, TimeUtil.getTime(1));
                            if (null != liveEpgGroup1) {
                                List<LiveEpgItem> arrayList1 = liveEpgGroup1.getEpgItems();
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
    public LiveEpgGroup getLiveEpg(LiveChannelItem item, String time) {
        String channelCh = item.getChannelCh();
        if (StringUtils.isNotEmpty(channelCh)) {
            LiveEpgGroup liveEpgGroup = getLiveEpg(channelCh, time);
            if (liveEpgGroup != null && liveEpgGroup.getEpgItems() != null && !liveEpgGroup.getEpgItems().isEmpty()) {
                return liveEpgGroup;
            }
        } else {
            if (StringUtils.isNotEmpty(item.getSocUrls())) {
                Map<String, LiveEpgGroup> stringLiveEpgMap = defaultliveEpgMap.get(time);
                if (stringLiveEpgMap != null) {
                    LiveEpgGroup liveEpgGroup1 = stringLiveEpgMap.get(item.getChannelName());
                    if (liveEpgGroup1 != null && liveEpgGroup1.getEpgItems() != null && !liveEpgGroup1.getEpgItems().isEmpty()) {
                        return liveEpgGroup1;
                    }
                }
                stringLiveEpgMap = new HashMap<>();
                List<LiveEpgItem> epgItems = new ArrayList<>();
                for (int i = 0; i < 23; i++) {
                    String start = String.format("%02d:00", i);
                    String end = String.format("%02d:00", i + 1);
                    epgItems.add(new LiveEpgItem(time, start, end, item.getChannelName(), i));
                }
                epgItems.add(new LiveEpgItem(time, "23:00", "23:59", item.getChannelName(), 23));
                LiveEpgGroup liveEpgGroup = new LiveEpgGroup();
                liveEpgGroup.setName(item.getChannelName());
                liveEpgGroup.setEpgItems(epgItems);
                stringLiveEpgMap.put(item.getChannelName(), liveEpgGroup);
                defaultliveEpgMap.put(time, stringLiveEpgMap);
                return liveEpgGroup;
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

    public void setEpgSelectedChannel(LiveChannelItem epgSelectedChannel) {
        this.epgSelectedChannel = epgSelectedChannel;
    }

    public LiveChannelItem getEpgSelectedChannel() {
        return epgSelectedChannel;
    }

    public LiveChannelItem getEpgBackChannel() {
        return epgBackChannel;
    }

    public void setEpgBackChannel(LiveChannelItem epgBackChannel) {
        this.epgBackChannel = epgBackChannel;
    }

    public String getBackUrl() {
        return backUrl;
    }

    public void setBackUrl(String backUrl) {
        this.backUrl = backUrl;
    }

    public LiveEpgItem getSelectedEpgItem() {
        return selectedEpgItem;
    }

    public void setSelectedEpgItem(LiveEpgItem selectedEpgItem) {
        this.selectedEpgItem = selectedEpgItem;
    }

    public boolean isCanPlay(LiveEpgItem item) {
        Date date = new Date();
        Date epgStartTime = TimeUtil.getEpgTime(item.currentEpgDate + item.start);
        if (date.compareTo(epgStartTime) < 0 || TimeUtil.getTimeToDate(-7).compareTo(epgStartTime) > 0
                || epgSelectedChannel == null || StringUtils.isEmpty(epgSelectedChannel.getSocUrls())) {
            return false;
        } else
            return date.compareTo(epgStartTime) <= 0 || date.compareTo(TimeUtil.getEpgTime(item.currentEpgDate + item.end)) >= 0;
    }

    public int getLiveEpgItemIndex(List<LiveEpgItem> epgItems) {
        Date time = new Date();
        for (LiveEpgItem epgItem : epgItems) {
            if (selectedEpgItem == epgItem) {
                return epgItem.index;
            }
            if (time.compareTo(TimeUtil.getEpgTime(epgItem.currentEpgDate + epgItem.start)) > 0 &&
                    time.compareTo(TimeUtil.getEpgTime(epgItem.currentEpgDate + epgItem.end)) < 0) {
                return epgItem.index;
            }
        }
        return 0;
    }

    public void init() {
        epgSelectedChannel = null;
        epgBackChannel = null;
        selectedEpgItem = null;
    }

    public void reLoad() {
        if (App.LIVE_SHOW_EPG) getEpg(new AppConfig.LoadCallback() {
            @Override
            public void success() {

            }

            @Override
            public void error(String msg) {

            }
        }, null);
    }
}
