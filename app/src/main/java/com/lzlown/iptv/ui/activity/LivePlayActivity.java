package com.lzlown.iptv.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.IntEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
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

    private final int showUiTime = 5000;

    private LiveConfig liveConfig;
    private EpgConfig epgConfig;
    private SettingConfig settingConfig;

    //频道列表
    private LinearLayout tvLeftChannelListLayout;
    private TvRecyclerView mChannelGroupView;
    private TvRecyclerView mChannelItemView;
    private LiveChannelGroupAdapter liveChannelGroupAdapter;
    private LiveChannelItemAdapter liveChannelItemAdapter;

    //设置列表
    private LinearLayout tvRightSettingLayout;
    private TvRecyclerView mSettingGroupView;
    private TvRecyclerView mSettingItemView;
    private LiveSettingGroupAdapter liveSettingGroupAdapter;
    private LiveSettingItemAdapter liveSettingItemAdapter;

    private long mExitTime = 0;
    private boolean loadEnd = false;

    //中间EPG
    private View centerEpgLayout;

    //右边显示
    private TextView tvName;
    private TextView tvTime;
    private TextView tvSpeed;
    private TextView tvBack;

    //回放显示列表
    private View tvEpgLayout;
    private TvRecyclerView mEpgChannelView;
    private TvRecyclerView mEpgDateView;
    private TvRecyclerView mEpgItemView;
    private LiveEpgChannelItemAdapter liveEpgChannelItemAdapter;
    private LiveEpgItemAdapter liveEpgItemAdapter;
    private LiveEpgDateAdapter liveEpgDateAdapter;
    private TextView no_epg;

    //回放控制
    private View tvBackLayout;
    private SeekBar sBar;
    private TextView tv_position;
    private TextView tv_duration;

    private Boolean isCanBack = false;
    private LiveEpgItem selectedEpgItem;
    private int selectTime = 0;
    private boolean mIsDragging;

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
        liveConfig = LiveConfig.get();
        epgConfig = EpgConfig.get();
        settingConfig = SettingConfig.get();

        loadEnd = false;
        //todo 睡眠处理
        mVideoView.release();
        mVideoView.setUrl(liveConfig.getLiveChannelGroupList().get(0).getLiveChannels().get(0).getUrl());
        mVideoView.start();
        mVideoView.setScreenScaleType(0);

        //界面 view
        tvLeftChannelListLayout = findViewById(R.id.tvLeftChannnelListLayout);
        mChannelGroupView = findViewById(R.id.mGroupGridView);
        mChannelItemView = findViewById(R.id.mChannelGridView);
        tvRightSettingLayout = findViewById(R.id.tvRightSettingLayout);
        mSettingGroupView = findViewById(R.id.mSettingGroupView);
        mSettingItemView = findViewById(R.id.mSettingItemView);
        //中间EPG
        centerEpgLayout = findViewById(R.id.ll_epg);
        centerEpgLayout.setVisibility(View.GONE);
        //右边显示
        tvName = findViewById(R.id.tvName);
        tvTime = findViewById(R.id.tvTime);
        tvSpeed = findViewById(R.id.tvSpeed);

        tvBack = findViewById(R.id.tvBack);

        tvEpgLayout = findViewById(R.id.divEPG);
        tvEpgLayout.setVisibility(View.INVISIBLE);
        mEpgChannelView = findViewById(R.id.mEpgChannelView);
        mEpgDateView = findViewById(R.id.mEpgDateGridView);
        mEpgItemView = findViewById(R.id.lv_epg);
        no_epg = findViewById(R.id.no_epg);

        tvBackLayout = findViewById(R.id.seekbar);
        tvBackLayout.setVisibility(View.INVISIBLE);
        sBar = findViewById(R.id.pb_progressbar);
        tv_position = findViewById(R.id.tv_currentpos);
        tv_duration = findViewById(R.id.tv_duration);
        sBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    return;
                }
                if (tvBackLayout.getVisibility() == View.VISIBLE) {
                    tv_position.setText(PlayerUtils.stringForTime(progress));
                } else {
                    long duration = mVideoView.getDuration();
                    long newPosition = (duration * progress) / sBar.getMax();
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
        sBar.setOnKeyListener(new View.OnKeyListener() {
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
                            playBackOnSeeBar(sBar.getProgress());
                        } else {
                            long duration = mVideoView.getDuration();
                            long newPosition = (duration * sBar.getProgress()) / sBar.getMax();
                            mVideoView.seekTo((int) newPosition);
                        }
                    }
                }
                return false;
            }
        });

        initVideoView();
        initChannelGroupView();
        initSettingGroupView();
        initEpgGroupView();
        initLiveChannelList();

        showTime();
        showSpeed();
        mHandler.post(mUpdateTimeRun);
        mHandler.post(mUpdateSpeedRun);
    }

    @Override
    public void onBackPressed() {
        if (tvLeftChannelListLayout.getVisibility() == View.VISIBLE || tvEpgLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideChannelListRun);
            mHandler.post(mHideChannelListRun);
        } else if (tvBackLayout.getVisibility() == View.VISIBLE) {
            showProgressBar(false);
        } else if (tvRightSettingLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideSettingLayoutRun);
            mHandler.post(mHideSettingLayoutRun);
        } else {
            exit();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int keyCode = event.getKeyCode();
            if (keyCode == KeyEvent.KEYCODE_MENU) {
                showSettingGroup();
            } else if (!isListOrSettingLayoutVisible()) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_UP:
                        if (Hawk.get(HawkConfig.LIVE_CHANNEL_REVERSE, true)) {
                            playNext();
                        } else {
                            playPrevious();
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        if (Hawk.get(HawkConfig.LIVE_CHANNEL_REVERSE, true)) {
                            playPrevious();
                        } else {
                            playNext();
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        showEpgMenu();
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
                        showChannelList();
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
        mHandler.removeCallbacks(mFocusAndShowChannelViewRun);
        mHandler.removeCallbacks(mHideChannelListRun);
        mHandler.removeCallbacks(mHideEpgListRun);
        mHandler.removeCallbacks(mFocusAndShowSettingViewRun);
        mHandler.removeCallbacks(mHideSettingLayoutRun);
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
        return liveConfig.getCurrentChannelGroupIndex();
    }

    private int getCurrentLiveChannelIndex() {
        return liveConfig.getCurrentLiveChannelIndex();
    }

    public LiveChannelItem getCurrentLiveChannelItem() {
        return liveConfig.getCurrentLiveChannelItem();
    }

    private void playChannel(int channelGroupIndex, int liveChannelIndex, boolean changeSource) {
        LiveChannelItem liveChannelItem = liveConfig.getLiveChannels(channelGroupIndex).get(liveChannelIndex);
        if (liveConfig.getCurrentLiveChannelItem() != null && liveConfig.getCurrentLiveChannelItem().equals(liveChannelItem) && !changeSource || (changeSource && liveChannelItem.getSourceNum() == 1)) {
            if (!isCanBack) {
                return;
            }
        }
        if (!changeSource) {
            liveConfig.setCurrentChannelGroupIndex(channelGroupIndex);
            liveConfig.setCurrentLiveChannelIndex(liveChannelIndex);
            liveConfig.setCurrentLiveChannelItem(liveChannelItem);
            liveConfig.setCurrentLiveChannelItem(liveChannelItem);
            Hawk.put(HawkConfig.LIVE_GROUP, channelGroupIndex);
            Hawk.put(HawkConfig.LIVE_CHANNEL, liveChannelItem.getChannelName());
            tvName.setText(String.format("%d %s", liveChannelItem.getChannelNum(), liveChannelItem.getChannelName()));
            tvName.setVisibility(View.VISIBLE);
        }
        tvBack.setVisibility(View.GONE);
        mHandler.removeCallbacks(playChannelRun);
        mHandler.postDelayed(playChannelRun, 100);
    }

    private final Runnable playChannelRun = new Runnable() {
        @Override
        public void run() {
            LiveChannelItem liveChannelItem = getCurrentLiveChannelItem();
            livePlayerManager.getLiveChannelPlayer(mVideoView, getCurrentChannelGroupIndex() + liveChannelItem.getChannelName() + liveChannelItem.getSourceIndex());
            isCanBack = liveChannelItem.getUrl().contains(".mp4");
            selectedEpgItem = null;
            epgConfig.setEpgBackChannel(null);
            if (liveEpgItemAdapter != null) {
                liveEpgItemAdapter.setLiveEpgItem(null);
            }
            selectTime = 0;
            mHandler.removeCallbacks(backChangeRun);
            showProgressBar(false);
//            showEpg();
            mVideoView.release();
            mVideoView.setUrl(liveChannelItem.getUrl());
            mVideoView.start();
        }
    };

    private void playNext() {
        if (!isCurrentLiveChannelValid()) return;
        Integer[] groupChannelIndex = liveConfig.getNextChannel(1);
        playChannel(groupChannelIndex[0], groupChannelIndex[1], false);
    }

    private void playPrevious() {
        if (!isCurrentLiveChannelValid()) return;
        Integer[] groupChannelIndex = liveConfig.getNextChannel(-1);
        playChannel(groupChannelIndex[0], groupChannelIndex[1], false);
    }

    private void initVideoView() {
        LiveController controller = new LiveController(this);
        controller.setListener(new LiveController.LiveControlListener() {
            @Override
            public boolean singleTap() {
                showChannelList();
                return true;
            }

            @Override
            public void longPress() {
                showSettingGroup();
            }

            @Override
            public void playStateChanged(int playState) {
                switch (playState) {
                    case VideoView.STATE_PREPARED:
                    case VideoView.STATE_BUFFERED:
                    case VideoView.STATE_PLAYING:
                        mHandler.removeCallbacks(mConnectTimeoutChangeSourceRun);
                        tvName.setVisibility(View.INVISIBLE);
                        break;
                    case VideoView.STATE_ERROR:
                    case VideoView.STATE_PREPARING:
                    case VideoView.STATE_BUFFERING:
                        mHandler.removeCallbacks(mConnectTimeoutChangeSourceRun);
                        mHandler.postDelayed(mConnectTimeoutChangeSourceRun, (Hawk.get(HawkConfig.LIVE_CONNECT_TIMEOUT, 10)) * 1000);
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
                    showEpgMenu();
                }
            }
        });
        controller.setCanChangePosition(false);
        controller.setEnableInNormal(true);
        controller.setGestureEnabled(true);
        controller.setDoubleTapTogglePlayEnabled(false);
        mVideoView.setVideoController(controller);
        mVideoView.setProgressManager(null);
    }

    private final Runnable mConnectTimeoutChangeSourceRun = new Runnable() {
        @Override
        public void run() {
            if (tvBack.getVisibility() == View.VISIBLE) {
                playChannel(getCurrentChannelGroupIndex(), getCurrentLiveChannelIndex(), false);
                return;
            }
            Integer[] groupChannelIndex = liveConfig.getNextChannel(Hawk.get(HawkConfig.LIVE_CHANNEL_REVERSE, false) ? -1 : 1);
            playChannel(groupChannelIndex[0], groupChannelIndex[1], false);
        }
    };

    private boolean isListOrSettingLayoutVisible() {
        return tvLeftChannelListLayout.getVisibility() == View.VISIBLE ||
                tvRightSettingLayout.getVisibility() == View.VISIBLE ||
                tvEpgLayout.getVisibility() == View.VISIBLE ||
                tvBackLayout.getVisibility() == View.VISIBLE;
    }

    private void initTvRecyclerView(TvRecyclerView view, BaseQuickAdapter adapter, Runnable runnable) {
        view.setHasFixedSize(true);
        view.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));
        adapter.closeLoadAnimation();
        view.setAdapter(adapter);
        view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mHandler.removeCallbacks(runnable);
                mHandler.postDelayed(runnable, showUiTime);
            }
        });
    }

    //左侧节目列表
    private void initChannelGroupView() {
        liveChannelGroupAdapter = new LiveChannelGroupAdapter();
        initTvRecyclerView(mChannelGroupView, liveChannelGroupAdapter, mHideChannelListRun);
        mChannelGroupView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                selectChannelGroup(0,position, true, -1);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {

            }
        });
        liveChannelGroupAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                selectChannelGroup(0,position, false, -1);
            }
        });

        liveChannelItemAdapter = new LiveChannelItemAdapter();
        initTvRecyclerView(mChannelItemView, liveChannelItemAdapter, mHideChannelListRun);
        mChannelItemView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                if (position < 0) return;
                liveChannelGroupAdapter.setFocusedGroupIndex(-1);
                liveChannelItemAdapter.setFocusedChannelIndex(position);
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, showUiTime);

            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {
                selectChannelGroup(1,-1, false, position);
            }
        });
        liveChannelItemAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                selectChannelGroup(1,-1, false, position);
            }
        });
    }

    private void selectChannelGroup(int group,int groupIndex, boolean focus, int liveChannelIndex) {
        switch (group) {
            case 0:
                if (focus) {
                    liveChannelGroupAdapter.setFocusedGroupIndex(groupIndex);
                    liveChannelItemAdapter.setFocusedChannelIndex(-1);
                }
                if ((groupIndex > -1 && groupIndex != liveChannelGroupAdapter.getSelectedGroupIndex())) {
                    liveChannelGroupAdapter.setSelectedGroupIndex(groupIndex);
                    loadChannelGroupDataAndPlay(groupIndex, liveChannelIndex);
                }
                break;
            case 1:
                liveChannelItemAdapter.setSelectedChannelIndex(liveChannelIndex);
                playChannel(liveChannelGroupAdapter.getSelectedGroupIndex(), liveChannelIndex, false);
                break;
        }
        if (tvLeftChannelListLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideChannelListRun);
            mHandler.postDelayed(mHideChannelListRun, showUiTime);
        }
    }

    private final Runnable mFocusAndShowChannelViewRun = new Runnable() {
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
                tvLeftChannelListLayout.setVisibility(View.VISIBLE);
                ViewObj viewObj = new ViewObj(tvLeftChannelListLayout, (ViewGroup.MarginLayoutParams) tvLeftChannelListLayout.getLayoutParams());
                ObjectAnimator animator = ObjectAnimator.ofObject(viewObj, "marginLeft", new IntEvaluator(), -tvLeftChannelListLayout.getLayoutParams().width, 0);
                animator.setDuration(200);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mHandler.removeCallbacks(mHideChannelListRun);
                        mHandler.postDelayed(mHideChannelListRun, showUiTime);
                    }
                });
                animator.start();
            }
        }
    };

    private final Runnable mHideChannelListRun = new Runnable() {
        @Override
        public void run() {
            if (tvLeftChannelListLayout.getVisibility() == View.VISIBLE) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) tvLeftChannelListLayout.getLayoutParams();
                ViewObj viewObj = new ViewObj(tvLeftChannelListLayout, params);
                ObjectAnimator animator = ObjectAnimator.ofObject(viewObj, "marginLeft", new IntEvaluator(), 0, -tvLeftChannelListLayout.getLayoutParams().width);
                animator.setDuration(200);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        tvLeftChannelListLayout.setVisibility(View.INVISIBLE);
                    }
                });
                animator.start();
            }
            if (tvEpgLayout.getVisibility() == View.VISIBLE) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) tvEpgLayout.getLayoutParams();
                ViewObj viewObj = new ViewObj(tvEpgLayout, params);
                ObjectAnimator animator = ObjectAnimator.ofObject(viewObj, "marginLeft", new IntEvaluator(), 0, -tvEpgLayout.getLayoutParams().width);
                animator.setDuration(200);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        tvEpgLayout.setVisibility(View.INVISIBLE);
                    }
                });
                animator.start();
            }
        }
    };

    private void showChannelList() {
        if (tvRightSettingLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideSettingLayoutRun);
            mHandler.post(mHideSettingLayoutRun);
            return;
        }
        if (tvEpgLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideChannelListRun);
            mHandler.post(mHideChannelListRun);
            return;
        }
        if (tvBackLayout.getVisibility() == View.VISIBLE) {
            showProgressBar(false);
            return;
        }
        if (tvLeftChannelListLayout.getVisibility() == View.INVISIBLE) {
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
            mHandler.postDelayed(mFocusAndShowChannelViewRun, 200);
            centerEpgLayout.setVisibility(View.GONE);
        } else {
            mHandler.removeCallbacks(mHideChannelListRun);
            mHandler.post(mHideChannelListRun);
        }
    }

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
            livePlayerManager.init(mVideoView);
            tvLeftChannelListLayout.setVisibility(View.INVISIBLE);
            tvRightSettingLayout.setVisibility(View.INVISIBLE);
            liveChannelGroupAdapter.setNewData(liveConfig.getLiveChannelGroupList());
            selectChannelGroup(0,lastChannelGroupIndex, false, lastLiveChannelIndex);
        }
    }

    //右侧设置列表
    private void initSettingGroupView() {
        liveSettingGroupAdapter = new LiveSettingGroupAdapter();
        initTvRecyclerView(mSettingGroupView, liveSettingGroupAdapter, mHideSettingLayoutRun);
        mSettingGroupView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                selectSettingGroup(0,position, true);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {
            }
        });
        liveSettingGroupAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                selectSettingGroup(0,position, false);
            }
        });

        liveSettingItemAdapter = new LiveSettingItemAdapter();
        initTvRecyclerView(mSettingItemView, liveSettingItemAdapter, mHideSettingLayoutRun);
        mSettingItemView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                if (position < 0) return;
                liveSettingGroupAdapter.setFocusedGroupIndex(-1);
                liveSettingItemAdapter.setFocusedItemIndex(position);
                mHandler.removeCallbacks(mHideSettingLayoutRun);
                mHandler.postDelayed(mHideSettingLayoutRun, showUiTime);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {
                selectSettingGroup(1,position, false);
            }
        });
        liveSettingItemAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                selectSettingGroup(1,position, false);
            }
        });

    }

    private void selectSettingGroup(int group, int position, boolean focus) {
        switch (group) {
            case 0:
                if (!isCurrentLiveChannelValid()) return;
                if (focus) {
                    liveSettingGroupAdapter.setFocusedGroupIndex(position);
                    liveSettingItemAdapter.setFocusedItemIndex(-1);
                }
                if (position == liveSettingGroupAdapter.getSelectedGroupIndex() || position < -1)
                    return;
                liveSettingGroupAdapter.setSelectedGroupIndex(position);
                liveSettingItemAdapter.setNewData(settingConfig.getLiveSettingGroupList().get(position).getLiveSettingItems());
                switch (position) {
                    case 0:
                        liveSettingItemAdapter.selectItem(getCurrentLiveChannelItem().getSourceIndex(), true, false);
                        break;
                    case 1:
                        liveSettingItemAdapter.selectItem(livePlayerManager.getLivePlayerScale(), true, true);
                        break;
                }
                int scrollToPosition = liveSettingItemAdapter.getSelectedItemIndex();
                if (scrollToPosition < 0) scrollToPosition = 0;
                mSettingItemView.scrollToPosition(scrollToPosition);
                LinearLayoutManager layoutManager = (LinearLayoutManager) mSettingItemView.getLayoutManager();
                if (layoutManager!=null){
                    layoutManager.scrollToPositionWithOffset(scrollToPosition, 0);
                }
                break;
            case 1:
                int settingGroupIndex = liveSettingGroupAdapter.getSelectedGroupIndex();
                if (settingGroupIndex < 2) {
                    if (position == liveSettingItemAdapter.getSelectedItemIndex())
                        return;
                    liveSettingItemAdapter.selectItem(position, true, true);
                }
                switch (settingGroupIndex) {
                    case 0://线路切换
                        if (tvBack.getVisibility() == View.VISIBLE) {
                            Toast.makeText(App.getInstance(), "回放中 选中无效", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        getCurrentLiveChannelItem().setSourceIndex(position);
                        playChannel(getCurrentChannelGroupIndex(), getCurrentLiveChannelIndex(), true);
                        break;
                    case 1://画面比例
                        if (tvBack.getVisibility() == View.VISIBLE) {
                            mVideoView.setScreenScaleType(position);
                            break;
                        }
                        LiveChannelItem liveChannelItem = getCurrentLiveChannelItem();
                        livePlayerManager.changeLivePlayerScale(mVideoView, position, getCurrentChannelGroupIndex() + liveChannelItem.getChannelName() + liveChannelItem.getSourceIndex());
                        break;
                    case 2://偏好设置
                        boolean select = false;
                        switch (position) {
                            case 0:
                                select = !Hawk.get(HawkConfig.LIVE_SHOW_TIME, false);
                                Hawk.put(HawkConfig.LIVE_SHOW_TIME, select);
                                showTime();
                                break;
                            case 1:
                                select = !Hawk.get(HawkConfig.LIVE_SHOW_SPEED, false);
                                Hawk.put(HawkConfig.LIVE_SHOW_SPEED, select);
                                showSpeed();
                                break;
                            case 2:
                                select = !Hawk.get(HawkConfig.LIVE_SHOW_EPG, false);
                                Hawk.put(HawkConfig.LIVE_SHOW_EPG, select);
                                Toast.makeText(App.getInstance(), "重启生效", Toast.LENGTH_SHORT).show();
                                break;
                            case 3:
                                App.getInstance().cleanParams();
                                settingConfig.reSet();
                                Toast.makeText(App.getInstance(), "缓存清理完成", Toast.LENGTH_SHORT).show();
                                showSpeed();
                                showTime();
                                mHandler.removeCallbacks(mHideSettingLayoutRun);
                                mHandler.post(mHideSettingLayoutRun);
                                return;
                        }
                        liveSettingItemAdapter.selectItem(position, select, false);
                        break;
                }
                break;
        }
        mHandler.removeCallbacks(mHideSettingLayoutRun);
        mHandler.postDelayed(mHideSettingLayoutRun, showUiTime);
    }

    private void showSettingGroup() {
        if (tvLeftChannelListLayout.getVisibility() == View.VISIBLE || tvEpgLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideChannelListRun);
            mHandler.post(mHideChannelListRun);
        }
        showProgressBar(false);
        tvName.setVisibility(View.INVISIBLE);
        centerEpgLayout.setVisibility(View.GONE);
        if (tvRightSettingLayout.getVisibility() == View.INVISIBLE) {
            if (!isCurrentLiveChannelValid()) return;
            LiveChannelItem liveChannelItem = getCurrentLiveChannelItem();
            ArrayList<String> currentSourceNames = liveChannelItem.getChannelSourceNames();
            ArrayList<LiveSettingItem> liveSettingItemList = new ArrayList<>();
            for (int j = 0; j < currentSourceNames.size(); j++) {
                LiveSettingItem liveSettingItem = new LiveSettingItem();
                liveSettingItem.setItemIndex(j);
                liveSettingItem.setItemName(currentSourceNames.get(j));
                liveSettingItemList.add(liveSettingItem);
            }
            settingConfig.getLiveSettingGroupList().get(0).setLiveSettingItems(liveSettingItemList);
            liveSettingGroupAdapter.setNewData(settingConfig.getLiveSettingGroupList());
            selectSettingGroup(0,0, false);
            mSettingGroupView.scrollToPosition(0);
            mSettingGroupView.setSelection(0);
            LinearLayoutManager layoutManager = (LinearLayoutManager) mSettingGroupView.getLayoutManager();
            if (layoutManager != null) {
                layoutManager.scrollToPositionWithOffset(0, 0);
            }
            mSettingItemView.scrollToPosition(liveChannelItem.getSourceIndex());
            LinearLayoutManager layoutManager2 = (LinearLayoutManager) mSettingItemView.getLayoutManager();
            if (layoutManager2 != null) {
                layoutManager2.scrollToPositionWithOffset(liveChannelItem.getSourceIndex(), 0);
            }
            mHandler.postDelayed(mFocusAndShowSettingViewRun, 200);
        } else {
            mHandler.removeCallbacks(mHideSettingLayoutRun);
            mHandler.post(mHideSettingLayoutRun);
        }
    }

    private final Runnable mFocusAndShowSettingViewRun = new Runnable() {
        @Override
        public void run() {
            if (mSettingGroupView.isScrolling() || mSettingItemView.isScrolling() || mSettingGroupView.isComputingLayout() || mSettingItemView.isComputingLayout()) {
                mHandler.postDelayed(this, 100);
            } else {
                RecyclerView.ViewHolder holder = mSettingGroupView.findViewHolderForAdapterPosition(0);
                if (holder != null)
                    holder.itemView.requestFocus();
                tvTime.setVisibility(View.GONE);
                tvSpeed.setVisibility(View.GONE);
                tvRightSettingLayout.setVisibility(View.VISIBLE);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) tvRightSettingLayout.getLayoutParams();
                ViewObj viewObj = new ViewObj(tvRightSettingLayout, params);
                ObjectAnimator animator = ObjectAnimator.ofObject(viewObj, "marginRight", new IntEvaluator(), -tvRightSettingLayout.getLayoutParams().width, 0);
                animator.setDuration(200);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mHandler.removeCallbacks(mHideSettingLayoutRun);
                        mHandler.postDelayed(mHideSettingLayoutRun, showUiTime);
                    }
                });
                animator.start();
            }
        }
    };

    private final Runnable mHideSettingLayoutRun = new Runnable() {
        @Override
        public void run() {
            if (tvRightSettingLayout.getVisibility() == View.VISIBLE) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) tvRightSettingLayout.getLayoutParams();
                ViewObj viewObj = new ViewObj(tvRightSettingLayout, params);
                ObjectAnimator animator = ObjectAnimator.ofObject(viewObj, "marginRight", new IntEvaluator(), 0, -tvRightSettingLayout.getLayoutParams().width);
                animator.setDuration(200);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        tvRightSettingLayout.setVisibility(View.INVISIBLE);
                        if (Hawk.get(HawkConfig.LIVE_SHOW_TIME, false)) {
                            tvTime.setVisibility(View.VISIBLE);
                        } else {
                            tvTime.setVisibility(View.GONE);
                        }
                        if (Hawk.get(HawkConfig.LIVE_SHOW_SPEED, false)) {
                            tvSpeed.setVisibility(View.VISIBLE);
                        } else {
                            tvSpeed.setVisibility(View.GONE);
                        }
                        liveSettingGroupAdapter.setSelectedGroupIndex(-1);
                    }
                });
                animator.start();
            }
        }
    };

    //频道功能
    private void loadChannelGroupDataAndPlay(int groupIndex, int liveChannelIndex) {
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
            selectChannelGroup(1,-1, false, liveChannelIndex);
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

    private boolean isCurrentLiveChannelValid() {
        if (getCurrentLiveChannelItem() == null) {
            Toast.makeText(App.getInstance(), "请先选择频道", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    //功能显示
    private void showEpg() {
        if (App.LIVE_SHOW_EPG && !isListOrSettingLayoutVisible()) {
            mHandler.removeCallbacks(mHideEpgListRun);
            ((TextView) findViewById(R.id.tv_channel_bar_name)).setText(getCurrentLiveChannelItem().getChannelName());//底部名称
            TextView tip_time1 = findViewById(R.id.tv_current_program_time);//底部EPG当前节目信息
            TextView tip_time2 = findViewById(R.id.tv_next_program_time);//底部EPG当下个节目信息
            TextView tip_name1 = findViewById(R.id.tv_current_program_name);//底部EPG当前节目信息
            TextView tip_name2 = findViewById(R.id.tv_next_program_name);//底部EPG当前节目信息
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
            centerEpgLayout.setVisibility(View.VISIBLE);
            mHandler.postDelayed(mHideEpgListRun, showUiTime);
        } else {
            centerEpgLayout.setVisibility(View.GONE);
        }

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

    private final Runnable mHideEpgListRun = new Runnable() {
        @Override
        public void run() {
            centerEpgLayout.setVisibility(View.GONE);
        }
    };

    private final Runnable mUpdateTimeRun = new Runnable() {
        @Override
        public void run() {
            Date day = new Date();
            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
            tvTime.setText(df.format(day));
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
    private CountDownTimer countDownTimer;

    public void showProgressBar(boolean show) {
        if (show) {
            if (tvBackLayout.getVisibility() == View.VISIBLE) {
                countDownTimer.cancel();
                countDownTimer.start();
            }
            tvBackLayout.setVisibility(View.VISIBLE);
            sBar.requestFocus();
            if (tvBack.getVisibility() != View.VISIBLE) {
                sBar.setMax((int) mVideoView.getDuration());
                sBar.setKeyProgressIncrement(10000);
            }
            tv_position.setText(PlayerUtils.stringForTime((int) mVideoView.getCurrentPosition() + selectTime));
            tv_duration.setText(PlayerUtils.stringForTime((int) mVideoView.getDuration() + selectTime));
            if (countDownTimer == null) {
                countDownTimer = new CountDownTimer(showUiTime, 1000) {
                    @Override
                    public void onTick(long arg0) {
                        if (mVideoView != null && mVideoView.isPlaying() && tvBackLayout.getVisibility() == View.VISIBLE) {
                            sBar.setProgress((int) mVideoView.getCurrentPosition() + selectTime);
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
        liveEpgChannelItemAdapter = new LiveEpgChannelItemAdapter();
        liveEpgChannelItemAdapter.setNewData(liveConfig.getLiveChannelList());
        initTvRecyclerView(mEpgChannelView, liveEpgChannelItemAdapter, mHideChannelListRun);
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
        liveEpgChannelItemAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                setSelectedEpgChannel(0, position, false);
            }
        });

        liveEpgDateAdapter = new LiveEpgDateAdapter();
        initTvRecyclerView(mEpgDateView, liveEpgDateAdapter, mHideChannelListRun);
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
        initTvRecyclerView(mEpgItemView, liveEpgItemAdapter, mHideChannelListRun);
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
    }

    private void setSelectedEpgChannel(int group, int position, boolean focus) {
        mHandler.removeCallbacks(mHideChannelListRun);
        mHandler.postDelayed(mHideChannelListRun, showUiTime);
        switch (group) {
            case 0:
                if (focus) {
                    liveEpgChannelItemAdapter.setFocusedChannelIndex(position);
                    liveEpgDateAdapter.setFocusedIndex(-1);
                }
                if (position == liveEpgChannelItemAdapter.getSelectedChannelIndex() || position < -1)
                    return;
                liveEpgChannelItemAdapter.setSelectedChannelIndex(position);
                epgConfig.setEpgSelectedChannelPos(position);
                mHandler.removeCallbacks(changeChannelEpgRun);
                mHandler.postDelayed(changeChannelEpgRun, 100);
                break;
            case 1:
                if (focus) {
                    liveEpgChannelItemAdapter.setFocusedChannelIndex(-1);
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
        mEpgDateView.setSelectedPosition(pos);
        liveEpgItemAdapter.setCanBack(StringUtils.isNotEmpty(epgSelectedChannel.getSocUrls()));
        String format = TimeUtil.timeFormat.format(epgDateList.get(pos).getDateParamVal());
        changeEpg(epgSelectedChannel, format);
    }

    private void changeEpg(LiveChannelItem liveChannelItem, String date) {
        LiveEpgGroup liveEpgGroup = epgConfig.getLiveEpg(liveChannelItem, date);
        if (liveEpgGroup == null) {
            mEpgItemView.setVisibility(View.GONE);
            no_epg.setVisibility(View.VISIBLE);
            return;
        } else {
            mEpgItemView.setVisibility(View.VISIBLE);
            no_epg.setVisibility(View.GONE);
        }
        List<LiveEpgItem> epgItems = liveEpgGroup.getEpgItems();
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

    private void showEpgMenu() {
        if (App.LIVE_SHOW_EPG && !isListOrSettingLayoutVisible()) {
            LiveChannelItem epgBackChannel = epgConfig.getEpgBackChannel();
            if (epgBackChannel != null) {
                epgConfig.setEpgSelectedChannel(epgBackChannel);
            } else {
                epgConfig.setEpgSelectedChannel(getCurrentLiveChannelItem());
            }
            int pos = epgConfig.getEpgSelectedChannel().getChannelNum() - 1;
            liveEpgChannelItemAdapter.setNewData(liveConfig.getLiveChannelList());
            mEpgChannelView.scrollToPosition(pos);
            LinearLayoutManager layoutManager = (LinearLayoutManager) mEpgChannelView.getLayoutManager();
            if (layoutManager != null) {
                layoutManager.scrollToPositionWithOffset(pos, 0);
            }
            liveEpgChannelItemAdapter.setSelectedChannelIndex(pos);
            mEpgChannelView.setSelection(pos);
            List<LiveEpgDate> epgDateList = epgConfig.getEpgDateList();
            liveEpgDateAdapter.setNewData(epgDateList);
            changeChannelEpg(pos);
            mHandler.postDelayed(mFocusAndShowEpgViewRun, 100);
            centerEpgLayout.setVisibility(View.GONE);
        }
    }

    private final Runnable mFocusAndShowEpgViewRun = new Runnable() {
        @Override
        public void run() {
            if (mEpgChannelView.isComputingLayout() || mEpgDateView.isComputingLayout() || mEpgItemView.isComputingLayout() || mEpgChannelView.isScrolling() || mEpgDateView.isScrolling() || mEpgItemView.isScrolling()) {
                mHandler.postDelayed(this, 100);
            } else {
                RecyclerView.ViewHolder holder = mEpgChannelView.findViewHolderForAdapterPosition(liveEpgChannelItemAdapter.getSelectedChannelIndex());
                if (holder != null)
                    holder.itemView.requestFocus();
                tvEpgLayout.setVisibility(View.VISIBLE);
                ViewObj viewObj = new ViewObj(tvEpgLayout, (ViewGroup.MarginLayoutParams) tvEpgLayout.getLayoutParams());
                ObjectAnimator animator = ObjectAnimator.ofObject(viewObj, "marginLeft", new IntEvaluator(), -tvEpgLayout.getLayoutParams().width, 0);
                animator.setDuration(200);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mHandler.removeCallbacks(mHideChannelListRun);
                        mHandler.postDelayed(mHideChannelListRun, showUiTime);
                    }
                });
                animator.start();
            }
        }
    };

    //回放处理
    private final Runnable backChangeRun = new Runnable() {
        @Override
        public void run() {
            if (tvBack.getVisibility() == View.VISIBLE) {
                if (mVideoView != null && mVideoView.isPlaying() && mVideoView.getCurrentPosition() + selectTime >= sBar.getMax()) {
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
        if (time >= sBar.getMax()) {
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
            epgConfig.setEpgBackChannel(epgConfig.getEpgSelectedChannel().clone());
            playBack(item, true);
            tvName.setVisibility(View.GONE);
            tvBack.setVisibility(View.VISIBLE);
            centerEpgLayout.setVisibility(View.GONE);
            for (int i = 0; i < liveConfig.getLiveChannelGroupList().size(); i++) {
                for (int i1 = 0; i1 < liveConfig.getLiveChannelGroupList().get(i).getLiveChannels().size(); i1++) {
                    if (liveConfig.getLiveChannelGroupList().get(i).getLiveChannels().get(i1).getChannelNum() == epgConfig.getEpgBackChannel().getChannelNum()) {
                        liveConfig.setCurrentLiveChannelItem(liveConfig.getLiveChannelGroupList().get(i).getLiveChannels().get(i1));
                        liveConfig.setCurrentChannelGroupIndex(i);
                        liveConfig.setCurrentLiveChannelIndex(i1);
                    }
                }
            }
        }
    }

    //播放回放
    private void playBack(LiveEpgItem item, boolean updateIndex) {
        showProgressBar(false);
        String startDate = item.currentEpgDate.replaceAll("-", "") + item.start.replace(":", "") + "00";
        String endDate = item.currentEpgDate.replaceAll("-", "") + item.end.replace(":", "") + "00";
        selectedEpgItem = item;
        liveEpgItemAdapter.setLiveEpgItem(item);
        if (updateIndex) liveEpgItemAdapter.setSelectedIndex(item.index);
        int maxTime = (int) TimeUtil.getTime(TimeUtil.timeFormat.format(new Date()) + " " + item.start + ":" + "00", TimeUtil.timeFormat.format(new Date()) + " " + item.end + ":" + "00");
        sBar.setProgress(0);
        sBar.setMax(maxTime * 1000);
        sBar.setKeyProgressIncrement(10000);
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