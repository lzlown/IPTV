package com.lzlown.iptv.base;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import androidx.multidex.MultiDexApplication;
import com.lzlown.iptv.util.HawkConfig;
import com.lzlown.iptv.videocache.HttpProxyCacheServer;
import com.lzlown.iptv.videocache.headers.HeaderInjector;
import com.orhanobut.hawk.Hawk;
import me.jessyan.autosize.AutoSizeConfig;

import java.util.HashMap;
import java.util.Map;

public class App extends MultiDexApplication implements HeaderInjector {
    private static App instance;
    public static Boolean LIVE_SHOW_EPG;
    public static Integer LIVE_UI_SHOW_TIME;
    public static Integer LIVE_CONNECT_TIMEOUT;
    private HttpProxyCacheServer proxy;
    public static final String auth_str = "lzlown_proxy_auth=auth";
    public static final String auth_key = "lzlown-auth";
    public static final String auth_value = "lzlown";
    public static final String userAgent = "lzlown-tv";
    public static final String requestAccept = "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9";
    public static HttpProxyCacheServer getProxy() {
        return instance.proxy;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        proxy=new HttpProxyCacheServer.Builder(this).headerInjector(this).build();
        Hawk.init(this).build();
        initParams();
        AutoSizeConfig.getInstance().setCustomFragment(true).getUnitsManager()
                .setSupportDP(true)
                .setSupportSP(true);
        try {
            PackageManager packageManager = getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            int versionCode = packageInfo.versionCode;
            Integer lzlVersionCode = Hawk.get("lzlVersionCode", -1);
            if (lzlVersionCode < versionCode) {
                cleanParams();
                Hawk.put("lzlVersionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException ignored) {

        }
    }

    private void initParams() {
        Hawk.put(HawkConfig.PLAY_TYPE, 0);
        Hawk.put(HawkConfig.PLAY_SCALE, 0);
        Hawk.put(HawkConfig.LIVE_CROSS_GROUP, true);
        Hawk.put(HawkConfig.API_URL, "https://lzlown.com:9090/6e082dd89e717324/v3/tvcfg.json");
        if (!Hawk.contains(HawkConfig.LIVE_CONNECT_TIMEOUT)) {
            Hawk.put(HawkConfig.LIVE_CONNECT_TIMEOUT, 5);
        }
        if (!Hawk.contains(HawkConfig.LIVE_SHOW_EPG)) {
            Hawk.put(HawkConfig.LIVE_SHOW_EPG, false);
        }
        if (!Hawk.contains(HawkConfig.LIVE_SHOW_TIME)) {
            Hawk.put(HawkConfig.LIVE_SHOW_TIME, false);
        }
        if (!Hawk.contains(HawkConfig.LIVE_SHOW_SPEED)) {
            Hawk.put(HawkConfig.LIVE_SHOW_SPEED, false);
        }
        if (!Hawk.contains(HawkConfig.LIVE_UI_SHOW_TIME)) {
            Hawk.put(HawkConfig.LIVE_UI_SHOW_TIME, 10);
        }
        LIVE_SHOW_EPG= Hawk.get(HawkConfig.LIVE_SHOW_EPG, false);
        LIVE_UI_SHOW_TIME= Hawk.get(HawkConfig.LIVE_UI_SHOW_TIME, 10)*1000;
        LIVE_CONNECT_TIMEOUT= Hawk.get(HawkConfig.LIVE_CONNECT_TIMEOUT, 5)*1000;
    }

    public void cleanParams(){
        Hawk.deleteAll();
        initParams();
        try {
            PackageManager packageManager = getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            Hawk.put("lzlVersionCode", packageInfo.versionCode);
        } catch (PackageManager.NameNotFoundException ignored) {

        }
    }

    public static App getInstance() {
        return instance;
    }

    @Override
    public Map<String, String> addHeaders(String url) {
        Map<String, String> map=new HashMap<>();
        map.put("User-Agent", userAgent);
        return map;
    }
}
