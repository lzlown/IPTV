package com.lzlown.iptv.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.IntEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.lzlown.iptv.R;
import com.lzlown.iptv.base.App;
import com.lzlown.iptv.base.BaseActivity;
import com.lzlown.iptv.bean.*;
import com.lzlown.iptv.config.EpgConfig;
import com.lzlown.iptv.config.LiveConfig;
import com.lzlown.iptv.config.SettingConfig;
import com.lzlown.iptv.player.controller.LiveController;
import com.lzlown.iptv.ui.adapter.*;
import com.lzlown.iptv.ui.tv.widget.ViewObj;
import com.lzlown.iptv.util.*;
import com.lzlown.iptv.videoplayer.player.VideoView;
import com.lzlown.iptv.videoplayer.util.PlayerUtils;
import com.orhanobut.hawk.Hawk;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7LinearLayoutManager;

import java.text.SimpleDateFormat;
import java.util.*;

public class LivePlayActivity extends BaseActivity {
    private static final String TAG = LivePlayActivity.class.getName();
    public static Context context;
    private VideoView mVideoView;
    private Handler mHandler = new Handler();
    private final LivePlayerManager livePlayerManager = new LivePlayerManager();

    //退出返回按键间隔
    private long mExitTime = 0;

    private final LiveConfig liveConfig = LiveConfig.get();
    private final EpgConfig epgConfig = EpgConfig.get();
    private final SettingConfig settingConfig = SettingConfig.get();

    //频道列表
    private LinearLayout tvChannelLayout;
    private TvRecyclerView mChannelGroupView;
    private TvRecyclerView mChannelItemView;
    private LiveChannelGroupAdapter liveChannelGroupAdapter;
    private LiveChannelItemAdapter liveChannelItemAdapter;

    //设置列表
    private LinearLayout tvSettingLayout;
    private TvRecyclerView mSettingGroupView;
    private LiveSettingGroupAdapter liveSettingGroupAdapter;
    //设置-子项
    private LinearLayout tvSettingItemLayout;
    private TextView settingItemName;
    private TvRecyclerView mSettingMenuView;
    private LiveEpgItemAdapter liveSettingItemEpgAdapter;
    private LiveSettingItemAdapter liveSettingItemSourceAdapter;
    private LiveSettingItemAdapter liveSettingItemScaleAdapter;


    //右设置列表
    private LinearLayout tvRightSettingGroupLayout;
    private TvRecyclerView mRightSettingGroupView;
    private LiveRightSettingGroupAdapter liveRightSettingGroupAdapter;
    //右设置-子项
    private LinearLayout tvRightSettingItemLayout;
    private TvRecyclerView mRightSettingItemView;
    private LiveSettingItemAdapter liveRightSettingItemAdapter;

    //右边显示
    private TextView tvName;
    private TextView tvTime;
    private TextView tvSpeed;
    private TextView tvBack;

    private TextView tvSTime;

    //回放显示列表
    private View tvEpgLayout;
    private TvRecyclerView mEpgChannelView;
    private TvRecyclerView mEpgDateView;
    private TvRecyclerView mEpgItemView;
    private LiveEpgChannelAdapter liveEpgChannelAdapter;
    private LiveEpgDateAdapter liveEpgDateAdapter;
    private LiveEpgItemAdapter liveEpgItemAdapter;
    private TextView no_epg;

    //回放控制
    private View tvBackLayout;
    private SeekBar TvSBar;
    private TextView tv_position;
    private TextView tv_duration;

    private Boolean isCanBack = false;
    private LiveEpgItem selectedEpgItem;
    private int selectTime = 0;
    private boolean mIsDragging;
    //判断睡眠重置
    private boolean loadEnd = false;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_live_play;
    }

    @Override
    protected void init() {
        context = this;
        initView();
    }

    protected void initView() {
        mVideoView = findViewById(R.id.mVideoView);

        loadEnd = false;
        //todo 睡眠处理
        mVideoView.release();
        mVideoView.setUrl(liveConfig.getLiveChannelGroupList().get(0).getLiveChannels().get(0).getUrl());
        mVideoView.start();
        mVideoView.setScreenScaleType(0);

        //界面 view
        tvChannelLayout = findViewById(R.id.tvLeftChannnelListLayout);
        mChannelGroupView = findViewById(R.id.mGroupGridView);
        mChannelItemView = findViewById(R.id.mChannelGridView);


        tvSTime = findViewById(R.id.tvSTime);
        tvSettingLayout = findViewById(R.id.tvSettingLayout);
        mSettingGroupView = findViewById(R.id.mSettingView);
        tvSettingItemLayout = findViewById(R.id.settingItemLayout);
        settingItemName = findViewById(R.id.settingItemName);
        mSettingMenuView = findViewById(R.id.mSettingMenuView);

        tvRightSettingGroupLayout = findViewById(R.id.tvRightSettingGroupLayout);
        mRightSettingGroupView = findViewById(R.id.mSettingGroupView);
        tvRightSettingItemLayout = findViewById(R.id.tvRightSettingItemLayout);
        mRightSettingItemView = findViewById(R.id.mSettingItemView);
        //右边显示
        tvName = findViewById(R.id.tvName);
        tvTime = findViewById(R.id.tvTime);
        tvSpeed = findViewById(R.id.tvSpeed);

        tvBack = findViewById(R.id.tvBack);

        tvEpgLayout = findViewById(R.id.divEPG);
        mEpgChannelView = findViewById(R.id.mEpgChannelView);
        mEpgDateView = findViewById(R.id.mEpgDateGridView);
        mEpgItemView = findViewById(R.id.lv_epg);
        no_epg = findViewById(R.id.no_epg);

        tvBackLayout = findViewById(R.id.seekbar);
        tvBackLayout.setVisibility(View.INVISIBLE);
        TvSBar = findViewById(R.id.pb_progressbar);
        tv_position = findViewById(R.id.tv_currentpos);
        tv_duration = findViewById(R.id.tv_duration);


        initVideoView();
        initChannelView();
        initSettingGroupView();
        initEpgGroupView();
        initTvSBar();
        initLiveChannelList();

        showTime();
        showSpeed();
        mHandler.post(mUpdateTimeRun);
        mHandler.post(mUpdateSpeedRun);
    }

    @Override
    public void onBackPressed() {
        if (tvChannelLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideChannelRun);
            mHandler.post(mHideChannelRun);
        } else if (tvEpgLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideEpgRun);
            mHandler.post(mHideEpgRun);
        } else if (tvBackLayout.getVisibility() == View.VISIBLE) {
            showProgressBar(false);
        } else if (tvSettingLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideSettingGroupRun);
            mHandler.post(mHideSettingGroupRun);
        } else if (tvSettingItemLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideSettingItemRun);
            mHandler.post(mHideSettingItemRun);
        } else if (tvRightSettingGroupLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideRightSettingItemRun);
            mHandler.post(mHideRightSettingItemRun);
            mHandler.removeCallbacks(mHideRightSettingGroupRun);
            mHandler.post(mHideRightSettingGroupRun);
        } else {
            exit();
        }


    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int keyCode = event.getKeyCode();
            if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_SPACE) {
                showSettingView();
            } else if (!isListOrSettingLayoutVisible()) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_UP:
                        if (Hawk.get(HawkConfig.LIVE_CHANNEL_REVERSE, true)) {
                            playNext(1);
                        } else {
                            playNext(-1);
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        if (Hawk.get(HawkConfig.LIVE_CHANNEL_REVERSE, true)) {
                            playNext(-1);
                        } else {
                            playNext(1);
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        showEpgView();
                        break;
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        if (isCanBack && mVideoView.isPlaying()) {
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    showProgressBar(true);
                                }
                            }, 200);
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_ENTER:
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                        showChannelView();
                        break;
                }
            }
        } else {
            event.getAction();
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mVideoView != null && loadEnd) {
            //todo 睡眠处理
            jumpActivity(LivePlayActivity.class);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoView != null) {
            mVideoView.release();
        }
        loadEnd = true;
        liveConfig.setCurrentLiveChannelItem(null);
        epgConfig.reSet();
        rmAllCallback();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVideoView != null) {
            mVideoView.release();
            mVideoView = null;
        }
    }

    private void rmAllCallback() {
        mHandler.removeCallbacks(mConnectTimeoutChangeSourceRun);
        mHandler.removeCallbacks(mUpdateTimeRun);
        mHandler.removeCallbacks(mUpdateSpeedRun);
    }

    private void exit() {
        if (System.currentTimeMillis() - mExitTime < 2000) {
            if (mVideoView != null) {
                mVideoView.release();
            }
            rmAllCallback();
            mVideoView = null;
            mHandler = null;
            AppManager.getInstance().appExit(0);
            finish();
            super.onBackPressed();
        } else {
            mExitTime = System.currentTimeMillis();
            Toast.makeText(mContext, "再按一次返回键退出应用", Toast.LENGTH_SHORT).show();
        }
    }

    private int getCurrentChannelGroupIndex() {
        return liveConfig.getCurrentLiveChannelItem() == null ? -1 : liveConfig.getCurrentLiveChannelItem().getChannelGroupIndex();
    }

    private int getCurrentLiveChannelIndex() {
        return liveConfig.getCurrentLiveChannelItem() == null ? -1 : liveConfig.getCurrentLiveChannelItem().getChannelIndex();
    }

    public LiveChannelItem getCurrentLiveChannelItem() {
        return liveConfig.getCurrentLiveChannelItem();
    }

    private void initVideoView() {
        LiveController controller = new LiveController(this);
        controller.setListener(new LiveController.LiveControlListener() {
            @Override
            public boolean singleTap() {
                showChannelView();
                return true;
            }

            @Override
            public void longPress() {
                showSettingView();
            }

            @Override
            public void playStateChanged(int playState) {
                switch (playState) {
                    case VideoView.STATE_PREPARED:
                    case VideoView.STATE_BUFFERED:
                    case VideoView.STATE_PLAYING:
                        mHandler.removeCallbacks(mConnectTimeoutChangeSourceRun);
                        mHandler.removeCallbacks(mHideChannelNameRun);
                        mHandler.postDelayed(mHideChannelNameRun, 5000);
                        break;
                    case VideoView.STATE_ERROR:
                    case VideoView.STATE_PREPARING:
                    case VideoView.STATE_BUFFERING:
                        mHandler.removeCallbacks(mConnectTimeoutChangeSourceRun);
                        if (App.LIVE_CONNECT_TIMEOUT < 0) {
                            mHandler.postDelayed(mConnectTimeoutChangeSourceRun, 10 * 1000);
                        } else {
                            mHandler.postDelayed(mConnectTimeoutChangeSourceRun, App.LIVE_CONNECT_TIMEOUT);
                        }
                        break;
                    case VideoView.STATE_PLAYBACK_COMPLETED:
                        mHandler.removeCallbacks(mConnectTimeoutChangeSourceRun);
                        mHandler.postDelayed(mConnectTimeoutChangeSourceRun, 3 * 1000);
                        break;
                }
            }

            @Override
            public void changeSource(int direction) {
                if (direction > 0) {
                    if (isCanBack && mVideoView.isPlaying()) {
                        if (isListOrSettingLayoutVisible()) return;
                        showProgressBar(true);
                    }
                } else {
                    showEpgView();
                }
            }
        });
        controller.setCanChangePosition(false);
        controller.setEnableInNormal(true);
        controller.setGestureEnabled(true);
        controller.setDoubleTapTogglePlayEnabled(false);
        mVideoView.setVideoController(controller);
        mVideoView.setProgressManager(null);
        livePlayerManager.init(mVideoView);
    }

    private void initTvRecyclerView(TvRecyclerView view, BaseQuickAdapter adapter, Runnable runnable, int orientation) {
        view.setHasFixedSize(true);
        view.setLayoutManager(new V7LinearLayoutManager(this.mContext, orientation, false));
        adapter.closeLoadAnimation();
        view.setAdapter(adapter);
        view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mHandler.removeCallbacks(runnable);
                mHandler.postDelayed(runnable, App.LIVE_UI_SHOW_TIME);
            }
        });
    }


    //播放准备
    private void playChannel(int channelGroupIndex, int liveChannelIndex, boolean changeSource) {
        LiveChannelItem liveChannelItem = liveConfig.getLiveChannels(channelGroupIndex).get(liveChannelIndex);
        if (liveConfig.getCurrentLiveChannelItem() != null && liveConfig.getCurrentLiveChannelItem().equals(liveChannelItem) && !changeSource || (changeSource && liveChannelItem.getSourceNum() == 1)) {
            if (!isCanBack) {
                return;
            }
        }
        if (!changeSource) {
            liveConfig.setCurrentLiveChannelItem(liveChannelItem);
            Hawk.put(HawkConfig.LIVE_GROUP, channelGroupIndex);
            Hawk.put(HawkConfig.LIVE_CHANNEL, liveChannelItem.getChannelName());
            tvName.setText(String.format("%d %s", liveChannelItem.getChannelNum(), liveChannelItem.getChannelName()));
            if (tvSettingLayout.getVisibility() == View.VISIBLE || tvRightSettingGroupLayout.getVisibility() == View.VISIBLE) {
                mHandler.removeCallbacks(mHideChannelNameRun);
                mHandler.post(mHideChannelNameRun);
                showEpg();
                showInfo();
            } else {
                tvName.setVisibility(View.VISIBLE);
            }

        }
        tvBack.setVisibility(View.GONE);
        mHandler.removeCallbacks(playChannelRun);
        mHandler.postDelayed(playChannelRun, 100);
    }

    //播放实行
    private final Runnable playChannelRun = new Runnable() {
        @Override
        public void run() {
            LiveChannelItem liveChannelItem = getCurrentLiveChannelItem();
            livePlayerManager.getLiveChannelPlayer(mVideoView, getCurrentChannelGroupIndex() + liveChannelItem.getChannelName() + liveChannelItem.getSourceIndex());
            isCanBack = liveChannelItem.getUrl().contains(".mp4");
            selectedEpgItem = null;
            epgConfig.setEpgBackChannel(null);
            if (liveEpgItemAdapter != null) liveEpgItemAdapter.setLiveEpgItem(null);
            selectTime = 0;
            mHandler.removeCallbacks(backChangeRun);
            showProgressBar(false);
            mVideoView.release();
            mVideoView.setUrl(liveChannelItem.getUrl());
            mVideoView.start();
        }
    };

    //播放下一个
    private void playNext(int direction) {
        if (getCurrentLiveChannelItem() == null) {
            Toast.makeText(App.getInstance(), "请先选择频道", Toast.LENGTH_SHORT).show();
            return;
        }
        Integer[] groupChannelIndex = liveConfig.getNextChannel(direction);
        playChannel(groupChannelIndex[0], groupChannelIndex[1], false);
    }

    private final Runnable mConnectTimeoutChangeSourceRun = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG, "mConnectTimeoutChangeSourceRun: ");
            if (tvBack.getVisibility() == View.VISIBLE || App.LIVE_CONNECT_TIMEOUT < 0) {
                playChannel(getCurrentChannelGroupIndex(), getCurrentLiveChannelIndex(), false);
                Log.i(TAG, "mConnectTimeoutChangeSourceRun: dd");
                return;
            }
            Integer[] groupChannelIndex = liveConfig.getNextChannel(Hawk.get(HawkConfig.LIVE_CHANNEL_REVERSE, false) ? -1 : 1);
            playChannel(groupChannelIndex[0], groupChannelIndex[1], false);

        }
    };

    //是否有控件显示
    private boolean isListOrSettingLayoutVisible() {
        return tvChannelLayout.getVisibility() == View.VISIBLE ||
                tvSettingLayout.getVisibility() == View.VISIBLE ||
                tvSettingItemLayout.getVisibility() == View.VISIBLE ||
                tvRightSettingGroupLayout.getVisibility() == View.VISIBLE ||
                tvEpgLayout.getVisibility() == View.VISIBLE ||
                tvBackLayout.getVisibility() == View.VISIBLE;
    }


    //左侧节目列表
    private void initChannelView() {
        liveChannelGroupAdapter = new LiveChannelGroupAdapter();
        liveChannelGroupAdapter.setNewData(liveConfig.getLiveChannelGroupList());
        initTvRecyclerView(mChannelGroupView, liveChannelGroupAdapter, mHideChannelRun, LinearLayout.VERTICAL);
        mChannelGroupView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                selectChannel(0, position, true, -1);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {

            }
        });
        liveChannelGroupAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                selectChannel(0, position, false, -1);
            }
        });


        liveChannelItemAdapter = new LiveChannelItemAdapter();
        initTvRecyclerView(mChannelItemView, liveChannelItemAdapter, mHideChannelRun, LinearLayout.VERTICAL);
        mChannelItemView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                if (position < 0) return;
                liveChannelGroupAdapter.setFocusedGroupIndex(-1);
                liveChannelItemAdapter.setFocusedChannelIndex(position);
                mHandler.removeCallbacks(mHideChannelRun);
                mHandler.postDelayed(mHideChannelRun, App.LIVE_UI_SHOW_TIME);

            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {
                selectChannel(1, -1, false, position);
            }
        });
        liveChannelItemAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                selectChannel(1, -1, false, position);
            }
        });

        tvChannelLayout.setVisibility(View.INVISIBLE);
    }

    private void showChannelView() {
        if (tvSettingLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideSettingGroupRun);
            mHandler.post(mHideSettingGroupRun);
            return;
        }
        if (tvSettingItemLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideSettingItemRun);
            mHandler.post(mHideSettingItemRun);
            return;
        }
        if (tvRightSettingGroupLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideRightSettingItemRun);
            mHandler.post(mHideRightSettingItemRun);
            mHandler.removeCallbacks(mHideRightSettingGroupRun);
            mHandler.post(mHideRightSettingGroupRun);
            return;
        }
        if (tvEpgLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideEpgRun);
            mHandler.post(mHideEpgRun);
            return;
        }
        if (tvBackLayout.getVisibility() == View.VISIBLE) {
            showProgressBar(false);
            return;
        }
        if (tvChannelLayout.getVisibility() != View.VISIBLE) {
            //重新载入上一次状态
            int currentChannelGroupIndex = getCurrentChannelGroupIndex();
            int currentLiveChannelIndex = getCurrentLiveChannelIndex();
            liveChannelItemAdapter.setNewData(liveConfig.getLiveChannels(currentChannelGroupIndex));
            if (currentLiveChannelIndex > -1) {
                mChannelItemView.scrollToPosition(currentLiveChannelIndex);
                LinearLayoutManager layoutManager = (LinearLayoutManager) mChannelItemView.getLayoutManager();
                if (layoutManager != null) {
                    layoutManager.scrollToPositionWithOffset(currentLiveChannelIndex, 0);
                }
            }
            mChannelItemView.setSelection(currentLiveChannelIndex);
            mChannelGroupView.scrollToPosition(currentChannelGroupIndex);
            LinearLayoutManager layoutManager = (LinearLayoutManager) mChannelGroupView.getLayoutManager();
            if (layoutManager != null) {
                layoutManager.scrollToPositionWithOffset(currentChannelGroupIndex, 0);
            }
            mChannelGroupView.setSelection(currentChannelGroupIndex);
            mHandler.postDelayed(mShowChannelRun, 200);
        } else {
            mHandler.removeCallbacks(mHideChannelRun);
            mHandler.post(mHideChannelRun);
        }
    }

    private final Runnable mShowChannelRun = new Runnable() {
        @Override
        public void run() {
            if (mChannelGroupView.isScrolling() || mChannelItemView.isScrolling() ||
                    mChannelGroupView.isComputingLayout() || mChannelItemView.isComputingLayout()) {
                mHandler.postDelayed(this, 100);
            } else {
                liveChannelGroupAdapter.setSelectedGroupIndex(getCurrentChannelGroupIndex());
                liveChannelItemAdapter.setSelectedChannelIndex(getCurrentLiveChannelIndex());
                RecyclerView.ViewHolder holder = mChannelItemView.findViewHolderForAdapterPosition(getCurrentLiveChannelIndex());
                if (holder != null)
                    holder.itemView.requestFocus();
                tvChannelLayout.setVisibility(View.VISIBLE);
                tvEpgLayout.requestLayout();
                ViewObj viewObj = new ViewObj(tvChannelLayout, (ViewGroup.MarginLayoutParams) tvChannelLayout.getLayoutParams());
                ObjectAnimator animator = ObjectAnimator.ofObject(viewObj, "marginLeft", new IntEvaluator(), -tvChannelLayout.getLayoutParams().width, 0);
                animator.setDuration(200);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        tvChannelLayout.clearAnimation();
                        mHandler.removeCallbacks(mHideChannelRun);
                        mHandler.postDelayed(mHideChannelRun, App.LIVE_UI_SHOW_TIME);
                    }
                });
                animator.start();
            }
        }
    };

    private final Runnable mHideChannelRun = new Runnable() {
        @Override
        public void run() {
            if (tvChannelLayout.getVisibility() == View.VISIBLE) {
                tvChannelLayout.setVisibility(View.GONE);
            }
        }
    };

    private final Runnable mHideChannelNameRun = new Runnable() {
        @Override
        public void run() {
            tvName.setVisibility(View.GONE);
        }
    };

    private void selectChannel(int group, int groupIndex, boolean focus, int liveChannelIndex) {
        switch (group) {
            case 0:
                if (focus) {
                    liveChannelGroupAdapter.setFocusedGroupIndex(groupIndex);
                    liveChannelItemAdapter.setFocusedChannelIndex(-1);
                }
                if ((groupIndex > -1 && groupIndex != liveChannelGroupAdapter.getSelectedGroupIndex())) {
                    liveChannelGroupAdapter.setSelectedGroupIndex(groupIndex);
                    liveChannelItemAdapter.setNewData(liveConfig.getLiveChannels(groupIndex));
                    int currentChannelGroupIndex = getCurrentChannelGroupIndex();
                    int currentLiveChannelIndex = getCurrentLiveChannelIndex();
                    if (groupIndex == currentChannelGroupIndex) {
                        if (currentLiveChannelIndex > -1) {
                            mChannelItemView.scrollToPosition(currentLiveChannelIndex);
                            LinearLayoutManager layoutManager = (LinearLayoutManager) mChannelItemView.getLayoutManager();
                            if (layoutManager != null) {
                                layoutManager.scrollToPositionWithOffset(currentLiveChannelIndex, 0);
                            }
                        }
                        liveChannelItemAdapter.setSelectedChannelIndex(currentLiveChannelIndex);
                    } else {
                        mChannelItemView.scrollToPosition(0);
                        liveChannelItemAdapter.setSelectedChannelIndex(-1);
                        LinearLayoutManager layoutManager = (LinearLayoutManager) mChannelItemView.getLayoutManager();
                        if (layoutManager != null) {
                            layoutManager.scrollToPositionWithOffset(0, 0);
                        }
                    }
                    if (liveChannelIndex > -1) {
                        selectChannel(1, -1, false, liveChannelIndex);
                        mChannelGroupView.scrollToPosition(groupIndex);
                        LinearLayoutManager layoutManager = (LinearLayoutManager) mChannelGroupView.getLayoutManager();
                        if (layoutManager != null) {
                            layoutManager.scrollToPositionWithOffset(groupIndex, 0);
                        }
                        mChannelItemView.scrollToPosition(liveChannelIndex);
                        LinearLayoutManager layoutManager2 = (LinearLayoutManager) mChannelItemView.getLayoutManager();
                        if (layoutManager2 != null) {
                            layoutManager2.scrollToPositionWithOffset(liveChannelIndex, 0);
                        }
                        playChannel(groupIndex, liveChannelIndex, false);
                    }
                }
                break;
            case 1:
                liveChannelItemAdapter.setSelectedChannelIndex(liveChannelIndex);
                playChannel(liveChannelGroupAdapter.getSelectedGroupIndex(), liveChannelIndex, false);
                break;
        }
        if (tvChannelLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideChannelRun);
            mHandler.postDelayed(mHideChannelRun, App.LIVE_UI_SHOW_TIME);
        }
    }

    //初始播放
    private void initLiveChannelList() {
        List<LiveChannelGroup> list = liveConfig.getLiveChannelGroupList();
        if (list.isEmpty()) {
            Toast.makeText(App.getInstance(), "频道列表为空", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            String lastChannelName = Hawk.get(HawkConfig.LIVE_CHANNEL, "");
            Integer lastChannelGroupIndex = Hawk.get(HawkConfig.LIVE_GROUP, -1);
            int lastLiveChannelIndex = -1;
            if (lastChannelGroupIndex != -1) {
                if (lastChannelGroupIndex < liveConfig.getLiveChannelGroupList().size()) {
                    LiveChannelGroup liveChannelGroup = liveConfig.getLiveChannelGroupList().get(lastChannelGroupIndex);
                    for (LiveChannelItem liveChannelItem : liveChannelGroup.getLiveChannels()) {
                        if (liveChannelItem.getChannelName().equals(lastChannelName)) {
                            lastLiveChannelIndex = liveChannelItem.getChannelIndex();
                            break;
                        }
                    }
                } else {
                    lastChannelGroupIndex = -1;
                }
            }
            if (lastChannelGroupIndex == -1 || lastLiveChannelIndex == -1) {
                lastChannelGroupIndex = 0;
                lastLiveChannelIndex = 0;
            }
            selectChannel(0, lastChannelGroupIndex, false, lastLiveChannelIndex);
        }
    }


    //设置
    private void initSettingGroupView() {
        //设置菜单
        liveSettingGroupAdapter = new LiveSettingGroupAdapter();
        liveSettingGroupAdapter.setNewData(settingConfig.getLiveSettingGroupList());
        initTvRecyclerView(mSettingGroupView, liveSettingGroupAdapter, mHideSettingGroupRun, LinearLayout.HORIZONTAL);
        mSettingGroupView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                mHandler.removeCallbacks(mHideSettingGroupRun);
                mHandler.postDelayed(mHideSettingGroupRun, App.LIVE_UI_SHOW_TIME);
                if (position < 0) return;
                liveSettingGroupAdapter.setFocusedGroupIndex(position);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {
            }
        });
        liveSettingGroupAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                if (position < 0) return;
                selectSettingGroup(0, position, false);
            }
        });
        tvSettingLayout.setVisibility(View.INVISIBLE);


        //设置子项
        mSettingMenuView.setHasFixedSize(true);
        mSettingMenuView.setLayoutManager(new V7LinearLayoutManager(this.mContext, LinearLayout.VERTICAL, false));
        mSettingMenuView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mHandler.removeCallbacks(mHideSettingItemRun);
                mHandler.postDelayed(mHideSettingItemRun, App.LIVE_UI_SHOW_TIME);
            }
        });
        mSettingMenuView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {

            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                mHandler.removeCallbacks(mHideSettingItemRun);
                mHandler.postDelayed(mHideSettingItemRun, App.LIVE_UI_SHOW_TIME);
                if (position < 0) return;
                selectSettingGroup(1, position, true);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {

            }
        });


        //设置子项 节目单
        liveSettingItemEpgAdapter = new LiveEpgItemAdapter();
        liveSettingItemEpgAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                mHandler.removeCallbacks(mHideSettingItemRun);
                mHandler.postDelayed(mHideSettingItemRun, App.LIVE_UI_SHOW_TIME);
                if (position < 0) return;
                selectSettingGroup(1, position, false);

            }
        });
        //设置子项 节目源
        liveSettingItemSourceAdapter = new LiveSettingItemAdapter();
        liveSettingItemSourceAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                mHandler.removeCallbacks(mHideSettingItemRun);
                mHandler.postDelayed(mHideSettingItemRun, App.LIVE_UI_SHOW_TIME);
                if (position < 0) return;
                selectSettingGroup(1, position, false);
            }
        });
        //设置子项 节目画面比例
        liveSettingItemScaleAdapter = new LiveSettingItemAdapter();
        liveSettingItemScaleAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                mHandler.removeCallbacks(mHideSettingItemRun);
                mHandler.postDelayed(mHideSettingItemRun, App.LIVE_UI_SHOW_TIME);
                if (position < 0) return;
                selectSettingGroup(1, position, false);
            }
        });
        tvSettingItemLayout.setVisibility(View.INVISIBLE);


        //设置子项 更多设置
        liveRightSettingGroupAdapter = new LiveRightSettingGroupAdapter();
        initTvRecyclerView(mRightSettingGroupView, liveRightSettingGroupAdapter, mHideRightSettingGroupRun, LinearLayout.VERTICAL);
        mRightSettingGroupView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {

            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                mHandler.removeCallbacks(mHideRightSettingGroupRun);
                mHandler.postDelayed(mHideRightSettingGroupRun, App.LIVE_UI_SHOW_TIME);
                if (position < 0) return;
                liveRightSettingGroupAdapter.setFocusedGroupIndex(position);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {

            }
        });
        liveRightSettingGroupAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                mHandler.removeCallbacks(mHideRightSettingGroupRun);
                mHandler.postDelayed(mHideRightSettingGroupRun, App.LIVE_UI_SHOW_TIME);
                if (position < 0) return;
                selectRightSettingGroup(0, position);
            }
        });
        tvRightSettingGroupLayout.setVisibility(View.INVISIBLE);


        liveRightSettingItemAdapter = new LiveSettingItemAdapter();
        initTvRecyclerView(mRightSettingItemView, liveRightSettingItemAdapter, mHideRightSettingItemRun, LinearLayout.VERTICAL);
        mRightSettingItemView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                mHandler.removeCallbacks(mHideRightSettingItemRun);
                mHandler.postDelayed(mHideRightSettingItemRun, App.LIVE_UI_SHOW_TIME);
                if (position < 0) return;
                liveRightSettingItemAdapter.setFocusedItemIndex(position);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {
            }
        });
        liveRightSettingItemAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                mHandler.removeCallbacks(mHideRightSettingItemRun);
                mHandler.postDelayed(mHideRightSettingItemRun, App.LIVE_UI_SHOW_TIME);
                FastClickCheckUtil.check(view);
                if (position < 0) return;
                selectRightSettingGroup(1, position);

            }
        });
        tvRightSettingItemLayout.setVisibility(View.INVISIBLE);

        Button button = findViewById(R.id.settingRightItemExit);
        button.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mHandler.removeCallbacks(mHideRightSettingItemRun);
                    mHandler.postDelayed(mHideRightSettingItemRun, App.LIVE_UI_SHOW_TIME);
                    liveRightSettingItemAdapter.setFocusedItemIndex(-1);
                    button.setTextColor(mContext.getResources().getColor(R.color.color_0E0E0E_90));
                } else {
                    button.setTextColor(Color.WHITE);
                }
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRightSettingGroupView.setVisibility(View.VISIBLE);
                mHandler.post(mHideRightSettingItemRun);
                mHandler.removeCallbacks(mHideRightSettingGroupRun);
                mHandler.postDelayed(mHideRightSettingGroupRun, App.LIVE_UI_SHOW_TIME);
            }
        });
    }

    private void showSettingView() {
        if (tvChannelLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideChannelRun);
            mHandler.post(mHideChannelRun);
        }
        if (tvEpgLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideEpgRun);
            mHandler.post(mHideEpgRun);
        }
        if (tvSettingItemLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideSettingItemRun);
            mHandler.post(mHideSettingItemRun);
            return;
        }
        if (tvRightSettingGroupLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideRightSettingItemRun);
            mHandler.post(mHideRightSettingItemRun);
            mHandler.removeCallbacks(mHideRightSettingGroupRun);
            mHandler.post(mHideRightSettingGroupRun);
            return;
        }
        if (tvBackLayout.getVisibility() == View.VISIBLE) {
            showProgressBar(false);
            return;
        }
        if (tvSettingLayout.getVisibility() != View.VISIBLE) {
            epgConfig.setEpgSelectedChannel(getCurrentLiveChannelItem());
            showEpg();
            showInfo();
            liveSettingGroupAdapter.setFocusedGroupIndex(-1);
            liveSettingGroupAdapter.setSelectedGroupIndex(-1);
            mSettingGroupView.scrollToPosition(0);
            mSettingGroupView.setSelection(0);
            LinearLayoutManager layoutManager = (LinearLayoutManager) mSettingGroupView.getLayoutManager();
            if (layoutManager != null) {
                layoutManager.scrollToPositionWithOffset(0, 0);
            }
            mSettingGroupView.setSelection(0);
            mHandler.postDelayed(mShowSettingGroupRun, 200);
        } else {
            mHandler.removeCallbacks(mHideSettingGroupRun);
            mHandler.post(mHideSettingGroupRun);
        }
    }

    private final Runnable mShowSettingGroupRun = new Runnable() {
        @Override
        public void run() {
            if (mSettingGroupView.isScrolling() || mSettingGroupView.isComputingLayout()) {
                mHandler.postDelayed(this, 100);
            } else {
                RecyclerView.ViewHolder holder = mSettingGroupView.findViewHolderForAdapterPosition(0);
                if (holder != null)
                    holder.itemView.requestFocus();
                tvSettingLayout.setVisibility(View.VISIBLE);
                tvSettingLayout.requestLayout();
                showProgressBar(false);
                tvTime.setVisibility(View.GONE);
                tvSpeed.setVisibility(View.GONE);
                mHandler.removeCallbacks(mHideChannelNameRun);
                mHandler.post(mHideChannelNameRun);
                mHandler.removeCallbacks(mHideSettingGroupRun);
                mHandler.postDelayed(mHideSettingGroupRun, App.LIVE_UI_SHOW_TIME);
            }
        }
    };

    private final Runnable mHideSettingGroupRun = new Runnable() {
        @Override
        public void run() {
            tvSettingLayout.setVisibility(View.GONE);
            showTime();
            showSpeed();
        }
    };

    private final Runnable mShowSettingItemRun = new Runnable() {
        @Override
        public void run() {
            if (mSettingMenuView.isScrolling() || mSettingMenuView.isComputingLayout()) {
                mHandler.postDelayed(this, 100);
            } else {
                tvSettingItemLayout.setVisibility(View.VISIBLE);
                tvSettingItemLayout.requestLayout();
                int index = liveSettingGroupAdapter.getSelectedGroupIndex();
                RecyclerView.ViewHolder holder = null;
                switch (index) {
                    case 0:
                        holder = mSettingMenuView.findViewHolderForAdapterPosition(liveSettingItemEpgAdapter.getSelectedIndex());
                    case 1:
                        holder = mSettingMenuView.findViewHolderForAdapterPosition(liveSettingItemSourceAdapter.getSelectedItemIndex());
                    case 2:
                        holder = mSettingMenuView.findViewHolderForAdapterPosition(liveSettingItemScaleAdapter.getSelectedItemIndex());
                        break;
                }
                if (holder != null)
                    holder.itemView.requestFocus();
                mHandler.removeCallbacks(mHideSettingItemRun);
                mHandler.postDelayed(mHideSettingItemRun, App.LIVE_UI_SHOW_TIME);
            }
        }
    };

    private final Runnable mHideSettingItemRun = new Runnable() {
        @Override
        public void run() {
            tvSettingItemLayout.setVisibility(View.GONE);
        }
    };

    private final Runnable mShowRightSettingGroupRun = new Runnable() {
        @Override
        public void run() {
            if (mRightSettingGroupView.isScrolling() || mRightSettingGroupView.isComputingLayout()) {
                mHandler.postDelayed(this, 100);
            } else {
                RecyclerView.ViewHolder holder = mRightSettingGroupView.findViewHolderForAdapterPosition(0);
                if (holder != null)
                    holder.itemView.requestFocus();
                mHandler.removeCallbacks(mHideChannelNameRun);
                mHandler.post(mHideChannelNameRun);
                tvTime.setVisibility(View.GONE);
                tvSpeed.setVisibility(View.GONE);
                mRightSettingGroupView.setVisibility(View.VISIBLE);
                tvRightSettingGroupLayout.setVisibility(View.VISIBLE);
                tvRightSettingGroupLayout.requestLayout();
                ViewObj viewObj = new ViewObj(tvRightSettingGroupLayout, (ViewGroup.MarginLayoutParams) tvRightSettingGroupLayout.getLayoutParams());
                ObjectAnimator animator = ObjectAnimator.ofObject(viewObj, "marginRight", new IntEvaluator(), -tvRightSettingGroupLayout.getLayoutParams().width, 0);
                animator.setDuration(200);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        tvRightSettingGroupLayout.clearAnimation();
                        mHandler.removeCallbacks(mHideRightSettingGroupRun);
                        mHandler.postDelayed(mHideRightSettingGroupRun, App.LIVE_UI_SHOW_TIME);
                    }
                });
                animator.start();
            }
        }
    };

    private final Runnable mHideRightSettingGroupRun = new Runnable() {
        @Override
        public void run() {
            if (tvRightSettingGroupLayout.getVisibility() == View.VISIBLE) {
                tvRightSettingGroupLayout.setVisibility(View.GONE);
                liveRightSettingGroupAdapter.setSelectedGroupIndex(-1);
                showTime();
                showSpeed();
            }
        }
    };

    private final Runnable mShowRightSettingItemRun = new Runnable() {
        @Override
        public void run() {
            if (mRightSettingItemView.isScrolling() || mRightSettingItemView.isComputingLayout()) {
                mHandler.postDelayed(this, 100);
            } else {
                RecyclerView.ViewHolder holder = mRightSettingItemView.findViewHolderForAdapterPosition(liveRightSettingItemAdapter.getSelectedItemIndex());
                if (holder != null)
                    holder.itemView.requestFocus();
                tvRightSettingItemLayout.setVisibility(View.VISIBLE);
                tvRightSettingItemLayout.requestLayout();
                mHandler.removeCallbacks(mHideRightSettingGroupRun);
                mHandler.removeCallbacks(mHideRightSettingItemRun);
                mHandler.postDelayed(mHideRightSettingItemRun, App.LIVE_UI_SHOW_TIME);
            }
        }
    };

    private final Runnable mHideRightSettingItemRun = new Runnable() {
        @Override
        public void run() {
            if (tvRightSettingItemLayout.getVisibility() == View.VISIBLE) {
                tvRightSettingItemLayout.setVisibility(View.GONE);
            }
        }
    };

    //设置 点击事件
    private void selectSettingGroup(int group, int position, boolean focus) {
        switch (group) {
            case 0:
                //设置 操作
                LiveSettingGroup liveSettingGroup = SettingConfig.get().getLiveSettingGroupList().get(position);
                String groupName = liveSettingGroup.getGroupName();
                settingItemName.setText(groupName);
                liveSettingGroupAdapter.setSelectedGroupIndex(position);
                if (tvSettingLayout.getVisibility() == View.VISIBLE) {
                    mHandler.removeCallbacks(mHideSettingGroupRun);
                    mHandler.post(mHideSettingGroupRun);
                }
                int valIndex = 0;
                switch (position) {
                    case 0:
                        if (App.LIVE_SHOW_EPG) {
                            LiveEpgGroup liveEpgGroup = epgConfig.getLiveEpg(getCurrentLiveChannelItem(), TimeUtil.getTime());
                            List<LiveEpgItem> epgItems = new ArrayList<>();
                            if (liveEpgGroup != null) {
                                epgItems = liveEpgGroup.getEpgItems();
                            }
                            if (!epgItems.isEmpty()) {
                                mSettingMenuView.setVisibility(View.VISIBLE);
                                liveSettingItemEpgAdapter.setLiveEpgItem(null);
                                liveSettingItemEpgAdapter.setFocusedIndex(-1);
                                mSettingMenuView.setAdapter(liveSettingItemEpgAdapter);
                                liveSettingItemEpgAdapter.setNewData(epgItems);
                                Date time = new Date();
                                for (LiveEpgItem epgItem : epgItems) {
                                    if (time.compareTo(TimeUtil.getEpgTime(epgItem.currentEpgDate + epgItem.start)) > 0 &&
                                            time.compareTo(TimeUtil.getEpgTime(epgItem.currentEpgDate + epgItem.end)) < 0) {
                                        liveSettingItemEpgAdapter.setSelectedIndex(epgItem.index);
                                        valIndex = epgItem.index;
                                    }
                                }
                            } else {
                                mSettingMenuView.setVisibility(View.INVISIBLE);
                            }
                        } else {
                            mSettingMenuView.setVisibility(View.INVISIBLE);
                        }
                        break;
                    case 1:
                        mSettingMenuView.setVisibility(View.VISIBLE);
                        ArrayList<String> currentSourceNames = getCurrentLiveChannelItem().getChannelSourceNames();
                        ArrayList<LiveSettingItem> liveSettingItemList = new ArrayList<>();
                        for (int j = 0; j < currentSourceNames.size(); j++) {
                            LiveSettingItem liveSettingItem = new LiveSettingItem();
                            liveSettingItem.setItemIndex(j);
                            liveSettingItem.setItemName(currentSourceNames.get(j));
                            liveSettingItemList.add(liveSettingItem);
                        }
                        int sourceIndex = getCurrentLiveChannelItem().getSourceIndex();
                        mSettingMenuView.setAdapter(liveSettingItemSourceAdapter);
                        liveSettingItemSourceAdapter.setFocusedItemIndex(-1);
                        liveSettingItemSourceAdapter.setNewData(liveSettingItemList);
                        liveSettingItemSourceAdapter.selectItem(sourceIndex, true, true);
                        break;
                    case 2:
                        mSettingMenuView.setVisibility(View.VISIBLE);
                        int livePlayerScale = livePlayerManager.getLivePlayerScale();
                        mSettingMenuView.setAdapter(liveSettingItemScaleAdapter);
                        liveSettingItemScaleAdapter.setFocusedItemIndex(-1);
                        liveSettingItemScaleAdapter.setNewData(SettingConfig.get().getLiveSettingGroupList().get(2).getLiveSettingItems());
                        liveSettingItemScaleAdapter.selectItem(livePlayerScale, true, true);
                        break;
                    case 3:
                        settingConfig.loadSettings();
                        liveRightSettingGroupAdapter.setFocusedGroupIndex(-1);
                        liveRightSettingGroupAdapter.setNewData(SettingConfig.get().getLiveSettingGroupMoreList());
                        mRightSettingGroupView.scrollToPosition(0);
                        LinearLayoutManager viewLayoutManager = (LinearLayoutManager) mRightSettingGroupView.getLayoutManager();
                        if (viewLayoutManager != null) {
                            viewLayoutManager.scrollToPositionWithOffset(0, 0);
                        }
                        mRightSettingGroupView.setSelection(0);
                        mHandler.postDelayed(mShowRightSettingGroupRun, 200);
                        break;
                }
                if (position < 3) {
                    mSettingMenuView.scrollToPosition(valIndex);
                    LinearLayoutManager mChannelSourceMenuLayoutManager = (LinearLayoutManager) mSettingMenuView.getLayoutManager();
                    if (mChannelSourceMenuLayoutManager != null) {
                        mChannelSourceMenuLayoutManager.scrollToPositionWithOffset(valIndex, 0);
                    }
                    mSettingMenuView.setSelection(valIndex);
                    mHandler.postDelayed(mShowSettingItemRun, 200);
                }
                break;
            case 1:
                //设置 子项操作
                int index = liveSettingGroupAdapter.getSelectedGroupIndex();
                if (index < 0) return;
                switch (index) {
                    case 0:
                        if (focus) {
                            liveSettingItemEpgAdapter.setFocusedIndex(position);
                        } else {
                            LiveEpgItem epgItem = liveSettingItemEpgAdapter.getItem(position);
                            if (selectedEpgItem == epgItem) return;
                            if (epgItem == null) {
                                return;
                            }
                            playBack(epgItem);
                        }
                        break;
                    case 1:
                        if (focus) {
                            liveSettingItemSourceAdapter.setFocusedItemIndex(position);
                        } else {
                            if (tvBack.getVisibility() == View.VISIBLE) {
                                Toast.makeText(App.getInstance(), "回放中 选中无效", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (liveSettingItemSourceAdapter.getSelectedItemIndex() == position) return;
                            getCurrentLiveChannelItem().setSourceIndex(position);
                            playChannel(getCurrentChannelGroupIndex(), getCurrentLiveChannelIndex(), true);
                            liveSettingItemSourceAdapter.selectItem(position, true, true);
                        }
                        break;
                    case 2:
                        if (focus) {
                            liveSettingItemScaleAdapter.setFocusedItemIndex(position);
                        } else {
                            if (tvBack.getVisibility() == View.VISIBLE) {
                                mVideoView.setScreenScaleType(position);
                                return;
                            }
                            if (liveSettingItemScaleAdapter.getSelectedItemIndex() == position) return;
                            LiveChannelItem liveChannelItem = getCurrentLiveChannelItem();
                            livePlayerManager.changeLivePlayerScale(mVideoView, position, getCurrentChannelGroupIndex() + liveChannelItem.getChannelName() + liveChannelItem.getSourceIndex());
                            liveSettingItemScaleAdapter.selectItem(position, true, true);
                        }
                        break;
                    case 3:
                        break;
                }
                break;
        }
    }

    private void selectRightSettingGroup(int group, int position) {
        switch (group) {
            case 0:
                TextView settingRightItemName = findViewById(R.id.settingRightItemName);
                switch (position) {
                    case 0:
                        Boolean select = !Hawk.get(HawkConfig.LIVE_SHOW_TIME, false);
                        Hawk.put(HawkConfig.LIVE_SHOW_TIME, select);
                        liveRightSettingGroupAdapter.getItem(position).setSelect(select);
                        break;
                    case 1:
                        Boolean select2 = !Hawk.get(HawkConfig.LIVE_SHOW_SPEED, false);
                        Hawk.put(HawkConfig.LIVE_SHOW_SPEED, select2);
                        liveRightSettingGroupAdapter.getItem(position).setSelect(select2);
                        break;
                    case 2:
                        Boolean select3 = !Hawk.get(HawkConfig.LIVE_SHOW_EPG, false);
                        Hawk.put(HawkConfig.LIVE_SHOW_EPG, select3);
                        liveRightSettingGroupAdapter.getItem(position).setSelect(select3);
                        Toast.makeText(App.getInstance(), "重启生效", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        settingRightItemName.setText(liveRightSettingGroupAdapter.getItem(position).getGroupName());
                        liveRightSettingItemAdapter.setNewData(liveRightSettingGroupAdapter.getItem(position).getLiveSettingItems());
                        liveRightSettingItemAdapter.selectItem(settingConfig.getUiTimeIndex(), true, false);
                        mRightSettingItemView.scrollToPosition(0);
                        LinearLayoutManager viewLayoutManager = (LinearLayoutManager) mRightSettingItemView.getLayoutManager();
                        if (viewLayoutManager != null) {
                            viewLayoutManager.scrollToPositionWithOffset(0, 0);
                        }
                        mRightSettingItemView.setSelection(0);
                        mRightSettingGroupView.setVisibility(View.INVISIBLE);
                        mHandler.postDelayed(mShowRightSettingItemRun, 200);
                        break;
                    case 4:
                        settingRightItemName.setText(liveRightSettingGroupAdapter.getItem(position).getGroupName());
                        liveRightSettingItemAdapter.setNewData(liveRightSettingGroupAdapter.getItem(position).getLiveSettingItems());
                        liveRightSettingItemAdapter.selectItem(settingConfig.getCanTimeIndex(), true, false);
                        mRightSettingItemView.scrollToPosition(0);
                        LinearLayoutManager manager = (LinearLayoutManager) mRightSettingItemView.getLayoutManager();
                        if (manager != null) {
                            manager.scrollToPositionWithOffset(0, 0);
                        }
                        mRightSettingItemView.setSelection(0);
                        mRightSettingGroupView.setVisibility(View.INVISIBLE);
                        mHandler.postDelayed(mShowRightSettingItemRun, 200);
                        break;
                    case 5:
                        App.getInstance().cleanParams();
                        settingConfig.reSet();
                        Toast.makeText(App.getInstance(), "缓存清理完成", Toast.LENGTH_SHORT).show();
                        mHandler.removeCallbacks(mHideRightSettingGroupRun);
                        mHandler.post(mHideRightSettingGroupRun);
                        break;
                    default:
                        break;
                }
                liveRightSettingGroupAdapter.setSelectedGroupIndex(position);
                break;
            case 1:
                int index = liveRightSettingGroupAdapter.getSelectedGroupIndex();
                if (index < 0) return;
                liveRightSettingItemAdapter.selectItem(position, true, true);
                switch (index) {
                    case 3:
                        settingConfig.setUiTime(position);
                        liveRightSettingGroupAdapter.getItem(index).setVal(liveRightSettingGroupAdapter.getItem(index).getLiveSettingItems().get(position).getItemName());
                        break;
                    case 4:
                        settingConfig.setCanTime(position);
                        liveRightSettingGroupAdapter.getItem(index).setVal(liveRightSettingGroupAdapter.getItem(index).getLiveSettingItems().get(position).getItemName());
                        break;
                }
                break;
        }
    }


    //功能显示
    private void showEpg() {
        ((TextView) findViewById(R.id.tv_channel_bar_name)).setText(getCurrentLiveChannelItem().getChannelName());//底部名称
        TextView tip_time1 = findViewById(R.id.tv_current_program_time);//底部EPG当前节目信息
        TextView tip_time2 = findViewById(R.id.tv_next_program_time);//底部EPG当下个节目信息
        TextView tip_name1 = findViewById(R.id.tv_current_program_name);//底部EPG当前节目信息
        TextView tip_name2 = findViewById(R.id.tv_next_program_name);//底部EPG当前节目信息
        if (App.LIVE_SHOW_EPG) {
            Map<String, LiveEpgItem> liveEpgItemForMap = epgConfig.getLiveEpgItemForMap(getCurrentLiveChannelItem());
            LiveEpgItem liveEpgItem = liveEpgItemForMap.get("c");
            if (liveEpgItem != null) {
                tip_time1.setText(liveEpgItem.getStart() + "-" + liveEpgItem.getEnd());
                tip_name1.setText(liveEpgItem.getTitle());
            } else {
                tip_time1.setText("00:00-00:00");
                tip_name1.setText("暂无预告");
            }
            LiveEpgItem liveEpgItem1 = liveEpgItemForMap.get("n");
            if (liveEpgItem1 != null) {
                tip_time2.setText(String.format("%s-%s", liveEpgItem1.getStart(), liveEpgItem1.getEnd()));
                tip_name2.setText(liveEpgItem1.getTitle());
            } else {
                tip_time2.setText("00:00-00:00");
                tip_name2.setText("暂无预告");
            }
            if (liveEpgItem == liveEpgItem1) {
                tip_time1.setText("ʚ ɞ");
                tip_name1.setText("");
            }
        } else {
            tip_time1.setText("ʚ ɞ");
            tip_name1.setText("");
            tip_time2.setText("00:00-23:59");
            tip_name2.setText("暂无预告");
        }

    }

    private void showInfo() {
        TextView tvSName = findViewById(R.id.tvSName);
        TextView tvSTimeDate = findViewById(R.id.tvSTimeDate);
        tvSName.setText(String.format("%d", getCurrentLiveChannelItem().getChannelNum()));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd");
        tvSTimeDate.setText(String.format("%s %s", simpleDateFormat.format(new Date()), TimeUtil.getWeek()));

    }

    private void showTime() {
        if (Hawk.get(HawkConfig.LIVE_SHOW_TIME, false)) {
            tvTime.setVisibility(View.VISIBLE);
        } else {
            tvTime.setVisibility(View.GONE);
        }
    }

    private void showSpeed() {
        if (Hawk.get(HawkConfig.LIVE_SHOW_SPEED, false)) {
            tvSpeed.setVisibility(View.VISIBLE);
        } else {
            tvSpeed.setVisibility(View.GONE);
        }
    }

    private final Runnable mUpdateTimeRun = new Runnable() {
        @Override
        public void run() {
            Date day = new Date();
            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
            String format = df.format(day);
            tvTime.setText(format);
            tvSTime.setText(format);
            mHandler.postDelayed(this, 1000);
        }
    };

    private final Runnable mUpdateSpeedRun = new Runnable() {
        @Override
        public void run() {
            tvSpeed.setText(PlayerHelper.getDisplaySpeed(mVideoView.getTcpSpeed()));
            mHandler.postDelayed(this, 1000);
        }
    };


    //进度条
    private void initTvSBar() {
        TvSBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    return;
                }
                if (tvBackLayout.getVisibility() == View.VISIBLE) {
                    tv_position.setText(PlayerUtils.stringForTime(progress));
                } else {
                    long duration = mVideoView.getDuration();
                    long newPosition = (duration * progress) / TvSBar.getMax();
                    tv_position.setText(PlayerUtils.stringForTime((int) newPosition));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mIsDragging = true;
                countDownTimer.cancel();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mIsDragging = false;
                if (tvBack.getVisibility() == View.VISIBLE) {
                    playBackOnSeeBar(seekBar.getProgress());
                } else {
                    long duration = mVideoView.getDuration();
                    long newPosition = (duration * seekBar.getProgress()) / seekBar.getMax();
                    mVideoView.seekTo((int) newPosition);
                }
                countDownTimer.start();

            }
        });
        TvSBar.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View arg0, int keycode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keycode == KeyEvent.KEYCODE_DPAD_LEFT || keycode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        mIsDragging = true;
                        countDownTimer.cancel();
                    }
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    if (keycode == KeyEvent.KEYCODE_DPAD_LEFT || keycode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        mIsDragging = false;
                        countDownTimer.start();
                        if (tvBack.getVisibility() == View.VISIBLE) {
                            playBackOnSeeBar(TvSBar.getProgress());
                        } else {
                            long duration = mVideoView.getDuration();
                            long newPosition = (duration * TvSBar.getProgress()) / TvSBar.getMax();
                            mVideoView.seekTo((int) newPosition);
                        }
                    }
                }
                return false;
            }
        });
    }

    private CountDownTimer countDownTimer;

    public void showProgressBar(boolean show) {
        if (show) {
            if (tvBackLayout.getVisibility() == View.VISIBLE) {
                countDownTimer.cancel();
                countDownTimer.start();
            }
            tvBackLayout.setVisibility(View.VISIBLE);
            TvSBar.requestFocus();
            if (tvBack.getVisibility() != View.VISIBLE) {
                TvSBar.setMax((int) mVideoView.getDuration());
                TvSBar.setKeyProgressIncrement(10000);
            }
            tv_position.setText(PlayerUtils.stringForTime((int) mVideoView.getCurrentPosition() + selectTime));
            tv_duration.setText(PlayerUtils.stringForTime((int) mVideoView.getDuration() + selectTime));
            if (countDownTimer == null) {
                countDownTimer = new CountDownTimer(App.LIVE_UI_SHOW_TIME, 1000) {
                    @Override
                    public void onTick(long arg0) {
                        if (mVideoView != null && mVideoView.isPlaying() && tvBackLayout.getVisibility() == View.VISIBLE) {
                            TvSBar.setProgress((int) mVideoView.getCurrentPosition() + selectTime);
                            tv_position.setText(PlayerUtils.stringForTime((int) mVideoView.getCurrentPosition() + selectTime));
                        }
                    }

                    @Override
                    public void onFinish() {
                        if (tvBackLayout.getVisibility() == View.VISIBLE) {
                            tvBackLayout.setVisibility(View.GONE);
                        }
                    }
                };
            } else {
                countDownTimer.cancel();
            }
            countDownTimer.start();
        } else {
            if (countDownTimer != null)
                countDownTimer.cancel();
            tvBackLayout.setVisibility(View.GONE);
        }
    }


    //预告回放
    private void initEpgGroupView() {
        liveEpgChannelAdapter = new LiveEpgChannelAdapter();
        liveEpgChannelAdapter.setNewData(liveConfig.getLiveChannelList());
        initTvRecyclerView(mEpgChannelView, liveEpgChannelAdapter, mHideEpgRun, LinearLayout.VERTICAL);
        mEpgChannelView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                setSelectedEpgChannel(0, position, true);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {

            }
        });
        liveEpgChannelAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                setSelectedEpgChannel(0, position, false);
            }
        });

        liveEpgDateAdapter = new LiveEpgDateAdapter();
        initTvRecyclerView(mEpgDateView, liveEpgDateAdapter, mHideEpgRun, LinearLayout.VERTICAL);
        mEpgDateView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                setSelectedEpgChannel(1, position, true);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {

            }
        });
        liveEpgDateAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                setSelectedEpgChannel(1, position, false);
            }
        });

        liveEpgItemAdapter = new LiveEpgItemAdapter();
        initTvRecyclerView(mEpgItemView, liveEpgItemAdapter, mHideEpgRun, LinearLayout.VERTICAL);
        mEpgItemView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {

            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                setSelectedEpgChannel(2, position, true);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {

            }
        });
        liveEpgItemAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                setSelectedEpgChannel(2, position, false);
            }
        });

        tvEpgLayout.setVisibility(View.INVISIBLE);
    }

    private void setSelectedEpgChannel(int group, int position, boolean focus) {
        mHandler.removeCallbacks(mHideEpgRun);
        mHandler.postDelayed(mHideEpgRun, App.LIVE_UI_SHOW_TIME);
        switch (group) {
            case 0:
                if (focus) {
                    liveEpgChannelAdapter.setFocusedChannelIndex(position);
                    liveEpgDateAdapter.setFocusedIndex(-1);
                }
                if (position == liveEpgChannelAdapter.getSelectedChannelIndex() || position < -1)
                    return;
                liveEpgChannelAdapter.setSelectedChannelIndex(position);
                epgConfig.setEpgSelectedChannelPos(position);
                mHandler.removeCallbacks(changeChannelEpgRun);
                mHandler.postDelayed(changeChannelEpgRun, 100);
                break;
            case 1:
                if (focus) {
                    liveEpgChannelAdapter.setFocusedChannelIndex(-1);
                    liveEpgDateAdapter.setFocusedIndex(position);
                    liveEpgItemAdapter.setFocusedIndex(-1);
                }
                if (position == liveEpgDateAdapter.getSelectedIndex() || position < -1)
                    return;
                liveEpgDateAdapter.setSelectedIndex(position);
                String format = TimeUtil.timeFormat.format(liveEpgDateAdapter.getData().get(position).getDateParamVal());
                changeEpg(epgConfig.getEpgSelectedChannel(), format);
                break;
            case 2:
                if (focus) {
                    liveEpgItemAdapter.setFocusedIndex(position);
                    liveEpgDateAdapter.setFocusedIndex(-1);
                    return;
                }
                if (position == liveEpgItemAdapter.getSelectedIndex() || position < -1)
                    return;
                LiveEpgItem epgItem = liveEpgItemAdapter.getItem(position);
                if (epgItem == null) {
                    return;
                }
                playBack(epgItem);
                break;
        }
    }

    private final Runnable changeChannelEpgRun = new Runnable() {
        @Override
        public void run() {
            changeChannelEpg(epgConfig.getEpgSelectedChannelPos());
        }
    };

    private void changeChannelEpg(int selectedPos) {
        if (selectedPos < 0) return;
        epgConfig.setEpgSelectedChannel(liveConfig.getLiveChannelList().get(selectedPos));
        List<LiveEpgDate> epgDateList = epgConfig.getEpgDateList();
        int pos = epgDateList.size() - 2;
        LiveChannelItem epgSelectedChannel = epgConfig.getEpgSelectedChannel();
        LiveChannelItem epgBackChannel = epgConfig.getEpgBackChannel();
        if (selectedEpgItem != null && epgBackChannel != null && epgBackChannel.getChannelNum() == epgSelectedChannel.getChannelNum()) {
            for (int i = 0; i < epgDateList.size(); i++) {
                if (TimeUtil.timeFormat.format(epgDateList.get(i).getDateParamVal()).equals(selectedEpgItem.currentEpgDate)) {
                    pos = i;
                }
            }
        }
        liveEpgDateAdapter.setSelectedIndex(pos);
        liveEpgDateAdapter.setFocusedIndex(-1);
        mEpgDateView.setSelectedPosition(pos);
        liveEpgItemAdapter.setCanBack(StringUtils.isNotEmpty(epgSelectedChannel.getSocUrls()));
        String format = TimeUtil.timeFormat.format(epgDateList.get(pos).getDateParamVal());
        changeEpg(epgSelectedChannel, format);
    }

    private void changeEpg(LiveChannelItem liveChannelItem, String date) {
        LiveEpgGroup liveEpgGroup = epgConfig.getLiveEpg(liveChannelItem, date);
        Log.i(TAG, "changeEpg: " + liveEpgGroup);
        if (liveEpgGroup == null) {
            mEpgItemView.setVisibility(View.GONE);
            no_epg.setVisibility(View.VISIBLE);
            return;
        } else {
            mEpgItemView.setVisibility(View.VISIBLE);
            no_epg.setVisibility(View.GONE);
        }
        List<LiveEpgItem> epgItems = liveEpgGroup.getEpgItems();
        liveEpgItemAdapter.setFocusedIndex(-1);
        liveEpgItemAdapter.setNewData(epgItems);
        LiveChannelItem epgBackChannel = epgConfig.getEpgBackChannel();
        if (selectedEpgItem != null && selectedEpgItem.currentEpgDate.equals(date) && epgBackChannel != null && epgBackChannel.getChannelNum() == liveChannelItem.getChannelNum()) {
            liveEpgItemAdapter.setSelectedIndex(selectedEpgItem.index);
            mEpgItemView.scrollToPosition(selectedEpgItem.index);
            LinearLayoutManager layoutManager = (LinearLayoutManager) mEpgItemView.getLayoutManager();
            if (layoutManager != null) {
                layoutManager.scrollToPositionWithOffset(selectedEpgItem.index, 0);
            }
            mEpgItemView.setSelectedPosition(selectedEpgItem.index);
            return;
        }
        if (date.equals(TimeUtil.getTime())) {
            Date time = new Date();
            for (LiveEpgItem epgItem : epgItems) {
                if (time.compareTo(TimeUtil.getEpgTime(epgItem.currentEpgDate + epgItem.start)) > 0 && time.compareTo(TimeUtil.getEpgTime(epgItem.currentEpgDate + epgItem.end)) < 0) {
                    liveEpgItemAdapter.setSelectedIndex(epgItem.index);
                    mEpgItemView.scrollToPosition(epgItem.index);
                    LinearLayoutManager layoutManager = (LinearLayoutManager) mEpgItemView.getLayoutManager();
                    if (layoutManager != null) {
                        layoutManager.scrollToPositionWithOffset(epgItem.index, 0);
                    }
                    mEpgItemView.setSelectedPosition(epgItem.index);
                    return;
                }
            }
        } else {
            liveEpgItemAdapter.setSelectedIndex(-1);
            mEpgItemView.scrollToPosition(0);
            LinearLayoutManager layoutManager = (LinearLayoutManager) mEpgItemView.getLayoutManager();
            if (layoutManager != null) {
                layoutManager.scrollToPositionWithOffset(0, 0);
            }
            mEpgItemView.setSelectedPosition(0);
        }
    }

    private void showEpgView() {
        if (App.LIVE_SHOW_EPG && !isListOrSettingLayoutVisible()) {
            LiveChannelItem epgBackChannel = epgConfig.getEpgBackChannel();
            if (epgBackChannel != null) {
                epgConfig.setEpgSelectedChannel(epgBackChannel);
            } else {
                epgConfig.setEpgSelectedChannel(getCurrentLiveChannelItem());
            }
            int pos = epgConfig.getEpgSelectedChannel().getChannelNum() - 1;
            liveEpgChannelAdapter.setNewData(liveConfig.getLiveChannelList());
            mEpgChannelView.scrollToPosition(pos);
            LinearLayoutManager layoutManager = (LinearLayoutManager) mEpgChannelView.getLayoutManager();
            if (layoutManager != null) {
                layoutManager.scrollToPositionWithOffset(pos, 0);
            }
            liveEpgChannelAdapter.setSelectedChannelIndex(pos);
            mEpgChannelView.setSelection(pos);
            List<LiveEpgDate> epgDateList = epgConfig.getEpgDateList();
            liveEpgDateAdapter.setNewData(epgDateList);
            changeChannelEpg(pos);
            mHandler.postDelayed(mShowEpgRun, 200);
        }
    }

    private final Runnable mShowEpgRun = new Runnable() {
        @Override
        public void run() {
            if (mEpgChannelView.isComputingLayout() || mEpgDateView.isComputingLayout() || mEpgItemView.isComputingLayout() || mEpgChannelView.isScrolling()
                    || mEpgDateView.isScrolling() || mEpgItemView.isScrolling()) {
                mHandler.postDelayed(this, 100);
            } else {
                RecyclerView.ViewHolder holder = mEpgChannelView.findViewHolderForAdapterPosition(liveEpgChannelAdapter.getSelectedChannelIndex());
                if (holder != null)
                    holder.itemView.requestFocus();
                tvEpgLayout.setVisibility(View.VISIBLE);
                tvEpgLayout.requestLayout();
                ViewObj viewObj = new ViewObj(tvEpgLayout, (ViewGroup.MarginLayoutParams) tvEpgLayout.getLayoutParams());
                ObjectAnimator animator = ObjectAnimator.ofObject(viewObj, "marginLeft", new IntEvaluator(), -tvEpgLayout.getLayoutParams().width, 0);
                animator.setDuration(200);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        tvEpgLayout.clearAnimation();
                        mHandler.removeCallbacks(mHideEpgRun);
                        mHandler.postDelayed(mHideEpgRun, App.LIVE_UI_SHOW_TIME);
                    }
                });
                animator.start();
            }
        }
    };

    private final Runnable mHideEpgRun = new Runnable() {
        @Override
        public void run() {
            if (tvEpgLayout.getVisibility() == View.VISIBLE) {
                tvEpgLayout.setVisibility(View.GONE);
            }
        }
    };

    //回放处理
    private final Runnable backChangeRun = new Runnable() {
        @Override
        public void run() {
            if (tvBack.getVisibility() == View.VISIBLE) {
                if (mVideoView != null && mVideoView.isPlaying() && mVideoView.getCurrentPosition() + selectTime >= TvSBar.getMax()) {
                    if (tvBack.getVisibility() == View.VISIBLE && epgConfig.getEpgBackChannel() != null && selectedEpgItem != null) {
                        playBackNext(selectedEpgItem);
                    }
                    return;
                }
                mHandler.postDelayed(this, 3000);
            }
        }
    };

    //进度条回放播放
    private void playBackOnSeeBar(int time) {
        if (time >= TvSBar.getMax()) {
            isCanBack = false;
            mVideoView.release();
            mHandler.removeCallbacks(backChangeRun);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isCanBack = true;
                    playBackNext(selectedEpgItem);
                }
            }, 500);
            return;
        }
        selectTime = time;
        String startDate = selectedEpgItem.currentEpgDate.replaceAll("-", "") + selectedEpgItem.start.replaceAll(":", "") + "00";
        startDate = TimeUtil.getTimeS(startDate, selectTime);
        String endDate = selectedEpgItem.currentEpgDate.replaceAll("-", "") + selectedEpgItem.end.replace(":", "") + "00";
        countDownTimer.cancel();
        countDownTimer.start();
        mVideoView.release();
        mVideoView.setUrl(String.format(epgConfig.getEpgBackChannel().getSocUrls(), TimeUtil.getTimeS(startDate) + "GMT-" + TimeUtil.getTimeS(endDate) + "GMT"));
        mVideoView.start();
    }

    //选中 判断 回放
    private void playBack(LiveEpgItem item) {
        if (epgConfig.isCanPlay(item)) {
            LiveChannelItem epgBackChannel = epgConfig.getEpgSelectedChannel().clone();
            epgConfig.setEpgBackChannel(epgBackChannel);
            liveConfig.setCurrentLiveChannelItem(epgConfig.getEpgSelectedChannel());
            mHandler.removeCallbacks(mHideChannelNameRun);
            mHandler.post(mHideChannelNameRun);
            tvBack.setVisibility(View.VISIBLE);
            playBack(item, true);
        }
    }

    //播放回放
    private void playBack(LiveEpgItem item, boolean updateIndex) {
        showProgressBar(false);
        String startDate = item.currentEpgDate.replaceAll("-", "") + item.start.replace(":", "") + "00";
        String endDate = item.currentEpgDate.replaceAll("-", "") + item.end.replace(":", "") + "00";
        selectedEpgItem = item;
        liveEpgItemAdapter.setLiveEpgItem(item);
        liveSettingItemEpgAdapter.setLiveEpgItem(item);
        if (updateIndex) {
            liveEpgItemAdapter.setSelectedIndex(item.index);
            liveSettingItemEpgAdapter.setSelectedIndex(item.index);
        }
        int maxTime = (int) TimeUtil.getTime(TimeUtil.timeFormat.format(new Date()) + " " + item.start + ":" + "00", TimeUtil.timeFormat.format(new Date()) + " " + item.end + ":" + "00");
        TvSBar.setProgress(0);
        TvSBar.setMax(maxTime * 1000);
        TvSBar.setKeyProgressIncrement(10000);
        isCanBack = true;
        selectTime = 0;
        mHandler.removeCallbacks(mConnectTimeoutChangeSourceRun);
        mHandler.removeCallbacks(backChangeRun);
        mHandler.postDelayed(backChangeRun, 3000);
        mVideoView.release();
        mVideoView.setUrl(String.format(epgConfig.getEpgBackChannel().getSocUrls(), TimeUtil.getTimeS(startDate) + "GMT-" + TimeUtil.getTimeS(endDate) + "GMT"));
        mVideoView.start();
    }

    //播放下一个回放
    private void playBackNext(LiveEpgItem epgItem) {
        LiveChannelItem epgBackChannel = epgConfig.getEpgBackChannel();
        LiveEpgGroup liveEpgGroup = epgConfig.getLiveEpg(epgBackChannel, epgItem.currentEpgDate);
        boolean updateIndex = false;
        Date date = new Date();
        try {
            if (epgConfig.getEpgSelectedChannel().getChannelNum() == epgBackChannel.getChannelNum() &&
                    TimeUtil.timeFormat.format(liveEpgDateAdapter.getItem(liveEpgDateAdapter.getSelectedIndex()).getDateParamVal()).equals(epgItem.currentEpgDate)) {
                updateIndex = true;
            }
        } catch (Exception ignored) {
        }
        for (int i = 0; i < liveEpgGroup.getEpgItems().size(); i++) {
            if (liveEpgGroup.getEpgItems().get(i).equals(epgItem) && i + 1 < liveEpgGroup.getEpgItems().size()) {
                LiveEpgItem item = liveEpgGroup.getEpgItems().get(i + 1);
                Date epgStartTime = TimeUtil.getEpgTime(item.currentEpgDate + item.start);
                if (date.compareTo(epgStartTime) > 0 && date.compareTo(TimeUtil.getEpgTime(item.currentEpgDate + item.end)) < 0) {
                    if (updateIndex) liveEpgItemAdapter.setSelectedIndex(item.index);
                    playChannel(getCurrentChannelGroupIndex(), getCurrentLiveChannelIndex(), false);
                    return;
                }
                playBack(item, updateIndex);
                return;
            }
        }
        if (updateIndex) {
            liveEpgItemAdapter.setSelectedIndex(-1);
            updateIndex = false;
        }
        List<LiveEpgDate> epgDateList = epgConfig.getEpgDateList();
        for (int i = 0; i < epgConfig.getEpgDateList().size(); i++) {
            if (TimeUtil.timeFormat.format(epgDateList.get(i).getDateParamVal()).equals(epgItem.currentEpgDate)) {
                LiveEpgItem item = epgConfig.getLiveEpg(epgBackChannel, TimeUtil.timeFormat.format(epgDateList.get(i + 1).getDateParamVal())).getEpgItems().get(0);
                try {
                    if (epgConfig.getEpgSelectedChannel().getChannelNum() == epgBackChannel.getChannelNum() &&
                            TimeUtil.timeFormat.format(liveEpgDateAdapter.getItem(liveEpgDateAdapter.getSelectedIndex()).getDateParamVal()).equals(item.currentEpgDate)) {
                        updateIndex = true;
                    }
                } catch (Exception ignored) {
                }
                Date epgStartTime = TimeUtil.getEpgTime(item.currentEpgDate + item.start);
                if (date.compareTo(epgStartTime) > 0 && date.compareTo(TimeUtil.getEpgTime(item.currentEpgDate + item.end)) < 0) {
                    if (updateIndex) liveEpgItemAdapter.setSelectedIndex(item.index);
                    playChannel(getCurrentChannelGroupIndex(), getCurrentLiveChannelIndex(), false);
                    return;
                }
                playBack(item, updateIndex);
                return;
            }
        }
        playChannel(getCurrentChannelGroupIndex(), getCurrentLiveChannelIndex(), false);
    }


}