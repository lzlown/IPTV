package xyz.doikki.videoplayer.player.vlc;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.view.Surface;
import android.view.SurfaceHolder;
import org.videolan.libvlc.MediaPlayer;
import tv.danmaku.ijk.media.player.pragma.DebugLog;
import xyz.doikki.videoplayer.player.AbstractPlayer;
import xyz.doikki.videoplayer.util.PlayerUtils;

import java.util.Map;

public class VlcPlayer extends AbstractPlayer implements VlcMediaPlayer.OnListener {
    private static final String TAG = VlcPlayer.class.getName();
    protected VlcMediaPlayer mMediaPlayer;
    private final Context mAppContext;
    private int mBufferedPercent;

    public VlcPlayer(Context context) {
        mAppContext = context;
    }

    @Override
    public void initPlayer() {
        mMediaPlayer = new VlcMediaPlayer(mAppContext);
        mMediaPlayer.setOnListener(this);
        setOptions();
    }

    @Override
    public void setOptions() {

    }

    @Override
    public void setDataSource(String path, Map<String, String> headers) {
        try {
            mMediaPlayer.setDataSource(Uri.parse(path), headers);
        } catch (Exception e) {
            DebugLog.e(TAG, "setDataSource ERR");
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
        mMediaPlayer.setOnListener(null);
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
        return 1;
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
