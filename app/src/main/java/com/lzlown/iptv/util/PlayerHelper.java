package com.lzlown.iptv.util;

import android.content.Context;
import com.lzlown.iptv.player.ijk.IjkmPlayer;
import com.lzlown.iptv.videoplayer.player.PlayerFactory;
import com.lzlown.iptv.videoplayer.player.VideoView;
import com.lzlown.iptv.videoplayer.player.android.AndroidMediaPlayerFactory;
import com.lzlown.iptv.videoplayer.render.RenderViewFactory;
import com.lzlown.iptv.videoplayer.render.SurfaceRenderViewFactory;
import com.lzlown.iptv.videoplayer.render.TextureRenderViewFactory;
import com.orhanobut.hawk.Hawk;
import org.json.JSONException;
import org.json.JSONObject;
import tv.danmaku.ijk.media.player.IjkLibLoader;

import java.text.DecimalFormat;

public class PlayerHelper {
    public static void updateCfg(VideoView videoView, JSONObject playerCfg) {
        updateCfg(videoView, playerCfg, -1);
    }

    public static void updateCfg(VideoView videoView, JSONObject playerCfg, int forcePlayerType) {
        int playerType = Hawk.get(HawkConfig.PLAY_TYPE, 0);
        int renderType = Hawk.get(HawkConfig.PLAY_RENDER, 1);
        int scale = Hawk.get(HawkConfig.PLAY_SCALE, 0);
        try {
            playerType = playerCfg.getInt("pl");
            renderType = playerCfg.getInt("pr");
            scale = playerCfg.getInt("sc");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (forcePlayerType >= 0) playerType = forcePlayerType;
        PlayerFactory playerFactory;
        if (playerType == 0) {
            playerFactory = new PlayerFactory<IjkmPlayer>() {
                @Override
                public IjkmPlayer createPlayer(Context context) {
                    return new IjkmPlayer(context);
                }
            };
            try {
                tv.danmaku.ijk.media.player.IjkMediaPlayer.loadLibrariesOnce(new IjkLibLoader() {
                    @Override
                    public void loadLibrary(String s) throws UnsatisfiedLinkError, SecurityException {
                        try {
                            System.loadLibrary(s);
                        } catch (Throwable th) {
                            th.printStackTrace();
                        }
                    }
                });
            } catch (Throwable th) {
                th.printStackTrace();
            }

        } else {
            playerFactory = AndroidMediaPlayerFactory.create();
        }
        RenderViewFactory renderViewFactory = null;
        switch (renderType) {
            case 0:
            default:
                renderViewFactory = TextureRenderViewFactory.create();
                break;
            case 1:
                renderViewFactory = SurfaceRenderViewFactory.create();
                break;
        }
        videoView.setPlayerFactory(playerFactory);
        videoView.setRenderViewFactory(renderViewFactory);
        videoView.setScreenScaleType(scale);

    }

    public static void init() {
        try {
            tv.danmaku.ijk.media.player.IjkMediaPlayer.loadLibrariesOnce(new IjkLibLoader() {
                @Override
                public void loadLibrary(String s) throws UnsatisfiedLinkError, SecurityException {
                    try {
                        System.loadLibrary(s);
                    } catch (Throwable th) {
                        th.printStackTrace();
                    }
                }
            });
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public static String getDisplaySpeed(long speed) {
        if (speed > 1048576)
            return new DecimalFormat("#.00").format(speed / 1048576d) + "Mb/s";
        else if (speed > 1024)
            return (speed / 1024) + "Kb/s";
        else
            return speed > 0 ? speed + "B/s" : "0B/s";
    }
}
