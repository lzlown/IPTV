package com.lzlown.iptv.ui.activity;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.lzlown.iptv.R;
import com.lzlown.iptv.api.ApiConfig;
import com.lzlown.iptv.base.BaseActivity;
import com.lzlown.iptv.util.AppManager;
import org.greenrobot.eventbus.EventBus;

public class HomeActivity extends BaseActivity {
    public static Context context;
    private RelativeLayout ll_loading;
    private ProgressBar progress;
    private TextView loadErr;
    private Handler mHandler = new Handler();

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
        ll_loading = findViewById(R.id.ll_loading);
        progress = findViewById(R.id.loadingBar);
        loadErr = findViewById(R.id.loadErr);
        ll_loading.setVisibility(View.VISIBLE);
        mHandler.post(getCfgRun);
    }

    @Override
    public void onBackPressed() {
        EventBus.getDefault().unregister(this);
        AppManager.getInstance().appExit(0);
        finish();
    }

    private final Runnable getCfgRun = new Runnable() {

        @Override
        public void run() {
            ApiConfig.get().loadData(new ApiConfig.LoadCallback() {
                @Override
                public void success() {
                    ll_loading.setVisibility(View.GONE);
                    jumpActivity(LivePlayActivity.class);
                }

                @Override
                public void error(String msg) {
                    progress.setVisibility(View.GONE);
                    loadErr.setText(msg);
                    loadErr.setVisibility(View.VISIBLE);
                }
            });
        }
    };
}