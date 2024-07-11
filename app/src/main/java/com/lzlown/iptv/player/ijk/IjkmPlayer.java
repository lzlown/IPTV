package com.lzlown.iptv.player.ijk;

import android.content.Context;
import android.text.TextUtils;
import com.lzlown.iptv.api.ApiConfig;
import com.lzlown.iptv.bean.IjkOption;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.pragma.DebugLog;
import xyz.doikki.videoplayer.player.ijk.IjkPlayer;

import java.util.List;
import java.util.Map;

public class IjkmPlayer extends IjkPlayer {

    private static final String TAG = IjkmPlayer.class.getName();

    public IjkmPlayer(Context context) {
        super(context);
    }

    @Override
    public void setOptions() {
        super.setOptions();
        List<IjkOption> ijkOptionList = ApiConfig.get().getIjkOptions().get("default");
        if (null != ijkOptionList) {
            for (IjkOption ijkOption : ijkOptionList) {
                mMediaPlayer.setOption(ijkOption.getCategory(), ijkOption.getName(), ijkOption.getValue());
                DebugLog.i(TAG, "setOption: " + ijkOption.getName()+"="+ijkOption.getValue());
            }
        }

        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "subtitle", 1);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_timeout", 60 * 60 * 1000);
    }

    @Override
    public void setDataSource(String path, Map<String, String> headers) {
        try {
            if (path.contains("rtsp") || path.contains("udp") || path.contains("rtp")) {
                mMediaPlayer.setOption(1, "infbuf", 1);
                mMediaPlayer.setOption(1, "rtsp_transport", "tcp");
                mMediaPlayer.setOption(1, "rtsp_flags", "prefer_tcp");
                List<IjkOption> ijkOptionList = ApiConfig.get().getIjkOptions().get("rtsp");
                if (null != ijkOptionList) {
                    for (IjkOption ijkOption : ijkOptionList) {
                        DebugLog.i(TAG, "setOption: " + ijkOption.getName()+"="+ijkOption.getValue());
                        mMediaPlayer.setOption(ijkOption.getCategory(), ijkOption.getName(), ijkOption.getValue());
                    }
                }
            }
//            setDataSourceHeader(headers);
        } catch (Exception ignored) {

        }
        mMediaPlayer.setOption(tv.danmaku.ijk.media.player.IjkMediaPlayer.OPT_CATEGORY_FORMAT, "protocol_whitelist", "ijkio,ffio,async,cache,crypto,file,dash,http,https,ijkhttphook,ijkinject,ijklivehook,ijklongurl,ijksegment,ijktcphook,pipe,rtp,tcp,tls,udp,ijkurlhook,data");
        super.setDataSource(path, headers);

    }

    private void setDataSourceHeader(Map<String, String> headers) {
        if (headers != null && !headers.isEmpty()) {
            String userAgent = headers.get("User-Agent");
            if (!TextUtils.isEmpty(userAgent)) {
                mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "user_agent", userAgent);
                // 移除header中的User-Agent，防止重复
                headers.remove("User-Agent");
            }
            if (headers.size() > 0) {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    String value = entry.getValue();
                    if (!TextUtils.isEmpty(value)) {
                        sb.append(entry.getKey());
                        sb.append(": ");
                        sb.append(value);
                        sb.append("\r\n");
                    }
                }
                mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "headers", sb.toString());
            }
        }
    }
}
