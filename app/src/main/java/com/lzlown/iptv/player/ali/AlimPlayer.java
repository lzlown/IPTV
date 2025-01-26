package com.lzlown.iptv.player.ali;

import android.content.Context;
import com.lzlown.iptv.base.App;
import com.lzlown.iptv.videoplayer.player.ali.AliPlayer;
import com.lzlown.iptv.videoplayer.util.PlayerUtils;

import java.util.Map;

public class AlimPlayer extends AliPlayer {
    private static final String TAG = AlimPlayer.class.getName();
    private String url;

    public AlimPlayer(Context context) {
        super(context);
    }

    @Override
    public void setDataSource(String path, Map<String, String> headers) {
        url = path;
        if (path.startsWith("proxy")) {
            path=path.replaceFirst("proxy","http");
            path = App.getProxy().getProxyUrl(path);
        }
        super.setDataSource(path, headers);
    }
}
