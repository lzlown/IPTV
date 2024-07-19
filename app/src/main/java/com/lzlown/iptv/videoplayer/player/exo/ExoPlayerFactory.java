package com.lzlown.iptv.videoplayer.player.exo;

import android.content.Context;
import com.lzlown.iptv.videoplayer.player.PlayerFactory;


public class ExoPlayerFactory extends PlayerFactory<ExoPlayer> {

    public static ExoPlayerFactory create() {
        return new ExoPlayerFactory();
    }

    @Override
    public ExoPlayer createPlayer(Context context) {
        return new ExoPlayer(context);
    }
}
