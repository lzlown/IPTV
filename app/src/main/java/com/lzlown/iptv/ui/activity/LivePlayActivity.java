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
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.lzlown.iptv.R;
import com.lzlown.iptv.api.ApiConfig;
import com.lzlown.iptv.base.App;
import com.lzlown.iptv.base.BaseActivity;
import com.lzlown.iptv.bean.*;
import com.lzlown.iptv.player.controller.LiveController;
import com.lzlown.iptv.ui.adapter.*;
import com.lzlown.iptv.ui.tv.widget.ViewObj;
import com.lzlown.iptv.util.*;
import com.lzlown.iptv.videoplayer.player.VideoView;
import com.orhanobut.hawk.Hawk;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7LinearLayoutManager;
import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.*;

public class LivePlayActivity extends BaseActivity {
    private static final String TAG = LivePlayActivity.class.getName();
    public static Context context;
    private VideoView mVideoView;
    private Handler mHandler = new Handler();
    private final LivePlayerManager livePlayerManager = new LivePlayerManager();

    //频道列表
    private LinearLayout tvLeftChannelListLayout;
    private TvRecyclerView mChannelGroupView;
    private TvRecyclerView mChannelItemView;
    private LiveChannelGroupAdapter liveChannelGroupAdapter;
    private LiveChannelItemAdapter liveChannelItemAdapter;
    private final List<LiveChannelGroup> liveChannelGroupList = new ArrayList<>();
    private int currentChannelGroupIndex = 0;
    private int currentLiveChannelIndex = -1;
    private int currentLiveChangeSourceTimes = 0;
    private LiveChannelItem currentLiveChannelItem = null;
    private LiveChannelItem epgLiveChannelItem = null;

    //设置列表
    private LinearLayout tvRightSettingLayout;
    private TvRecyclerView mSettingGroupView;
    private TvRecyclerView mSettingItemView;
    private LiveSettingGroupAdapter liveSettingGroupAdapter;
    private LiveSettingItemAdapter liveSettingItemAdapter;
    private final List<LiveSettingGroup> liveSettingGroupList = new ArrayList<>();

    private long mExitTime = 0;
    private boolean loadEnd = false;
    private boolean canChangeSource = false;

    //中间EPG
    private View centerEpgLayout;

    //右边显示
    private TextView tvName;
    private TextView tvTime;
    private TextView tvSpeed;
    //显示回放
    private TextView tvBack;

    //回放显示列表
    private View tvEpgLayout;
    private TvRecyclerView mEpgGroupView;
    private TvRecyclerView mEpgItemView;
    private LiveEpgItemAdapter liveEpgItemAdapter;
    private LiveEpgGroupAdapter liveEpgGroupAdapter;

    //回放控制
    private View tvBackLayout;
    private SeekBar sBar;
    private TextView tv_currentpos;
    private TextView tv_duration;

    private Boolean isCanBack = false;
    private LiveEpgItem selectedData;
    private int selectTime = 0;

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
//        mVideoView.release();
//        if (currentLiveChannelItem != null) {
//            mVideoView.setUrl(currentLiveChannelItem.getUrl());
//        } else {
//            mVideoView.setUrl(ApiConfig.get().getChannelGroupList().get(0).getLiveChannels().get(0).getUrl());
//        }
//        mVideoView.start();
//        mVideoView.setScreenScaleType(0);

        //界面 view
        tvLeftChannelListLayout = findViewById(R.id.tvLeftChannnelListLayout);
        mChannelGroupView = findViewById(R.id.mGroupGridView);
        mChannelItemView = findViewById(R.id.mChannelGridView);
        tvRightSettingLayout = findViewById(R.id.tvRightSettingLayout);
        mSettingGroupView = findViewById(R.id.mSettingGroupView);
        mSettingItemView = findViewById(R.id.mSettingItemView);

        //中间EPG
        centerEpgLayout = findViewById(R.id.ll_epg);
        centerEpgLayout.setVisibility(View.INVISIBLE);

        //右边显示
        tvName = findViewById(R.id.tvName);
        tvTime = findViewById(R.id.tvTime);
        tvSpeed = findViewById(R.id.tvSpeed);

        tvBack = findViewById(R.id.tvBack);
        tvEpgLayout = findViewById(R.id.divEPG);
        mEpgGroupView = findViewById(R.id.mEpgDateGridView);
        mEpgItemView = findViewById(R.id.lv_epg);

        tvBackLayout = findViewById(R.id.backcontroller);
        sBar = findViewById(R.id.pb_progressbar);
        tv_currentpos = findViewById(R.id.tv_currentpos);
        tv_duration = findViewById(R.id.tv_duration);
        tvBackLayout.setVisibility(View.GONE);
        sBar.setKeyProgressIncrement(10000);
        sBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {

            }

            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (fromUser) {
                    if (tvBack.getVisibility() == View.VISIBLE && isCanBack) {
                        playBack(progress);
                    } else {
                        mVideoView.seekTo(progress);
                    }
                }
            }
        });
        initVideoView();
        initChannelGroupView();
        initLiveChannelView();
        initLiveChannelList();
        initSettingGroupView();
        initSettingItemView();
        initLiveSettingGroupList();

        initEpgDateView();
        initEpgListView();
    }

    @Override
    public void onBackPressed() {
        if (tvLeftChannelListLayout.getVisibility() == View.VISIBLE || tvEpgLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideChannelListRun);
            mHandler.post(mHideChannelListRun);
        } else if (tvBackLayout.getVisibility() == View.VISIBLE) {
            tvBackLayout.setVisibility(View.INVISIBLE);
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
                        showEpg();
                        break;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        if (Hawk.get(HawkConfig.LIVE_CHANNEL_REVERSE, true)) {
                            playPrevious();
                        } else {
                            playNext();
                        }
                        showEpg();
                        break;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        if (tvBackLayout.getVisibility() != View.VISIBLE) {
                            showEpgMenu(true);
                            break;
                        } else {
                            if (canChangeSource) {
                                playPreSource();
                            }
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        if (isCanBack && mVideoView.isPlaying()) {
                            showProgressBars(true);
                            break;
                        } else {
                            if (canChangeSource) {
                                playNextSource();
                            }
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_ENTER:
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                        showChannelList();
                        break;
                }
            }
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mVideoView != null && loadEnd) {
            //todo 睡眠处理
            mVideoView.release();
            mVideoView.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoView != null) {
            mVideoView.release();
        }
        loadEnd = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVideoView != null) {
            mVideoView.release();
            mVideoView = null;
        }
    }

    private void rmRunAll() {
        mHandler.removeCallbacks(mFocusCurrentChannelAndShowChannelList);
        mHandler.removeCallbacks(mHideChannelListRun);
        mHandler.removeCallbacks(mHideEpgListRun);
        mHandler.removeCallbacks(mFocusAndShowSettingGroup);
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
            rmRunAll();
            mVideoView = null;
            mHandler = null;
            EventBus.getDefault().unregister(this);
            AppManager.getInstance().appExit(0);
            finish();
            super.onBackPressed();
        } else {
            mExitTime = System.currentTimeMillis();
            Toast.makeText(mContext, "再按一次返回键退出应用", Toast.LENGTH_SHORT).show();
        }
    }

    private void showChannelList() {
        if (tvRightSettingLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideSettingLayoutRun);
            mHandler.post(mHideSettingLayoutRun);
            return;
        }
        if (tvEpgLayout.getVisibility() == View.VISIBLE) {
            tvEpgLayout.setVisibility(View.GONE);
            return;
        }
        if (tvLeftChannelListLayout.getVisibility() == View.INVISIBLE) {
            //重新载入上一次状态
            liveChannelItemAdapter.setNewData(getLiveChannels(currentChannelGroupIndex));
            if (currentLiveChannelIndex > -1) {
                mChannelItemView.scrollToPosition(currentLiveChannelIndex);
                LinearLayoutManager layoutManager = (LinearLayoutManager) mChannelItemView.getLayoutManager();
                layoutManager.scrollToPositionWithOffset(currentLiveChannelIndex, 0);
            }
            mChannelItemView.setSelection(currentLiveChannelIndex);
            mChannelGroupView.scrollToPosition(currentChannelGroupIndex);
            LinearLayoutManager layoutManager = (LinearLayoutManager) mChannelGroupView.getLayoutManager();
            layoutManager.scrollToPositionWithOffset(currentChannelGroupIndex, 0);
            mChannelGroupView.setSelection(currentChannelGroupIndex);
            mHandler.postDelayed(mFocusCurrentChannelAndShowChannelList, 200);
            centerEpgLayout.setVisibility(View.INVISIBLE);
        } else {
            mHandler.removeCallbacks(mHideChannelListRun);
            mHandler.post(mHideChannelListRun);
        }
    }

    private final Runnable mFocusCurrentChannelAndShowChannelList = new Runnable() {
        @Override
        public void run() {
            if (mChannelGroupView.isScrolling() || mChannelItemView.isScrolling() || mChannelGroupView.isComputingLayout() || mChannelItemView.isComputingLayout()) {
                mHandler.postDelayed(this, 100);
            } else {
                liveChannelGroupAdapter.setSelectedGroupIndex(currentChannelGroupIndex);
                liveChannelItemAdapter.setSelectedChannelIndex(currentLiveChannelIndex);
                RecyclerView.ViewHolder holder = mChannelItemView.findViewHolderForAdapterPosition(currentLiveChannelIndex);
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
                        mHandler.postDelayed(mHideChannelListRun, 5000);
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
                tvEpgLayout.setVisibility(View.GONE);
            }
        }
    };

    private void playChannel(int channelGroupIndex, int liveChannelIndex, boolean changeSource) {
        if ((channelGroupIndex == currentChannelGroupIndex && liveChannelIndex == currentLiveChannelIndex && !changeSource)
                || (changeSource && currentLiveChannelItem.getSourceNum() == 1)) {
            if (!isCanBack) {
                return;
            }
        }
        if (!changeSource) {
            currentChannelGroupIndex = channelGroupIndex;
            currentLiveChannelIndex = liveChannelIndex;
            currentLiveChannelItem = getLiveChannels(currentChannelGroupIndex).get(currentLiveChannelIndex);
            Hawk.put(HawkConfig.LIVE_GROUP, currentChannelGroupIndex);
            Hawk.put(HawkConfig.LIVE_CHANNEL, currentLiveChannelItem.getChannelName());
            tvName.setText(String.format("%d %s", currentLiveChannelItem.getChannelNum(), currentLiveChannelItem.getChannelName()));
            tvName.setVisibility(View.VISIBLE);
        }
        livePlayerManager.getLiveChannelPlayer(mVideoView, currentChannelGroupIndex + currentLiveChannelItem.getChannelName() + currentLiveChannelItem.getSourceIndex());
        if (currentLiveChannelItem.getUrl().contains(".mp4")) {
            isCanBack = true;
        } else {
            isCanBack = false;
        }
        tvBack.setVisibility(View.GONE);
        selectedData = null;
        if (liveEpgItemAdapter != null) {
            liveEpgItemAdapter.setLiveEpgItemIndex(null);
        }
        selectTime = 0;
        mHandler.removeCallbacks(backChange);
        showProgressBars(false);
        mVideoView.release();
        mVideoView.setUrl(currentLiveChannelItem.getUrl());
        mVideoView.start();
    }

    private void playNext() {
        if (!isCurrentLiveChannelValid()) return;
        Integer[] groupChannelIndex = getNextChannel(1);
        playChannel(groupChannelIndex[0], groupChannelIndex[1], false);
    }

    private void playPrevious() {
        if (!isCurrentLiveChannelValid()) return;
        Integer[] groupChannelIndex = getNextChannel(-1);
        playChannel(groupChannelIndex[0], groupChannelIndex[1], false);
    }

    public void playPreSource() {
        if (!isCurrentLiveChannelValid()) return;
        currentLiveChannelItem.preSource();
        playChannel(currentChannelGroupIndex, currentLiveChannelIndex, true);
    }

    public void playNextSource() {
        if (!isCurrentLiveChannelValid()) return;
        currentLiveChannelItem.nextSource();
        playChannel(currentChannelGroupIndex, currentLiveChannelIndex, true);
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
                        showProgressBars(true);
                    }
                } else {
                    showEpgMenu(false);
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
                playChannel(currentChannelGroupIndex, currentLiveChannelIndex, false);
                return;
            }
            if (canChangeSource) {
                currentLiveChangeSourceTimes++;
                if (currentLiveChannelItem.getSourceNum() == currentLiveChangeSourceTimes) {
                    currentLiveChangeSourceTimes = 0;
                    Integer[] groupChannelIndex = getNextChannel(Hawk.get(HawkConfig.LIVE_CHANNEL_REVERSE, false) ? -1 : 1);
                    playChannel(groupChannelIndex[0], groupChannelIndex[1], false);
                } else {
                    playNextSource();
                }
            } else {
                currentLiveChangeSourceTimes = 0;
                Integer[] groupChannelIndex = getNextChannel(Hawk.get(HawkConfig.LIVE_CHANNEL_REVERSE, false) ? -1 : 1);
                playChannel(groupChannelIndex[0], groupChannelIndex[1], false);
            }
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
                mHandler.postDelayed(runnable, 5000);
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
                selectChannelGroup(position, true, -1);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {

            }
        });
        liveChannelGroupAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                selectChannelGroup(position, false, -1);
            }
        });
    }

    private void selectChannelGroup(int groupIndex, boolean focus, int liveChannelIndex) {
        if (focus) {
            liveChannelGroupAdapter.setFocusedGroupIndex(groupIndex);
            liveChannelItemAdapter.setFocusedChannelIndex(-1);
        }
        if ((groupIndex > -1 && groupIndex != liveChannelGroupAdapter.getSelectedGroupIndex())) {
            liveChannelGroupAdapter.setSelectedGroupIndex(groupIndex);
            loadChannelGroupDataAndPlay(groupIndex, liveChannelIndex);
        }
        if (tvLeftChannelListLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideChannelListRun);
            mHandler.postDelayed(mHideChannelListRun, 5000);
        }
    }

    private void initLiveChannelView() {
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
                mHandler.postDelayed(mHideChannelListRun, 5000);

            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {
                clickLiveChannel(position);
            }
        });
        liveChannelItemAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                clickLiveChannel(position);
            }
        });
    }

    private void clickLiveChannel(int position) {
        liveChannelItemAdapter.setSelectedChannelIndex(position);
        playChannel(liveChannelGroupAdapter.getSelectedGroupIndex(), position, false);
        if (tvLeftChannelListLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideChannelListRun);
            mHandler.postDelayed(mHideChannelListRun, 5000);
        }
    }

    private void initLiveChannelList() {
        List<LiveChannelGroup> list = ApiConfig.get().getChannelGroupList();
        if (list.isEmpty()) {
            Toast.makeText(App.getInstance(), "频道列表为空", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            liveChannelGroupList.clear();
            liveChannelGroupList.addAll(list);
            initLiveState();
        }
    }

    private void initLiveState() {
        String lastChannelName = Hawk.get(HawkConfig.LIVE_CHANNEL, "");
        Integer lastChannelGroupIndex = Hawk.get(HawkConfig.LIVE_GROUP, -1);
        int lastLiveChannelIndex = -1;
        if (lastChannelGroupIndex != -1) {
            if (lastChannelGroupIndex < liveChannelGroupList.size()) {
                LiveChannelGroup liveChannelGroup = liveChannelGroupList.get(lastChannelGroupIndex);
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
        showTime();
        showSpeed();
        livePlayerManager.init(mVideoView);
        tvLeftChannelListLayout.setVisibility(View.INVISIBLE);
        tvRightSettingLayout.setVisibility(View.INVISIBLE);
        liveChannelGroupAdapter.setNewData(liveChannelGroupList);
        selectChannelGroup(lastChannelGroupIndex, false, lastLiveChannelIndex);
    }

    //右侧设置列表
    private void initLiveSettingGroupList() {
        ArrayList<ArrayList<String>> itemsArrayList = new ArrayList<>();
        ArrayList<String> groupNames = new ArrayList<>(Arrays.asList("画质线路", "画面比例", "偏好设置"));
        ArrayList<String> sourceItems = new ArrayList<>();
        ArrayList<String> scaleItems = new ArrayList<>(Arrays.asList("默认", "16:9", "4:3", "填充", "原始", "裁剪"));
        ArrayList<String> personalSettingItems = new ArrayList<>(Arrays.asList("显示时间", "显示网速", "显示预告", "清理缓存"));
        itemsArrayList.add(sourceItems);
        itemsArrayList.add(scaleItems);
        itemsArrayList.add(personalSettingItems);
        liveSettingGroupList.clear();
        for (int i = 0; i < groupNames.size(); i++) {
            LiveSettingGroup liveSettingGroup = new LiveSettingGroup();
            ArrayList<LiveSettingItem> liveSettingItemList = new ArrayList<>();
            liveSettingGroup.setGroupIndex(i);
            liveSettingGroup.setGroupName(groupNames.get(i));
            for (int j = 0; j < itemsArrayList.get(i).size(); j++) {
                LiveSettingItem liveSettingItem = new LiveSettingItem();
                liveSettingItem.setItemIndex(j);
                liveSettingItem.setItemName(itemsArrayList.get(i).get(j));
                liveSettingItemList.add(liveSettingItem);
            }
            liveSettingGroup.setLiveSettingItems(liveSettingItemList);
            liveSettingGroupList.add(liveSettingGroup);
        }
        loadLiveSettingGroupList();
    }

    private void loadLiveSettingGroupList() {
        liveSettingGroupList.get(2).getLiveSettingItems().get(0).setItemSelected(Hawk.get(HawkConfig.LIVE_SHOW_TIME, false));
        liveSettingGroupList.get(2).getLiveSettingItems().get(1).setItemSelected(Hawk.get(HawkConfig.LIVE_SHOW_SPEED, false));
        liveSettingGroupList.get(2).getLiveSettingItems().get(2).setItemSelected(Hawk.get(HawkConfig.LIVE_SHOW_EPG, false));
    }

    private void showSettingGroup() {
        if (tvLeftChannelListLayout.getVisibility() == View.VISIBLE || tvEpgLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideChannelListRun);
            mHandler.post(mHideChannelListRun);
        }
        showProgressBars(false);
        tvName.setVisibility(View.INVISIBLE);
        if (tvRightSettingLayout.getVisibility() == View.INVISIBLE) {
            if (!isCurrentLiveChannelValid()) return;
            loadCurrentSourceList();
            liveSettingGroupAdapter.setNewData(liveSettingGroupList);
            selectSettingGroup(0, false);
            mSettingGroupView.scrollToPosition(0);
            mSettingGroupView.setSelection(0);
            LinearLayoutManager layoutManager = (LinearLayoutManager) mSettingGroupView.getLayoutManager();
            layoutManager.scrollToPositionWithOffset(0, 0);
            mSettingItemView.scrollToPosition(currentLiveChannelItem.getSourceIndex());
            LinearLayoutManager layoutManager2 = (LinearLayoutManager) mSettingItemView.getLayoutManager();
            layoutManager2.scrollToPositionWithOffset(currentLiveChannelItem.getSourceIndex(), 0);
            mHandler.postDelayed(mFocusAndShowSettingGroup, 200);
            centerEpgLayout.setVisibility(View.INVISIBLE);
        } else {
            mHandler.removeCallbacks(mHideSettingLayoutRun);
            mHandler.post(mHideSettingLayoutRun);
        }
    }

    private void loadCurrentSourceList() {
        ArrayList<String> currentSourceNames = currentLiveChannelItem.getChannelSourceNames();
        ArrayList<LiveSettingItem> liveSettingItemList = new ArrayList<>();
        for (int j = 0; j < currentSourceNames.size(); j++) {
            LiveSettingItem liveSettingItem = new LiveSettingItem();
            liveSettingItem.setItemIndex(j);
            liveSettingItem.setItemName(currentSourceNames.get(j));
            liveSettingItemList.add(liveSettingItem);
        }
        liveSettingGroupList.get(0).setLiveSettingItems(liveSettingItemList);
    }

    private void initSettingGroupView() {
        liveSettingGroupAdapter = new LiveSettingGroupAdapter();
        initTvRecyclerView(mSettingGroupView, liveSettingGroupAdapter, mHideSettingLayoutRun);
        mSettingGroupView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                selectSettingGroup(position, true);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {
            }
        });
        liveSettingGroupAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                selectSettingGroup(position, false);
            }
        });
    }

    private void selectSettingGroup(int position, boolean focus) {
        if (!isCurrentLiveChannelValid()) return;
        if (focus) {
            liveSettingGroupAdapter.setFocusedGroupIndex(position);
            liveSettingItemAdapter.setFocusedItemIndex(-1);
        }
        if (position == liveSettingGroupAdapter.getSelectedGroupIndex() || position < -1)
            return;
        liveSettingGroupAdapter.setSelectedGroupIndex(position);
        liveSettingItemAdapter.setNewData(liveSettingGroupList.get(position).getLiveSettingItems());
        switch (position) {
            case 0:
                liveSettingItemAdapter.selectItem(currentLiveChannelItem.getSourceIndex(), true, false);
                break;
            case 1:
                liveSettingItemAdapter.selectItem(livePlayerManager.getLivePlayerScale(), true, true);
                break;
        }
        int scrollToPosition = liveSettingItemAdapter.getSelectedItemIndex();
        if (scrollToPosition < 0) scrollToPosition = 0;
        mSettingItemView.scrollToPosition(scrollToPosition);
        LinearLayoutManager layoutManager = (LinearLayoutManager) mSettingItemView.getLayoutManager();
        layoutManager.scrollToPositionWithOffset(scrollToPosition, 0);
        mHandler.removeCallbacks(mHideSettingLayoutRun);
        mHandler.postDelayed(mHideSettingLayoutRun, 5000);
    }

    private void initSettingItemView() {
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
                mHandler.postDelayed(mHideSettingLayoutRun, 5000);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {
                clickSettingItem(position);
            }
        });
        liveSettingItemAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                clickSettingItem(position);
            }
        });
    }

    private void clickSettingItem(int position) {
        int settingGroupIndex = liveSettingGroupAdapter.getSelectedGroupIndex();
        if (settingGroupIndex < 2) {
            if (position == liveSettingItemAdapter.getSelectedItemIndex())
                return;
            liveSettingItemAdapter.selectItem(position, true, true);
        }
        switch (settingGroupIndex) {
            case 0://线路切换
                currentLiveChannelItem.setSourceIndex(position);
                playChannel(currentChannelGroupIndex, currentLiveChannelIndex, true);
                break;
            case 1://画面比例
                livePlayerManager.changeLivePlayerScale(mVideoView, position, currentChannelGroupIndex + currentLiveChannelItem.getChannelName() + currentLiveChannelItem.getSourceIndex());
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
                        loadLiveSettingGroupList();
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
        mHandler.removeCallbacks(mHideSettingLayoutRun);
        mHandler.postDelayed(mHideSettingLayoutRun, 5000);
    }

    private final Runnable mFocusAndShowSettingGroup = new Runnable() {
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
                        mHandler.postDelayed(mHideSettingLayoutRun, 5000);
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
        liveChannelItemAdapter.setNewData(getLiveChannels(groupIndex));
        if (groupIndex == currentChannelGroupIndex) {
            if (currentLiveChannelIndex > -1) {
                mChannelItemView.scrollToPosition(currentLiveChannelIndex);
                LinearLayoutManager layoutManager = (LinearLayoutManager) mChannelItemView.getLayoutManager();
                layoutManager.scrollToPositionWithOffset(currentLiveChannelIndex, 0);
            }
            liveChannelItemAdapter.setSelectedChannelIndex(currentLiveChannelIndex);
        } else {
            mChannelItemView.scrollToPosition(0);
            liveChannelItemAdapter.setSelectedChannelIndex(-1);
            LinearLayoutManager layoutManager = (LinearLayoutManager) mChannelItemView.getLayoutManager();
            layoutManager.scrollToPositionWithOffset(0, 0);
        }
        if (liveChannelIndex > -1) {
            clickLiveChannel(liveChannelIndex);
            mChannelGroupView.scrollToPosition(groupIndex);
            LinearLayoutManager layoutManager = (LinearLayoutManager) mChannelGroupView.getLayoutManager();
            layoutManager.scrollToPositionWithOffset(groupIndex, 0);
            mChannelItemView.scrollToPosition(liveChannelIndex);
            LinearLayoutManager layoutManager2 = (LinearLayoutManager) mChannelItemView.getLayoutManager();
            layoutManager2.scrollToPositionWithOffset(liveChannelIndex, 0);
            playChannel(groupIndex, liveChannelIndex, false);
        }
    }

    private ArrayList<LiveChannelItem> getLiveChannels(int groupIndex) {
        return liveChannelGroupList.get(groupIndex).getLiveChannels();
    }

    private Integer[] getNextChannel(int direction) {
        int channelGroupIndex = currentChannelGroupIndex;
        int liveChannelIndex = currentLiveChannelIndex;
        if (direction > 0) {
            liveChannelIndex++;
            if (liveChannelIndex >= getLiveChannels(channelGroupIndex).size()) {
                liveChannelIndex = 0;
                if (Hawk.get(HawkConfig.LIVE_CROSS_GROUP, true)) {
                    do {
                        channelGroupIndex++;
                        if (channelGroupIndex >= liveChannelGroupList.size()) {
                            channelGroupIndex = 0;
                        }
                    } while (channelGroupIndex == currentChannelGroupIndex && channelGroupIndex != 0);
                }
            }
        } else {
            liveChannelIndex--;
            if (liveChannelIndex < 0) {
                if (Hawk.get(HawkConfig.LIVE_CROSS_GROUP, true)) {
                    do {
                        channelGroupIndex--;
                        if (channelGroupIndex < 0)
                            channelGroupIndex = liveChannelGroupList.size() - 1;
                    } while (channelGroupIndex == currentChannelGroupIndex);
                }
                liveChannelIndex = getLiveChannels(channelGroupIndex).size() - 1;
            }
        }

        Integer[] groupChannelIndex = new Integer[2];
        groupChannelIndex[0] = channelGroupIndex;
        groupChannelIndex[1] = liveChannelIndex;
        return groupChannelIndex;
    }

    private boolean isCurrentLiveChannelValid() {
        if (currentLiveChannelItem == null) {
            Toast.makeText(App.getInstance(), "请先选择频道", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    //功能显示
    private void showEpg() {
        if (App.LIVE_SHOW_EPG) {
            if (tvLeftChannelListLayout.getVisibility() != View.VISIBLE || tvRightSettingLayout.getVisibility() != View.VISIBLE) {
                mHandler.removeCallbacks(mHideEpgListRun);
                ((TextView) findViewById(R.id.tv_channel_bar_name)).setText(currentLiveChannelItem.getChannelName());//底部名称
                TextView tip_time1 = findViewById(R.id.tv_current_program_time);//底部EPG当前节目信息
                TextView tip_time2 = findViewById(R.id.tv_next_program_time);//底部EPG当下个节目信息
                TextView tip_name1 = findViewById(R.id.tv_current_program_name);//底部EPG当前节目信息
                TextView tip_name2 = findViewById(R.id.tv_next_program_name);//底部EPG当前节目信息
                Map<String, LiveEpgItem> liveEpgItemForMap = ApiConfig.get().getLiveEpgItemForMap(currentLiveChannelItem);
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
                mHandler.postDelayed(mHideEpgListRun, 5000);
            } else {
                centerEpgLayout.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void showTime() {
        if (Hawk.get(HawkConfig.LIVE_SHOW_TIME, false)) {
            mHandler.post(mUpdateTimeRun);
            tvTime.setVisibility(View.VISIBLE);
        } else {
            mHandler.removeCallbacks(mUpdateTimeRun);
            tvTime.setVisibility(View.GONE);
        }
    }

    private void showSpeed() {
        if (Hawk.get(HawkConfig.LIVE_SHOW_SPEED, false)) {
            mHandler.post(mUpdateSpeedRun);
            tvSpeed.setVisibility(View.VISIBLE);
        } else {
            mHandler.removeCallbacks(mUpdateSpeedRun);
            tvSpeed.setVisibility(View.GONE);
        }
    }

    private final Runnable mHideEpgListRun = new Runnable() {
        @Override
        public void run() {
            centerEpgLayout.setVisibility(View.INVISIBLE);
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

    private CountDownTimer countDownTimer;

    //进度条
    public void showProgressBars(boolean show) {
        if (show) {
            sBar.requestFocus();
            if (tvBack.getVisibility() != View.VISIBLE) {
                sBar.setMax((int) mVideoView.getDuration());
                sBar.setKeyProgressIncrement(10000);
            }
            tv_currentpos.setText(TimeUtil.durationToString((int) mVideoView.getCurrentPosition() + selectTime));
            tv_duration.setText(TimeUtil.durationToString((int) mVideoView.getDuration() + selectTime));
            tvBackLayout.setVisibility(View.VISIBLE);
            if (countDownTimer == null) {
                countDownTimer = new CountDownTimer(5000, 1000) {
                    @Override
                    public void onTick(long arg0) {
                        if (mVideoView != null && mVideoView.isPlaying()) {
                            if (tvBackLayout.getVisibility() == View.VISIBLE) {
                                sBar.setProgress((int) mVideoView.getCurrentPosition() + selectTime);
                                tv_currentpos.setText(TimeUtil.durationToString((int) mVideoView.getCurrentPosition() + selectTime));
                            }
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
            tvBackLayout.setVisibility(View.GONE);
        }

    }

    //预告回放
    private void initEpgDateView() {
        liveEpgGroupAdapter = new LiveEpgGroupAdapter();
        initTvRecyclerView(mEpgGroupView, liveEpgGroupAdapter, mHideChannelListRun);
        mEpgGroupView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
                if (mEpgGroupView.isComputingLayout() || position < 0)
                    return;
                if (liveEpgGroupAdapter.getSelectedIndex() == position) {
                    liveEpgItemAdapter.setFocusedIndex(-1);
                    liveEpgGroupAdapter.setFocusedIndex(position);
                    return;
                }
                liveEpgGroupAdapter.setFocusedIndex(position);
                liveEpgGroupAdapter.setSelectedIndex(position);
                liveEpgItemAdapter.setFocusedIndex(-1);
                liveEpgItemAdapter.setSelectedIndex(-1);
                String format = TimeUtil.timeFormat.format(liveEpgGroupAdapter.getData().get(position).getDateParamVal());
                loadEpg(format);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {

            }
        });
        liveEpgGroupAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
                if (mEpgGroupView.isComputingLayout() || liveEpgGroupAdapter.getSelectedIndex() == position || position < 0)
                    return;
                liveEpgGroupAdapter.setSelectedIndex(position);
                liveEpgItemAdapter.setSelectedIndex(-1);
                String format = TimeUtil.timeFormat.format(liveEpgGroupAdapter.getData().get(position).getDateParamVal());
                loadEpg(format);
            }
        });
    }

    private void initEpgListView() {
        liveEpgItemAdapter = new LiveEpgItemAdapter();
        initTvRecyclerView(mEpgItemView, liveEpgItemAdapter, mHideChannelListRun);
        mEpgItemView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {

            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
                if (parent.isComputingLayout() || position < 0) return;
                liveEpgItemAdapter.setFocusedIndex(position);
                liveEpgGroupAdapter.setFocusedIndex(-1);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {

            }
        });
        liveEpgItemAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
                LiveEpgItem epgItem = liveEpgItemAdapter.getItem(position);
                if (epgItem == null) {
                    return;
                }
                playBack(epgItem);
            }
        });
    }

    private void showEpgMenu(boolean focus) {
        if (App.LIVE_SHOW_EPG) {
            if (!isListOrSettingLayoutVisible()) {
                epgLiveChannelItem = currentLiveChannelItem.clone();
                liveEpgItemAdapter.setCanBack(StringUtils.isNotEmpty(epgLiveChannelItem.getSocUrls()));
                List<LiveEpgDate> epgDateList = ApiConfig.get().getEpgDateList();
                liveEpgGroupAdapter.setNewData(epgDateList);
                int pos = epgDateList.size() - 2;
                mEpgGroupView.setSelection(pos);
                if (focus) {
                    liveEpgGroupAdapter.setSelectedIndex(pos);
                    liveEpgGroupAdapter.setFocusedIndex(pos);
                } else {
                    liveEpgGroupAdapter.setSelectedIndex(pos);
                    liveEpgGroupAdapter.setFocusedIndex(-1);
                }
                mEpgGroupView.scrollToPosition(pos);
                LinearLayoutManager layoutManager = (LinearLayoutManager) mEpgGroupView.getLayoutManager();
                layoutManager.scrollToPositionWithOffset(pos, 0);
                String format = TimeUtil.timeFormat.format(epgDateList.get(pos).getDateParamVal());
                loadEpg(format);
                mHandler.postDelayed(mFocusAndShowEpgView, 200);
            }
        }
    }


    private void loadEpg(String date) {
        LiveEpg liveEpg = ApiConfig.get().getLiveEpg(epgLiveChannelItem, date);
        List<LiveEpgItem> epgItems = liveEpg.getEpgItems();
        liveEpgItemAdapter.setNewData(epgItems);
        if (selectedData != null && selectedData.currentEpgDate.equals(date)) {
            liveEpgItemAdapter.setSelectedIndex(selectedData.index);
            mEpgItemView.scrollToPosition(selectedData.index);
            LinearLayoutManager layoutManager = (LinearLayoutManager) mEpgItemView.getLayoutManager();
            layoutManager.scrollToPositionWithOffset(selectedData.index, 0);
            return;
        }
        if (date.equals(TimeUtil.getTime())) {
            Date time = new Date();
            for (LiveEpgItem epgItem : epgItems) {
                if (time.compareTo(TimeUtil.getEpgTime(epgItem.currentEpgDate + epgItem.start)) > 0 && time.compareTo(TimeUtil.getEpgTime(epgItem.currentEpgDate + epgItem.end)) < 0) {
                    liveEpgItemAdapter.setSelectedIndex(epgItem.index);
                    mEpgItemView.scrollToPosition(epgItem.index);
                    LinearLayoutManager layoutManager = (LinearLayoutManager) mEpgItemView.getLayoutManager();
                    layoutManager.scrollToPositionWithOffset(epgItem.index, 0);
                    return;
                }
            }
        } else {
            mEpgItemView.scrollToPosition(0);
            LinearLayoutManager layoutManager = (LinearLayoutManager) mEpgItemView.getLayoutManager();
            layoutManager.scrollToPositionWithOffset(0, 0);
        }
    }

    private final Runnable mFocusAndShowEpgView = new Runnable() {
        @Override
        public void run() {
            if (mEpgGroupView.isComputingLayout() || mEpgItemView.isComputingLayout() || mEpgGroupView.isScrolling() || mEpgItemView.isScrolling()) {
                mHandler.postDelayed(this, 100);
            } else {
                RecyclerView.ViewHolder holder = mEpgGroupView.findViewHolderForAdapterPosition(7);
                if (holder != null)
                    holder.itemView.requestFocus();
                tvEpgLayout.setVisibility(View.VISIBLE);
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
            }
        }
    };

    //回放处理
    private final Runnable backChange = new Runnable() {
        @Override
        public void run() {
            if (tvBack.getVisibility() == View.VISIBLE) {
                if (mVideoView != null && mVideoView.isPlaying() && mVideoView.getCurrentPosition() + selectTime >= sBar.getMax()) {
                    if (tvBack.getVisibility() == View.VISIBLE && epgLiveChannelItem != null && selectedData != null) {
                        playBackNext(selectedData);
                    }
                    return;
                }
                mHandler.postDelayed(this, 3000);
            }
        }
    };

    private void playBack(int time) {
        if (time >= sBar.getMax()) {
            isCanBack = false;
            mVideoView.release();
            mHandler.removeCallbacks(backChange);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isCanBack = true;
                    playBackNext(selectedData);
                }
            }, 500);
            return;
        }
        selectTime = time;
        String startDate = selectedData.currentEpgDate.replaceAll("-", "") + selectedData.start.replaceAll(":", "") + "00";
        startDate = TimeUtil.getTimeS(startDate, selectTime);
        String endDate = selectedData.currentEpgDate.replaceAll("-", "") + selectedData.end.replace(":", "") + "00";
        tv_currentpos.setText(TimeUtil.durationToString(selectTime));
        countDownTimer.cancel();
        countDownTimer.start();
        mVideoView.release();
        mVideoView.setUrl(String.format(epgLiveChannelItem.getSocUrls(), TimeUtil.getTimeS(startDate) + "GMT-" + TimeUtil.getTimeS(endDate) + "GMT"));
        mVideoView.start();
    }

    private void playBack(LiveEpgItem item) {
        Date date = new Date();
        Date epgStartTime = TimeUtil.getEpgTime(item.currentEpgDate + item.start);
        if (date.compareTo(epgStartTime) < 0 || TimeUtil.getTimeToDate(-7).compareTo(epgStartTime) > 0 || StringUtils.isEmpty(epgLiveChannelItem.getSocUrls())) {
            return;
        } else if (date.compareTo(epgStartTime) > 0 && date.compareTo(TimeUtil.getEpgTime(item.currentEpgDate + item.end)) < 0) {
            liveEpgItemAdapter.setSelectedIndex(item.index);
            playChannel(currentChannelGroupIndex, currentLiveChannelIndex, false);
            return;
        } else if (epgLiveChannelItem == null) {
            playChannel(currentChannelGroupIndex, currentLiveChannelIndex, false);
            return;
        }
        String startDate = item.currentEpgDate.replaceAll("-", "") + item.start.replace(":", "") + "00";
        String endDate = item.currentEpgDate.replaceAll("-", "") + item.end.replace(":", "") + "00";
        selectedData = item;
        liveEpgItemAdapter.setSelectedIndex(item.index);
        liveEpgItemAdapter.setLiveEpgItemIndex(item);
        int maxTime = (int) TimeUtil.getTime(TimeUtil.timeFormat.format(new Date()) + " " + item.start + ":" + "00", TimeUtil.timeFormat.format(new Date()) + " " + item.end + ":" + "00");
        sBar.setProgress(0);
        sBar.setMax(maxTime * 1000);
        sBar.setKeyProgressIncrement(10000);
        tv_currentpos.setText(TimeUtil.durationToString(0));
        tv_duration.setText(TimeUtil.durationToString(maxTime * 1000));
        isCanBack = true;
        selectTime = 0;
        showProgressBars(false);
        tvName.setVisibility(View.GONE);
        tvBack.setVisibility(View.VISIBLE);
        centerEpgLayout.setVisibility(View.INVISIBLE);
        mHandler.removeCallbacks(mConnectTimeoutChangeSourceRun);
        mHandler.removeCallbacks(backChange);
        mHandler.postDelayed(backChange, 3000);
        mVideoView.release();
        mVideoView.setUrl(String.format(epgLiveChannelItem.getSocUrls(), TimeUtil.getTimeS(startDate) + "GMT-" + TimeUtil.getTimeS(endDate) + "GMT"));
        mVideoView.start();
    }

    private void playBackNext(LiveEpgItem epgItem) {
        LiveEpg liveEpg = ApiConfig.get().getLiveEpg(epgLiveChannelItem, epgItem.currentEpgDate);
        for (int i = 0; i < liveEpg.getEpgItems().size(); i++) {
            if (liveEpg.getEpgItems().get(i).equals(epgItem) && i + 1 < liveEpg.getEpgItems().size() - 1) {
                LiveEpgItem item = liveEpg.getEpgItems().get(i + 1);
                playBack(item);
                return;
            }
        }
        List<LiveEpgDate> epgDateList = ApiConfig.get().getEpgDateList();
        for (int i = 0; i < ApiConfig.get().getEpgDateList().size(); i++) {
            if (TimeUtil.timeFormat.format(epgDateList.get(i).getDateParamVal()).equals(epgItem.currentEpgDate)) {
                LiveEpgItem epgItem1 = ApiConfig.get().getLiveEpg(epgLiveChannelItem, TimeUtil.timeFormat.format(epgDateList.get(i + 1).getDateParamVal())).getEpgItems().get(0);
                playBack(epgItem1);
                return;
            }
        }
        playChannel(currentChannelGroupIndex, currentLiveChannelIndex, false);
    }
}