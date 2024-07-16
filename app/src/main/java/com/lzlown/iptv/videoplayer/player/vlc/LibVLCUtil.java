package com.lzlown.iptv.videoplayer.player.vlc;

import com.lzlown.iptv.base.App;
import org.videolan.libvlc.LibVLC;

public class LibVLCUtil {

    private static LibVLC libVLC = new LibVLC(App.getInstance().getApplicationContext());

    public static LibVLC getLibVLC() {
        return libVLC;
    }

}
