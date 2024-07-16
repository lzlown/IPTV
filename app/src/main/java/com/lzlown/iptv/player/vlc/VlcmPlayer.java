package com.lzlown.iptv.player.vlc;

import android.content.Context;
import com.lzlown.iptv.api.ApiConfig;
import tv.danmaku.ijk.media.player.pragma.DebugLog;
import com.lzlown.iptv.videoplayer.player.vlc.VlcPlayer;

import java.util.List;

public class VlcmPlayer extends VlcPlayer {
    private static final String TAG = VlcmPlayer.class.getName();
    public VlcmPlayer(Context context) {
        super(context);
    }

    @Override
    public void setOptions() {
        List<String> defaultOptions = ApiConfig.get().getVlcOptions().get("default");
        if (null != defaultOptions) {
            for (String ops : defaultOptions) {
                mMediaPlayer.setOption(ops);
                DebugLog.i(TAG, "setOption: " + ops);
            }
        }
    }
}
