package com.lzlown.iptv.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.lzlown.iptv.base.App;
import com.lzlown.iptv.util.HawkConfig;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.GetRequest;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class AppConfig implements Config {
    private static volatile AppConfig instance;
    private CountDownLatch latch;
    private final List<Config> configs = new ArrayList<>();
    private AppConfig() {
    }

    public GetRequest<String> getOkGo(String url) {
        return OkGo.<String>get(url)
                .headers("User-Agent", App.userAgent)
                .headers("Accept", App.requestAccept)
                .headers(App.auth_key, App.auth_value);
    }

    public interface LoadCallback {
        void success();

        void error(String msg);
    }

    public static AppConfig get() {
        if (instance == null) {
            synchronized (AppConfig.class) {
                if (instance == null) {
                    instance = new AppConfig();
                }
            }
        }
        return instance;
    }

    @Override
    public void init(JsonElement json, LoadCallback callback) {
        LoadCallback loadCallback = new LoadCallback() {
            @Override
            public void success() {
                latch.countDown();
                if (latch.getCount() == 0) {
                    callback.success();
                }
            }

            @Override
            public void error(String msg) {
                callback.error(msg);
            }
        };
        getOkGo(Hawk.get(HawkConfig.API_URL)).execute(new AbsCallback<String>() {
            @Override
            public void onSuccess(Response<String> response) {
                try {
                    JsonElement jsonElement = JsonParser.parseString(response.getRawResponse().body().string());
                    PlayerConfig playerConfig = PlayerConfig.get();
                    configs.clear();
                    configs.add(playerConfig);
                    LiveConfig liveConfig = LiveConfig.get();
                    configs.add(liveConfig);
                    if (Hawk.get(HawkConfig.LIVE_SHOW_EPG, false)) {
                        EpgConfig epgConfig = EpgConfig.get();
                        configs.add(epgConfig);
                    }
                    SettingConfig settingConfig = SettingConfig.get();
                    configs.add(settingConfig);
                    latch = new CountDownLatch(configs.size());
                    for (Config baseConfig : configs) {
                        baseConfig.init(jsonElement, loadCallback);
                    }
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
}
