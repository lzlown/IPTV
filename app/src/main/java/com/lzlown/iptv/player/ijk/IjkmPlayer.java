package com.lzlown.iptv.player.ijk;

import android.content.Context;
import com.lzlown.iptv.config.PlayerConfig;
import com.lzlown.iptv.base.App;
import com.lzlown.iptv.bean.LivePlayerOption;
import com.lzlown.iptv.videoplayer.player.ijk.IjkPlayer;
import com.lzlown.iptv.videoplayer.util.PlayerUtils;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import java.util.List;
import java.util.Map;

public class IjkmPlayer extends IjkPlayer {
    private static final String TAG = IjkmPlayer.class.getName();
    private String url;

    public IjkmPlayer(Context context) {
        super(context);
    }


    @Override
    public void setOptions() {
        super.setOptions();
        List<LivePlayerOption> livePlayerOptionList = PlayerConfig.get().getIjkOptions().get("default");
        if (null != livePlayerOptionList) {
            for (LivePlayerOption livePlayerOption : livePlayerOptionList) {
                mMediaPlayer.setOption(livePlayerOption.getCategory(), livePlayerOption.getName(), livePlayerOption.getValue());
            }
        }
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "subtitle", 1);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);
    }

    @Override
    public void setDataSource(String path, Map<String, String> headers) {
        url = path;
        if (path.startsWith("proxy")) {
            path=path.replaceFirst("proxy","http");
            mMediaPlayer.setOption(1, "infbuf", 1);
            path = App.getProxy().getProxyUrl(path);
        }
        if (path.contains("rtsp") || path.contains("udp") || path.contains("rtp")) {
            mMediaPlayer.setOption(1, "infbuf", 1);
            mMediaPlayer.setOption(1, "rtsp_transport", "tcp");
            mMediaPlayer.setOption(1, "rtsp_flags", "prefer_tcp");
            List<LivePlayerOption> livePlayerOptionList = PlayerConfig.get().getIjkOptions().get("rtsp");
            if (null != livePlayerOptionList) {
                for (LivePlayerOption livePlayerOption : livePlayerOptionList) {
                    mMediaPlayer.setOption(livePlayerOption.getCategory(), livePlayerOption.getName(), livePlayerOption.getValue());
                }
            }
        }
        mMediaPlayer.setOption(tv.danmaku.ijk.media.player.IjkMediaPlayer.OPT_CATEGORY_FORMAT, "protocol_whitelist", "ijkio,ffio,async,cache,crypto,file,dash,http,https,ijkhttphook,ijkinject,ijklivehook,ijklongurl,ijksegment,ijktcphook,pipe,rtp,tcp,tls,udp,ijkurlhook,data");
        super.setDataSource(path, headers);
    }

    @Override
    public long getTcpSpeed() {
        if (url.contains("rtsp") || url.contains("udp") || url.contains("rtp")) {
            return PlayerUtils.getNetSpeed(mAppContext);
        } else {
            return super.getTcpSpeed();
        }
    }
}
