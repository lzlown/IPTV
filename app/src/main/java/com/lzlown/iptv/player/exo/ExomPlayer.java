package com.lzlown.iptv.player.exo;

import android.content.Context;
import com.lzlown.iptv.base.App;
import com.lzlown.iptv.videoplayer.player.exo.ExoPlayer;

import java.util.Map;

public class ExomPlayer extends ExoPlayer {
    private static final String TAG = ExomPlayer.class.getName();
    private Boolean socket=false;
    public ExomPlayer(Context context) {
        super(context);
    }

    @Override
    public void setDataSource(String path, Map<String, String> headers) {
        if (path.startsWith("rtmp")||path.startsWith("rtsp")) {

        }else {
            socket=true;
            path = App.getProxy().getProxyUrl(path);
        }
        super.setDataSource(path, headers);
    }
    @Override
    public long getTcpSpeed() {
        long tcpSpeed = super.getTcpSpeed();
        if (tcpSpeed>0&& socket){
            return tcpSpeed/2;
        }else {
            return tcpSpeed;
        }
    }
}
