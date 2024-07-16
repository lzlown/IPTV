package com.lzlown.iptv.videoplayer.player.ijk;

import android.content.Context;

import com.lzlown.iptv.videoplayer.player.PlayerFactory;

public class IjkPlayerFactory extends PlayerFactory<IjkPlayer> {

    public static IjkPlayerFactory create() {
        return new IjkPlayerFactory();
    }

    @Override
    public IjkPlayer createPlayer(Context context) {
        return new IjkPlayer(context);
    }
}
