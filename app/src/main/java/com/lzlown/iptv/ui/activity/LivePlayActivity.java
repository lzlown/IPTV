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
    private TvRecyclerView mLiveChannelView;
    private LiveChannelGroupAdapter liveChannelGroupAdapter;
    private LiveChannelItemAdapter liveChannelItemAdapter;
    private final List<LiveChannelGroup> liveChannelGroupList = new ArrayList<>();
    private int currentChannelGroupIndex = 0;
    private int currentLiveChannelIndex = -1;
    private int currentLiveChangeSourceTimes = 0;
    private LiveChannelItem currentLiveChannelItem = null;

    //设置列表
    private LinearLayout tvRightSettingLayout;
    private TvRecyclerView mSettingGroupView;
    private TvRecyclerView mSettingItemView;
    private LiveSettingGroupAdapter liveSettingGroupAdapter;
    private LiveSettingItemAdapter liveSettingItemAdapter;
    private final List<LiveSettingGroup> liveSettingGroupList = new ArrayList<>();

    private long mExitTime = 0;
    private boolean loadEnd = false;

    //中间EPG
    private RelativeLayout centerEpgLayout;

    //右边显示
    private TextView tvName;
    private TextView tvTime;
    private TextView tvSpeed;
    //显示回放
    private TextView tvBack;

    //回放显示列表
    private LinearLayout mEpgLayout;
    private TvRecyclerView mEpgChannelGridView;
    private TvRecyclerView mEpgDateGridView;
    private TvRecyclerView mEpgListView;
    private LiveEpgAdapter liveEpgAdapter;
    private LiveEpgDateAdapter liveEpgDateAdapter;
    private LiveEpgChannelItemAdapter liveEpgChannelItemAdapter;

    //回放控制
    private View backController;
    private SeekBar sBar;
    private TextView tv_currentpos;
    private TextView tv_duration;

    private Boolean isCanBack = false;
    private LiveChannelItem backLiveChannelItem;
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
        mLiveChannelView = findViewById(R.id.mChannelGridView);
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
        mEpgLayout = findViewById(R.id.divEPG);
        mEpgChannelGridView = findViewById(R.id.mEpgChannelGridView);
        mEpgDateGridView = findViewById(R.id.mEpgDateGridView);
        mEpgListView = findViewById(R.id.lv_epg);

        sBar = findViewById(R.id.pb_progressbar);
        tv_currentpos = findViewById(R.id.tv_currentpos);
        backController = findViewById(R.id.backcontroller);
        tv_duration = findViewById(R.id.tv_duration);
        backController.setVisibility(View.GONE);
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
                    if (tvBack.getVisibility() == View.VISIBLE) {
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

        initEpgChannelView();
        initEpgDateView();
        initEpgListView();
    }

    @Override
    public void onBackPressed() {
        if (tvLeftChannelListLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideChannelListRun);
            mHandler.post(mHideChannelListRun);
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
                        if (backController.getVisibility() != View.VISIBLE) {
                            showEpgMenu(true);
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        if (isCanBack && mVideoView.isPlaying()) {
                            showProgressBars(true);
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
        if (mEpgLayout.getVisibility() == View.VISIBLE) {
            mEpgLayout.setVisibility(View.GONE);
            return;
        }
        if (tvLeftChannelListLayout.getVisibility() == View.INVISIBLE) {
            //重新载入上一次状态
            liveChannelItemAdapter.setNewData(getLiveChannels(currentChannelGroupIndex));
            if (currentLiveChannelIndex > -1)
                mLiveChannelView.scrollToPosition(currentLiveChannelIndex);
            mLiveChannelView.setSelection(currentLiveChannelIndex);
            mChannelGroupView.scrollToPosition(currentChannelGroupIndex);
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
            if (mChannelGroupView.isScrolling() || mLiveChannelView.isScrolling() || mChannelGroupView.isComputingLayout() || mLiveChannelView.isComputingLayout()) {
                mHandler.postDelayed(this, 100);
            } else {
                liveChannelGroupAdapter.setSelectedGroupIndex(currentChannelGroupIndex);
                liveChannelItemAdapter.setSelectedChannelIndex(currentLiveChannelIndex);
                RecyclerView.ViewHolder holder = mLiveChannelView.findViewHolderForAdapterPosition(currentLiveChannelIndex);
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
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) tvLeftChannelListLayout.getLayoutParams();
            if (tvLeftChannelListLayout.getVisibility() == View.VISIBLE) {
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
            if (mEpgLayout.getVisibility() == View.VISIBLE) {
                mEpgLayout.setVisibility(View.GONE);
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
        if (tvBack.getVisibility() == View.VISIBLE) {
            tvBack.setVisibility(View.GONE);
            backLiveChannelItem = null;
            selectedData = null;
            if (liveEpgAdapter != null) {
                liveEpgAdapter.setLiveEpgItemIndex(null);
            }
        }
        selectTime = 0;
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
//            currentLiveChangeSourceTimes++;
//            if (currentLiveChannelItem.getSourceNum() == currentLiveChangeSourceTimes) {
//                currentLiveChangeSourceTimes = 0;
//                Integer[] groupChannelIndex = getNextChannel(Hawk.get(HawkConfig.LIVE_CHANNEL_REVERSE, false) ? -1 : 1);
//                playChannel(groupChannelIndex[0], groupChannelIndex[1], false);
//            } else {
//                playNextSource();
//            }
            if (tvBack.getVisibility() == View.VISIBLE) {
                playChannel(currentChannelGroupIndex, currentLiveChannelIndex, false);
                return;
            }
            currentLiveChangeSourceTimes = 0;
            Integer[] groupChannelIndex = getNextChannel(Hawk.get(HawkConfig.LIVE_CHANNEL_REVERSE, false) ? -1 : 1);
            playChannel(groupChannelIndex[0], groupChannelIndex[1], false);

        }
    };

    private boolean isListOrSettingLayoutVisible() {
        return tvLeftChannelListLayout.getVisibility() == View.VISIBLE || tvRightSettingLayout.getVisibility() == View.VISIBLE || mEpgLayout.getVisibility() == View.VISIBLE|| backController.getVisibility() == View.VISIBLE;
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
        initTvRecyclerView(mLiveChannelView, liveChannelItemAdapter, mHideChannelListRun);
        mLiveChannelView.setOnItemListener(new TvRecyclerView.OnItemListener() {
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
            return;
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
        liveSettingGroupList.get(2).getLiveSettingItems().get(0).setItemSelected(Hawk.get(HawkConfig.LIVE_SHOW_TIME, false));
        liveSettingGroupList.get(2).getLiveSettingItems().get(1).setItemSelected(Hawk.get(HawkConfig.LIVE_SHOW_SPEED, false));
        liveSettingGroupList.get(2).getLiveSettingItems().get(2).setItemSelected(Hawk.get(HawkConfig.LIVE_SHOW_EPG, false));
    }

    private void showSettingGroup() {
        if (tvLeftChannelListLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideChannelListRun);
            mHandler.post(mHideChannelListRun);
        }
        tvName.setVisibility(View.INVISIBLE);
        if (tvRightSettingLayout.getVisibility() == View.INVISIBLE) {
            if (!isCurrentLiveChannelValid()) return;
            loadCurrentSourceList();
            liveSettingGroupAdapter.setNewData(liveSettingGroupList);
            selectSettingGroup(0, false);
            mSettingGroupView.scrollToPosition(0);
            mSettingItemView.scrollToPosition(currentLiveChannelItem.getSourceIndex());
            mHandler.postDelayed(mFocusAndShowSettingGroup, 200);
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
                        break;
                    case 3:
                        App.getInstance().cleanParams();
                        Toast.makeText(App.getInstance(), "缓存清理完成", Toast.LENGTH_SHORT).show();
                        break;
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
                tvRightSettingLayout.setVisibility(View.VISIBLE);
                tvTime.setVisibility(View.GONE);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) tvRightSettingLayout.getLayoutParams();
                if (tvRightSettingLayout.getVisibility() == View.VISIBLE) {
                    ViewObj viewObj = new ViewObj(tvRightSettingLayout, params);
                    ObjectAnimator animator = ObjectAnimator.ofObject(viewObj, "marginRight", new IntEvaluator(), -tvRightSettingLayout.getLayoutParams().width, 0);
                    animator.setDuration(200);
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mHandler.postDelayed(mHideSettingLayoutRun, 5000);
                        }
                    });
                    animator.start();
                }
            }
        }
    };

    private final Runnable mHideSettingLayoutRun = new Runnable() {
        @Override
        public void run() {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) tvRightSettingLayout.getLayoutParams();
            if (tvRightSettingLayout.getVisibility() == View.VISIBLE) {
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
            if (currentLiveChannelIndex > -1)
                mLiveChannelView.scrollToPosition(currentLiveChannelIndex);
            liveChannelItemAdapter.setSelectedChannelIndex(currentLiveChannelIndex);
        } else {
            mLiveChannelView.scrollToPosition(0);
            liveChannelItemAdapter.setSelectedChannelIndex(-1);
        }

        if (liveChannelIndex > -1) {
            clickLiveChannel(liveChannelIndex);
            mChannelGroupView.scrollToPosition(groupIndex);
            mLiveChannelView.scrollToPosition(liveChannelIndex);
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
        if (Hawk.get(HawkConfig.LIVE_SHOW_EPG, false)) {
            if (tvLeftChannelListLayout.getVisibility() != View.VISIBLE || tvRightSettingLayout.getVisibility() != View.VISIBLE) {
                mHandler.removeCallbacks(mHideEpgListRun);
                ((TextView) findViewById(R.id.tv_channel_bar_name)).setText(currentLiveChannelItem.getChannelName());//底部名称
                TextView tip_time1 = findViewById(R.id.tv_current_program_time);//底部EPG当前节目信息
                TextView tip_time2 = findViewById(R.id.tv_next_program_time);//底部EPG当下个节目信息
                TextView tip_name1 = findViewById(R.id.tv_current_program_name);//底部EPG当前节目信息
                TextView tip_name2 = findViewById(R.id.tv_next_program_name);//底部EPG当前节目信息
                Map<String, LiveEpgItem> liveEpgItemForMap = ApiConfig.get().getLiveEpgItemForMap(currentLiveChannelItem.getChannelCh());
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


    public void showProgressBars(boolean show) {
        if (show) {
            sBar.requestFocus();
            if (tvBack.getVisibility() != View.VISIBLE) {
                sBar.setMax((int) mVideoView.getDuration());
                sBar.setKeyProgressIncrement(10000);
            }
            tv_currentpos.setText(TimeUtil.durationToString((int) mVideoView.getCurrentPosition() + selectTime));
            tv_duration.setText(TimeUtil.durationToString((int) mVideoView.getDuration() + selectTime));
            backController.setVisibility(View.VISIBLE);
            if (countDownTimer == null) {
                countDownTimer = new CountDownTimer(5000, 1000) {
                    @Override
                    public void onTick(long arg0) {
                        if (mVideoView != null&& mVideoView.isPlaying()) {
                            sBar.setProgress((int) mVideoView.getCurrentPosition() + selectTime);
                            tv_currentpos.setText(TimeUtil.durationToString((int) mVideoView.getCurrentPosition() + selectTime));
                        }
                    }

                    @Override
                    public void onFinish() {
                        if (backController.getVisibility() == View.VISIBLE) {
                            backController.setVisibility(View.GONE);
                        }
                    }
                };
            } else {
                countDownTimer.cancel();
            }
            countDownTimer.start();
        } else {
            backController.setVisibility(View.GONE);
        }

    }

    //预告回放
    private void initEpgChannelView() {
        liveEpgChannelItemAdapter = new LiveEpgChannelItemAdapter();
        liveEpgChannelItemAdapter.setNewData(ApiConfig.get().getLiveChannelList());
        initTvRecyclerView(mEpgChannelGridView, liveEpgChannelItemAdapter, mHideChannelListRun);
        mEpgChannelGridView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
                if (mEpgChannelGridView.isComputingLayout() || position == -1) {
                    return;
                }
                if (position == liveEpgChannelItemAdapter.getSelectedChannelIndex()) {
                    liveEpgChannelItemAdapter.setFocusedChannelIndex(position);
                    return;
                }
                liveEpgChannelItemAdapter.setFocusedChannelIndex(position);
                liveEpgChannelItemAdapter.setSelectedChannelIndex(position);
                changeEpgRight(position);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {

            }
        });
        liveEpgChannelItemAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
                if (position == liveEpgChannelItemAdapter.getSelectedChannelIndex()) {
                    return;
                }
                liveEpgChannelItemAdapter.setSelectedChannelIndex(position);
                changeEpgRight(position);
            }
        });
    }

    private void initEpgDateView() {
        liveEpgDateAdapter = new LiveEpgDateAdapter();
        initTvRecyclerView(mEpgDateGridView, liveEpgDateAdapter, mHideChannelListRun);
        mEpgDateGridView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
                if (position == -1) {
                    return;
                }
                if (position == liveEpgDateAdapter.getSelectedIndex()) {
                    liveEpgDateAdapter.setFocusedIndex(position);
                    return;
                }
                liveEpgDateAdapter.setFocusedIndex(position);
                liveEpgDateAdapter.setSelectedIndex(position);
                epgRight(position);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {

            }
        });
        liveEpgDateAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
                liveEpgDateAdapter.setSelectedIndex(position);
                epgRight(position);
            }
        });
    }

    private void initEpgListView() {
        liveEpgAdapter = new LiveEpgAdapter();
        initTvRecyclerView(mEpgListView, liveEpgAdapter, mHideChannelListRun);
        mEpgListView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {

            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
                liveEpgAdapter.setFocusedEpgIndex(position);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {

            }
        });
        liveEpgAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
                selectedData = liveEpgAdapter.getItem(position);
                if (selectedData == null) {
                    return;
                }
                String startDate = selectedData.currentEpgDate.replaceAll("-", "") + selectedData.originStart.replace(":", "") + "00";
                String endDate = selectedData.currentEpgDate.replaceAll("-", "") + selectedData.originEnd.replace(":", "") + "00";
                Date now = new Date();
                Date ctime = TimeUtil.getTime(selectedData.currentEpgDate);
                Date time = TimeUtil.getTime(TimeUtil.getTime());
                if (ctime.compareTo(time) <= 0) {
                    if (ctime.compareTo(time) == 0 && now.compareTo(selectedData.startdateTime) >= 0 && now.compareTo(selectedData.enddateTime) <= 0) {
                        return;
                    }
                    if (ctime.compareTo(time) == 0 && now.compareTo(selectedData.startdateTime) <= 0) {
                        return;
                    }
                    Date breTime = TimeUtil.getTime(TimeUtil.getTime(-1));
                    if (ctime.compareTo(breTime) >= 0) {
                        LiveChannelItem currentLiveChannel = liveEpgChannelItemAdapter.getItem(liveEpgChannelItemAdapter.getSelectedChannelIndex());
                        if (currentLiveChannel == null) {
                            return;
                        }
                        if (StringUtils.isNotEmpty(currentLiveChannel.getSocUrls())) {
                            liveEpgAdapter.setLiveEpgItemIndex(selectedData);
                            backLiveChannelItem = currentLiveChannel;
                            int maxTime = (int) TimeUtil.getTime(TimeUtil.timeFormat.format(new Date()) + " " + selectedData.start + ":" + "00", TimeUtil.timeFormat.format(new Date()) + " " + selectedData.end + ":" + "00");
                            sBar.setMax(maxTime * 1000);
                            sBar.setProgress(0);
                            sBar.setKeyProgressIncrement(10000);
                            tv_currentpos.setText(TimeUtil.durationToString(0));
                            tv_duration.setText(TimeUtil.durationToString(maxTime * 1000));
                            isCanBack = true;
                            selectTime = 0;
                            mVideoView.release();
                            mVideoView.setUrl(String.format(currentLiveChannel.getSocUrls(), TimeUtil.getTimeS(startDate) + "GMT-" + TimeUtil.getTimeS(endDate) + "GMT"));
                            mVideoView.start();
                            showProgressBars(false);
                            tvBack.setVisibility(View.VISIBLE);
                            mHandler.removeCallbacks(backChange);
                            mHandler.postDelayed(backChange, 5000);
                        }
                    }
                }
            }
        });
    }

    private void showEpgMenu(boolean focus) {
        if (!isListOrSettingLayoutVisible()) {
            int pos;
            if (backLiveChannelItem != null) {
                pos = backLiveChannelItem.getChannelNum() - 1;
            } else {
                pos = currentLiveChannelItem.getChannelNum() - 1;
            }
            if (focus) {
                liveEpgChannelItemAdapter.setSelectedChannelIndex(-1);
                liveEpgChannelItemAdapter.setFocusedChannelIndex(pos);
            } else {
                liveEpgChannelItemAdapter.setSelectedChannelIndex(pos);
                liveEpgChannelItemAdapter.setFocusedChannelIndex(-1);
            }
            changeEpgRight(pos);
            liveEpgChannelItemAdapter.setNewData(ApiConfig.get().getLiveChannelList());
            mEpgChannelGridView.scrollToPosition(pos);
            mEpgChannelGridView.setSelection(pos);
            mEpgLayout.setVisibility(View.VISIBLE);
            mHandler.removeCallbacks(mHideChannelListRun);
            mHandler.postDelayed(mHideChannelListRun, 5000);

        }
    }

    public void changeEpgRight(int ops) {
        int pos = 6;
        List<LiveEpgDate> epgDateList = ApiConfig.get().getEpgDateList();
        liveEpgDateAdapter.setNewData(epgDateList);
        liveEpgDateAdapter.setSelectedIndex(pos);
        liveEpgDateAdapter.setFocusedIndex(-1);
        mEpgDateGridView.setSelectedPosition(pos);

        LiveChannelItem currentLiveChannel = ApiConfig.get().getLiveChannelList().get(ops);
        String format = TimeUtil.timeFormat.format(epgDateList.get(pos).getDateParamVal());
        LiveEpg liveEpg = ApiConfig.get().getLiveEpg(currentLiveChannel.getChannelCh(), format);
        if (liveEpg != null) {
            showEpg(format, liveEpg.getEpgItems());
        } else {
            showEpg(format, new ArrayList<>());
        }
    }

    private void epgRight(int pos) {
        LiveChannelItem currentLiveChannel = liveEpgChannelItemAdapter.getItem(liveEpgChannelItemAdapter.getSelectedChannelIndex());
        String format = TimeUtil.timeFormat.format(liveEpgDateAdapter.getData().get(pos).getDateParamVal());
        LiveEpg liveEpg = ApiConfig.get().getLiveEpg(currentLiveChannel.getChannelCh(), format);
        if (liveEpg != null) {
            showEpg(format, liveEpg.getEpgItems());
        } else {
            showEpg(format, new ArrayList<>());
        }
    }

    private void showEpg(String date, List<LiveEpgItem> arrayList) {
        if (arrayList != null && !arrayList.isEmpty()) {
            mEpgListView.setVisibility(View.VISIBLE);
            liveEpgAdapter.setLiveEpgItemIndex(selectedData);
            liveEpgAdapter.setNewData(arrayList);
            if (date.equals(TimeUtil.getTime())) {
                int i = -1;
                int size = arrayList.size() - 1;
                while (size >= 0) {
                    if (new Date().compareTo(arrayList.get(size).startdateTime) >= 0) {
                        break;
                    }
                    size--;
                }
                i = size;
                if (i >= 0 && new Date().compareTo(arrayList.get(i).enddateTime) <= 0) {
                    liveEpgAdapter.setSelectedEpgIndex(i);
                    int finalI = i;
                    mEpgListView.post(new Runnable() {
                        @Override
                        public void run() {
                            mEpgListView.smoothScrollToPosition(finalI);
                        }
                    });
                }
            } else {
                mEpgListView.scrollToPosition(0);
            }

        } else {
            liveEpgAdapter.setNewData(new ArrayList<>());
            mEpgListView.setVisibility(View.INVISIBLE);
        }
    }

    private final Runnable backChange = new Runnable() {
        @Override
        public void run() {
            if (tvBack.getVisibility() == View.VISIBLE) {
                if (mVideoView != null && mVideoView.getCurrentPosition() > mVideoView.getDuration()) {
                    playChannel(currentChannelGroupIndex, currentLiveChannelIndex, false);
                    return;
                }
                mHandler.postDelayed(this, 5000);
            }
        }
    };

    private void playBack(int time) {
        selectTime = time;
        String startDate = selectedData.currentEpgDate.replaceAll("-", "") + selectedData.originStart.replaceAll(":", "") + "00";
        startDate = TimeUtil.getTimeS(startDate, selectTime);
        String endDate = selectedData.currentEpgDate.replaceAll("-", "") + selectedData.originEnd.replace(":", "") + "00";
        mVideoView.release();
        mVideoView.setUrl(String.format(backLiveChannelItem.getSocUrls(), TimeUtil.getTimeS(startDate) + "GMT-" + TimeUtil.getTimeS(endDate) + "GMT"));
        mVideoView.start();
        tv_currentpos.setText(TimeUtil.durationToString(selectTime));
        countDownTimer.cancel();
        countDownTimer.start();
    }

}