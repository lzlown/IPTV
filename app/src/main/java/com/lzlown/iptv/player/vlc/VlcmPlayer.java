package com.lzlown.iptv.player.vlc;

import android.content.Context;
import com.lzlown.iptv.api.ApiConfig;
import xyz.doikki.videoplayer.player.vlc.VlcPlayer;

import java.util.List;

public class VlcmPlayer extends VlcPlayer {

    public VlcmPlayer(Context context) {
        super(context);
    }

    @Override
    public void setOptions() {
        super.setOptions();
        List<String> defaultOptions = ApiConfig.get().getVlcOptions().get("default");
        if(null!=defaultOptions){
            getVlcOps().addAll(defaultOptions);
        }
    }
}
