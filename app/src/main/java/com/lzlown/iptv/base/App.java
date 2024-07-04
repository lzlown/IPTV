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
        initParams();
        AutoSizeConfig.getInstance().setCustomFragment(true).getUnitsManager()
                .setSupportDP(false)
                .setSupportSP(false)
                .setSupportSubunits(Subunits.MM);
    }

    private void initParams() {
        // Hawk
        Hawk.init(this).build();
        Hawk.put(HawkConfig.DEBUG_OPEN, false);
        Hawk.put(HawkConfig.PLAY_TYPE, 0);
//        Hawk.put(HawkConfig.PLAY_RENDER, 1);
//        Hawk.put(HawkConfig.IJK_CODEC, "硬解码");
//        Hawk.put(HawkConfig.PLAY_SCALE, 1);
        Hawk.put(HawkConfig.LIVE_CROSS_GROUP, true);
        Hawk.put(HawkConfig.LIVE_API_URL, "https://lzlown.com:9090/6b16ccd8540eca89/tvcfg.json");
        if (!Hawk.contains(HawkConfig.LIVE_SHOW_EPG)) {
            Hawk.put(HawkConfig.LIVE_SHOW_EPG, true);
        }
        if (!Hawk.contains(HawkConfig.LIVE_SHOW_TIME)) {
            Hawk.put(HawkConfig.LIVE_SHOW_TIME, true);
        }
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
