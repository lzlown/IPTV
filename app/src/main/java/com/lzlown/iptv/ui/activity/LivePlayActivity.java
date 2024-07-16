package com.lzlown.iptv.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.IntEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.lzlown.iptv.R;
import com.lzlown.iptv.api.ApiConfig;
import com.lzlown.iptv.base.App;
import com.lzlown.iptv.base.BaseActivity;
import com.lzlown.iptv.bean.*;
import com.lzlown.iptv.player.controller.LiveController;
import com.lzlown.iptv.ui.adapter.LiveChannelGroupAdapter;
import com.lzlown.iptv.ui.adapter.LiveChannelItemAdapter;
import com.lzlown.iptv.ui.adapter.LiveSettingGroupAdapter;
import com.lzlown.iptv.ui.adapter.LiveSettingItemAdapter;
import com.lzlown.iptv.ui.tv.widget.ViewObj;
import com.lzlown.iptv.util.*;
import com.orhanobut.hawk.Hawk;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7LinearLayoutManager;
import org.greenrobot.eventbus.EventBus;
import com.lzlown.iptv.videoplayer.player.VideoView;

import java.text.SimpleDateFormat;
import java.util.*;

public class LivePlayActivity extends BaseActivity {
    private static final String TAG = LivePlayActivity.class.getName();
    public static Context context;
    private VideoView mVideoView;

    private RelativeLayout ll_loading;
    private LinearLayout tvLeftChannelListLayout;
    private TvRecyclerView mChannelGroupView;
    private TvRecyclerView mLiveChannelView;
    private LiveChannelGroupAdapter liveChannelGroupAdapter;
    private LiveChannelItemAdapter liveChannelItemAdapter;

    private LinearLayout tvRightSettingLayout;
    private TvRecyclerView mSettingGroupView;
    private TvRecyclerView mSettingItemView;
    private LiveSettingGroupAdapter liveSettingGroupAdapter;
    private LiveSettingItemAdapter liveSettingItemAdapter;
    private List<LiveSettingGroup> liveSettingGroupList = new ArrayList<>();

    public static int currentChannelGroupIndex = 0;
    private Handler mHandler = new Handler();

    private List<LiveChannelGroup> liveChannelGroupList = new ArrayList<>();
    private int currentLiveChannelIndex = -1;
    private int currentLiveChangeSourceTimes = 0;
    private LiveChannelItem currentLiveChannelItem = null;
    private LivePlayerManager livePlayerManager = new LivePlayerManager();

    private long mExitTime = 0;
    private boolean loadEnd = false;

    private RelativeLayout ll_epg;
    //右边显示
    private TextView tvName;
    private TextView tvTime;
    private TextView tvSpeed;

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

        ll_loading = findViewById(R.id.ll_loading);
        ll_loading.setVisibility(View.GONE);

        //界面 view
        tvLeftChannelListLayout = findViewById(R.id.tvLeftChannnelListLayout);
        mChannelGroupView = findViewById(R.id.mGroupGridView);
        mLiveChannelView = findViewById(R.id.mChannelGridView);
        tvRightSettingLayout = findViewById(R.id.tvRightSettingLayout);
        mSettingGroupView = findViewById(R.id.mSettingGroupView);
        mSettingItemView = findViewById(R.id.mSettingItemView);

        //中间EPG
        ll_epg = findViewById(R.id.ll_epg);
        ll_epg.setVisibility(View.INVISIBLE);

        //右边显示
        tvName = findViewById(R.id.tvName);
        tvTime = findViewById(R.id.tvTime);
        tvSpeed = findViewById(R.id.tvSpeed);

        initVideoView();
        initChannelGroupView();
        initLiveChannelView();
        initLiveChannelList();
        initSettingGroupView();
        initSettingItemView();
        initLiveSettingGroupList();
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
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
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
            ll_loading.setVisibility(View.VISIBLE);
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
        if (tvLeftChannelListLayout.getVisibility() == View.INVISIBLE) {
            //重新载入上一次状态
            liveChannelItemAdapter.setNewData(getLiveChannels(currentChannelGroupIndex));
            if (currentLiveChannelIndex > -1)
                mLiveChannelView.scrollToPosition(currentLiveChannelIndex);
            mLiveChannelView.setSelection(currentLiveChannelIndex);
            mChannelGroupView.scrollToPosition(currentChannelGroupIndex);
            mChannelGroupView.setSelection(currentChannelGroupIndex);
            mHandler.postDelayed(mFocusCurrentChannelAndShowChannelList, 200);
            ll_epg.setVisibility(View.INVISIBLE);
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
        }
    };

    private void playChannel(int channelGroupIndex, int liveChannelIndex, boolean changeSource) {
        if ((channelGroupIndex == currentChannelGroupIndex && liveChannelIndex == currentLiveChannelIndex && !changeSource)
                || (changeSource && currentLiveChannelItem.getSourceNum() == 1)) {
            return;
        }
        if (!changeSource) {
            currentChannelGroupIndex = channelGroupIndex;
            currentLiveChannelIndex = liveChannelIndex;
            currentLiveChannelItem = getLiveChannels(currentChannelGroupIndex).get(currentLiveChannelIndex);
            Hawk.put(HawkConfig.LIVE_GROUP, currentChannelGroupIndex);
            Hawk.put(HawkConfig.LIVE_CHANNEL, currentLiveChannelItem.getChannelName());
            tvName.setText(currentLiveChannelItem.getChannelNum() + " " + currentLiveChannelItem.getChannelName());
            tvName.setVisibility(View.VISIBLE);
        }
        Log.i(TAG,"playChannel"+currentChannelGroupIndex + currentLiveChannelItem.getChannelName()+currentLiveChannelItem.getSourceIndex());
        livePlayerManager.getLiveChannelPlayer(mVideoView, currentChannelGroupIndex + currentLiveChannelItem.getChannelName()+currentLiveChannelItem.getSourceIndex());
        ll_loading.setVisibility(View.VISIBLE);
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
                        ll_loading.setVisibility(View.INVISIBLE);
                        tvName.setVisibility(View.INVISIBLE);
                        break;
                    case VideoView.STATE_ERROR:
                        mHandler.removeCallbacks(mConnectTimeoutChangeSourceRun);
                        mHandler.postDelayed(mConnectTimeoutChangeSourceRun, 10 * 1000);
                        break;
                    case VideoView.STATE_PLAYBACK_COMPLETED:
                        mHandler.removeCallbacks(mConnectTimeoutChangeSourceRun);
                        mHandler.postDelayed(mConnectTimeoutChangeSourceRun, 3 * 1000);
                        break;
                    case VideoView.STATE_PREPARING:
                    case VideoView.STATE_BUFFERING:
                        mHandler.removeCallbacks(mConnectTimeoutChangeSourceRun);
                        mHandler.postDelayed(mConnectTimeoutChangeSourceRun, (Hawk.get(HawkConfig.LIVE_CONNECT_TIMEOUT, 10)) * 1000);
                        break;
                }
            }

            @Override
            public void changeSource(int direction) {
                if (direction > 0)
                    playNextSource();
                else
                    playPreSource();
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
                currentLiveChangeSourceTimes = 0;
                Integer[] groupChannelIndex = getNextChannel(Hawk.get(HawkConfig.LIVE_CHANNEL_REVERSE, false) ? -1 : 1);
                playChannel(groupChannelIndex[0], groupChannelIndex[1], false);
//            } else {
//                playNextSource();
//            }
        }
    };

    private void initChannelGroupView() {
        mChannelGroupView.setHasFixedSize(true);
        mChannelGroupView.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));
        liveChannelGroupAdapter = new LiveChannelGroupAdapter();
        liveChannelGroupAdapter.closeLoadAnimation();
        mChannelGroupView.setAdapter(liveChannelGroupAdapter);
        mChannelGroupView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
            }
        });

        //电视
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

        //手机/模拟器
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
        mLiveChannelView.setHasFixedSize(true);
        mLiveChannelView.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));
        liveChannelItemAdapter = new LiveChannelItemAdapter();
        liveChannelItemAdapter.closeLoadAnimation();
        mLiveChannelView.setAdapter(liveChannelItemAdapter);
        mLiveChannelView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
            }
        });

        //电视
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

        //手机/模拟器
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

    private boolean isListOrSettingLayoutVisible() {
        return tvLeftChannelListLayout.getVisibility() == View.VISIBLE || tvRightSettingLayout.getVisibility() == View.VISIBLE;
    }

    private void initLiveSettingGroupList() {
        ArrayList<ArrayList<String>> itemsArrayList = new ArrayList<>();
        ArrayList<String> groupNames = new ArrayList<>(Arrays.asList("画质选择","画面比例", "播放解码", "偏好设置"));
        ArrayList<String> sourceItems = new ArrayList<>();
        ArrayList<String> playerDecoderItems = new ArrayList<>(Arrays.asList("IJK", "VLC"));
        ArrayList<String> scaleItems = new ArrayList<>(Arrays.asList("默认", "16:9", "4:3", "填充", "原始", "裁剪"));
        ArrayList<String> personalSettingItems = new ArrayList<>(Arrays.asList("显示时间", "显示网速", "显示预告","清理缓存"));

        itemsArrayList.add(sourceItems);
        itemsArrayList.add(scaleItems);
        itemsArrayList.add(playerDecoderItems);
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
        liveSettingGroupList.get(3).getLiveSettingItems().get(0).setItemSelected(Hawk.get(HawkConfig.LIVE_SHOW_TIME, false));
        liveSettingGroupList.get(3).getLiveSettingItems().get(1).setItemSelected(Hawk.get(HawkConfig.LIVE_SHOW_SPEED, false));
        liveSettingGroupList.get(3).getLiveSettingItems().get(2).setItemSelected(Hawk.get(HawkConfig.LIVE_SHOW_EPG, false));
    }

    //显示设置列表
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
        mSettingGroupView.setHasFixedSize(true);
        mSettingGroupView.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));

        liveSettingGroupAdapter = new LiveSettingGroupAdapter();
        mSettingGroupView.setAdapter(liveSettingGroupAdapter);
        mSettingGroupView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mHandler.removeCallbacks(mHideSettingLayoutRun);
                mHandler.postDelayed(mHideSettingLayoutRun, 5000);
            }
        });

        //电视
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

        //手机/模拟器
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
            case 2:
                liveSettingItemAdapter.selectItem(livePlayerManager.getLivePlayerType(), true, true);
                break;
        }
        int scrollToPosition = liveSettingItemAdapter.getSelectedItemIndex();
        if (scrollToPosition < 0) scrollToPosition = 0;
        mSettingItemView.scrollToPosition(scrollToPosition);
        mHandler.removeCallbacks(mHideSettingLayoutRun);
        mHandler.postDelayed(mHideSettingLayoutRun, 5000);
    }

    private void initSettingItemView() {
        mSettingItemView.setHasFixedSize(true);
        mSettingItemView.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));

        liveSettingItemAdapter = new LiveSettingItemAdapter();
        mSettingItemView.setAdapter(liveSettingItemAdapter);
        mSettingItemView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mHandler.removeCallbacks(mHideSettingLayoutRun);
                mHandler.postDelayed(mHideSettingLayoutRun, 5000);
            }
        });

        //电视
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

        //手机/模拟器
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
        if (settingGroupIndex < 3) {
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
                livePlayerManager.changeLivePlayerScale(mVideoView, position, currentChannelGroupIndex + currentLiveChannelItem.getChannelName()+currentLiveChannelItem.getSourceIndex());
                break;
            case 2://播放器
                ll_loading.setVisibility(View.VISIBLE);
                mVideoView.release();
                livePlayerManager.changeLivePlayerType(mVideoView, position, currentChannelGroupIndex + currentLiveChannelItem.getChannelName()+currentLiveChannelItem.getSourceIndex());
                mVideoView.start();
                break;
            case 3://偏好设置
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
                    tip_time2.setText(liveEpgItem1.getStart() + "-" + liveEpgItem1.getEnd());
                    tip_name2.setText(liveEpgItem1.getTitle());
                } else {
                    tip_time2.setText("00:00-00:00");
                    tip_name2.setText("暂无预告");
                }
                ll_epg.setVisibility(View.VISIBLE);
                mHandler.postDelayed(mHideEpgListRun, 5000);
            } else {
                ll_epg.setVisibility(View.INVISIBLE);
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
            ll_epg.setVisibility(View.INVISIBLE);
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
}