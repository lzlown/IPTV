package com.lzlown.iptv.videoplayer.player.vlc;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.lzlown.iptv.videoplayer.player.AbstractPlayer;
import com.lzlown.iptv.videoplayer.util.PlayerUtils;
import org.videolan.libvlc.MediaPlayer;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VlcPlayer extends AbstractPlayer implements VlcMediaPlayer.OnErrorListener,
        VlcMediaPlayer.OnCompletionListener,
        VlcMediaPlayer.OnInfoListener,
        VlcMediaPlayer.OnBufferingUpdateListener,
        VlcMediaPlayer.OnPreparedListener,
        VlcMediaPlayer.OnVideoSizeChangedListener {
    private VlcMediaPlayer mMediaPlayer;
    private final Context mAppContext;
    private int mBufferedPercent;
    public List<String> vlcOps = new ArrayList<>();

    public VlcPlayer(Context context) {
        mAppContext = context;
    }

    public List<String> getVlcOps() {
        return vlcOps;
    }

    @Override
    public void initPlayer() {
        setOptions();
        mMediaPlayer = new VlcMediaPlayer(mAppContext, vlcOps);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnVideoSizeChangedListener(this);
    }

    @Override
    public void setOptions() {

    }

    @Override
    public void setDataSource(String path, Map<String, String> headers) {
        try {
            mMediaPlayer.setDataSource(Uri.parse(path), headers);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setDataSource(AssetFileDescriptor fd) {

    }

    @Override
    public void pause() {
        try {
            mMediaPlayer.pause();
        } catch (IllegalStateException e) {
            mPlayerEventListener.onError();
        }
    }

    @Override
    public void start() {
        try {
            mMediaPlayer.play();
        } catch (IllegalStateException e) {
            mPlayerEventListener.onError();
        }
    }

    @Override
    public void stop() {
        try {
            mMediaPlayer.stop();
        } catch (IllegalStateException e) {
            mPlayerEventListener.onError();
        }
    }

    @Override
    public void prepareAsync() {
        try {
            mMediaPlayer.prepareAsync();
        } catch (IllegalStateException e) {
            mPlayerEventListener.onError();
        }

    }

    @Override
    public void reset() {

    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    @Override
    public void seekTo(long time) {

    }

    @Override
    public void release() {
        mMediaPlayer.setOnErrorListener(null);
        mMediaPlayer.setOnCompletionListener(null);
        mMediaPlayer.setOnInfoListener(null);
        mMediaPlayer.setOnBufferingUpdateListener(null);
        mMediaPlayer.setOnPreparedListener(null);
        mMediaPlayer.setOnVideoSizeChangedListener(null);
        mMediaPlayer.release();

    }

    @Override
    public long getCurrentPosition() {
        return 0;
    }

    @Override
    public long getDuration() {
        return 0;
    }

    @Override
    public int getBufferedPercentage() {
        return mBufferedPercent;
    }

    @Override
    public void setSurface(Surface surface) {
        mMediaPlayer.setSurface(surface);
    }

    @Override
    public void setDisplay(SurfaceHolder holder) {
        mMediaPlayer.setDisplay(holder);
    }

    @Override
    public void setVolume(float v1, float v2) {

    }

    @Override
    public void setLooping(boolean isLooping) {

    }

    @Override
    public void setSpeed(float speed) {

    }

    @Override
    public float getSpeed() {
        return 0;
    }

    @Override
    public long getTcpSpeed() {
        return PlayerUtils.getNetSpeed(mAppContext);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mPlayerEventListener.onError();
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mPlayerEventListener.onCompletion();
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        mBufferedPercent = percent;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mPlayerEventListener.onInfo(AbstractPlayer.MEDIA_INFO_RENDERING_START, 0);
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        if (width != 0 && height != 0) {
            mPlayerEventListener.onVideoSizeChanged(width, height);
        }
    }
}
