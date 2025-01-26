package com.lzlown.iptv.ui.activity;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.lzlown.iptv.R;
import com.lzlown.iptv.base.BaseActivity;
import com.lzlown.iptv.config.AppConfig;
import com.lzlown.iptv.util.AppManager;

public class HomeActivity extends BaseActivity {
    public static Context context;
    private RelativeLayout ll_loading;
    private ProgressBar progress;
    private TextView loadErr;
    private Handler mHandler = new Handler();
    private AppConfig appConfig;
    private Integer errSum = 0;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_home;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void init() {
        context = this;
        errSum = 0;
        ll_loading = findViewById(R.id.ll_loading);
        progress = findViewById(R.id.loadingBar);
        loadErr = findViewById(R.id.loadErr);
        ll_loading.setVisibility(View.VISIBLE);
        appConfig = AppConfig.get();
        mHandler.post(getCfgRun);
    }

    @Override
    public void onBackPressed() {
        AppManager.getInstance().appExit(0);
        finish();
    }

    private final Runnable getCfgRun = new Runnable() {
        @Override
        public void run() {
            appConfig.init(null, new AppConfig.LoadCallback() {
                @Override
                public void success() {
                    ll_loading.setVisibility(View.GONE);
                    jumpActivity(LivePlayActivity.class);
                }

                @Override
                public void error(String msg) {
                    errSum++;
                    if (errSum > 5) {
                        progress.setVisibility(View.GONE);
                        loadErr.setText(msg);
                        loadErr.setVisibility(View.VISIBLE);
                    } else {
                        mHandler.postDelayed(getCfgRun, 3000);
                    }

                }
            });
        }
    };
}