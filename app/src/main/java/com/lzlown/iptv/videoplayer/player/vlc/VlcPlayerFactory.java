package com.lzlown.iptv.videoplayer.player.vlc;

import android.content.Context;
import com.lzlown.iptv.videoplayer.player.PlayerFactory;

public class VlcPlayerFactory extends PlayerFactory<VlcPlayer > {

    public static VlcPlayerFactory create() {
        return new VlcPlayerFactory();
    }

    @Override
    public VlcPlayer createPlayer(Context context) {
        return new VlcPlayer(context);
    }
}
