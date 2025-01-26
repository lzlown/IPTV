package com.lzlown.iptv.videoplayer.player.ali;

import android.content.Context;
import com.lzlown.iptv.videoplayer.player.PlayerFactory;

public class AliPlayerFactory extends PlayerFactory<AliPlayer> {

    public static AliPlayerFactory create() {
        return new AliPlayerFactory();
    }

    @Override
    public AliPlayer createPlayer(Context context) {
        return new AliPlayer(context);
    }
}
