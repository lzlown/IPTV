package com.lzlown.iptv.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.lzlown.iptv.bean.LivePlayerOption;

import java.util.*;

public class PlayerConfig implements Config {
    private static final PlayerConfig instance = new PlayerConfig();

    private PlayerConfig() {

    }

    public static PlayerConfig get() {
        return instance;
    }

    private final HashMap<String, List<LivePlayerOption>> ijkOptions = new HashMap<>();

    public HashMap<String, List<LivePlayerOption>> getIjkOptions() {
        return ijkOptions;
    }

    private List<LivePlayerOption> defaultIJK() {
        List<LivePlayerOption> list = new ArrayList<>();
        list.add(new LivePlayerOption(4, "opensles", "0"));
//        list.add(new LivePlayerOption(4, "framedrop", "5"));
        list.add(new LivePlayerOption(4, "start-on-prepared", "1"));
        list.add(new LivePlayerOption(1, "http-detect-rangeupport", "0"));
//        list.add(new LivePlayerOption(2, "skip_loop_filter", "0"));
        list.add(new LivePlayerOption(4, "reconnect", "5"));
        list.add(new LivePlayerOption(4, "fast", "1"));
        list.add(new LivePlayerOption(1, "fflags", "fastseek"));
        list.add(new LivePlayerOption(4, "enable-accurate-seek", "1"));

        list.add(new LivePlayerOption(4, "mediacodec", "1"));
        list.add(new LivePlayerOption(4, "mediacodec-all-videos", "1"));
        list.add(new LivePlayerOption(4, "mediacodec-auto-rotate", "1"));
        list.add(new LivePlayerOption(4, "mediacodec-handle-resolution-change", "1"));
        return list;
    }

    @Override
    public void init(JsonElement jsonElement, AppConfig.LoadCallback callback) {
        ijkOptions.put("default", defaultIJK());
        try {
            JsonArray ijk_options = jsonElement.getAsJsonObject().getAsJsonArray("ijk");
            for (JsonElement option : ijk_options) {
                String group = option.getAsJsonObject().get("group").getAsString();
                List<LivePlayerOption> optionList = ijkOptions.get(group);
                if (null == optionList) {
                    optionList = new ArrayList<>();
                }
                JsonArray rules = option.getAsJsonObject().getAsJsonArray("options");
                for (JsonElement item : rules) {
                    int category = item.getAsJsonObject().get("category").getAsInt();
                    String name = item.getAsJsonObject().get("name").getAsString();
                    String value = item.getAsJsonObject().get("value").getAsString();
                    LivePlayerOption livePlayerOption = new LivePlayerOption(category, name, value);
                    optionList.add(livePlayerOption);
                }
                ijkOptions.put(group, optionList);
            }
        } catch (Exception ignored) {
        } finally {
            callback.success();
        }
    }
}
