package com.lzlown.iptv.base;

import android.app.Activity;
import androidx.multidex.MultiDexApplication;
import com.lzlown.iptv.util.AppManager;
import com.lzlown.iptv.util.HawkConfig;
import com.orhanobut.hawk.Hawk;
import me.jessyan.autosize.AutoSizeConfig;
import me.jessyan.autosize.unit.Subunits;

public class App extends MultiDexApplication {
    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Hawk.init(this).build();
        initParams();
        AutoSizeConfig.getInstance().setCustomFragment(true).getUnitsManager()
                .setSupportDP(false)
                .setSupportSP(false)
                .setSupportSubunits(Subunits.MM);
    }

    private void initParams() {
        Hawk.put(HawkConfig.PLAY_TYPE, 1);
        Hawk.put(HawkConfig.PLAY_SCALE, 1);
        Hawk.put(HawkConfig.LIVE_CROSS_GROUP, true);
        if (!Hawk.contains(HawkConfig.API_URL)) {
            Hawk.put(HawkConfig.API_URL, "https://lzlown.com:9090/6b16ccd8540eca89/tvcfg.json");
        }
        if (!Hawk.contains(HawkConfig.LIVE_SHOW_EPG)) {
            Hawk.put(HawkConfig.LIVE_SHOW_EPG, true);
        }
        if (!Hawk.contains(HawkConfig.LIVE_CONNECT_TIMEOUT)) {
            Hawk.put(HawkConfig.LIVE_CONNECT_TIMEOUT, 10);
        }
        if (!Hawk.contains(HawkConfig.LIVE_SHOW_TIME)) {
            Hawk.put(HawkConfig.LIVE_SHOW_TIME, true);
        }
        if (!Hawk.contains(HawkConfig.LIVE_SHOW_SPEED)) {
            Hawk.put(HawkConfig.LIVE_SHOW_SPEED, true);
        }
    }

    public void cleanParams(){
        Hawk.deleteAll();
        initParams();
    }

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public Activity getCurrentActivity() {
        return AppManager.getInstance().currentActivity();
    }
}
