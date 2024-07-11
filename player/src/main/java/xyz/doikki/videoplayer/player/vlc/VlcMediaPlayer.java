package xyz.doikki.videoplayer.player.vlc;

import android.content.Context;
import android.net.Uri;
import android.view.Surface;
import android.view.SurfaceHolder;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.interfaces.ILibVLC;
import org.videolan.libvlc.interfaces.IMedia;
import org.videolan.libvlc.interfaces.IVLCVout;
import tv.danmaku.ijk.media.player.pragma.DebugLog;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VlcMediaPlayer {
    private final static String TAG = VlcMediaPlayer.class.getName();
    private final ILibVLC mILibVLC;
    private final MediaPlayer mMediaPlayer;
    public List<String> mediaOps = new ArrayList<>();
    private OnListener onListener;
    private Integer sum = 0;
    private Long time;
    private String path;



    public VlcMediaPlayer(Context context) {
        mILibVLC = new LibVLC(context);
        mMediaPlayer = new MediaPlayer(mILibVLC);
        mMediaPlayer.setEventListener(eventListener);
    }

    public VlcMediaPlayer(Context context, List<String> args) {
        mILibVLC = new LibVLC(context,args);
        mMediaPlayer = new MediaPlayer(mILibVLC);
        mMediaPlayer.setEventListener(eventListener);
    }

    private final IVLCVout.OnNewVideoLayoutListener onNewVideoLayoutListener = new IVLCVout.OnNewVideoLayoutListener() {
        @Override
        public void onNewVideoLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
            if (onListener != null) {
                onListener.onVideoSizeChanged(mMediaPlayer, width, height);
                DebugLog.i(TAG, "onNewVideoLayout");
            }
        }
    };

    public void setOption(String value) {
        mediaOps.add(value);
    }

    private final MediaPlayer.EventListener eventListener = new MediaPlayer.EventListener() {
        @Override
        public void onEvent(MediaPlayer.Event event) {
            switch (event.type) {
                case MediaPlayer.Event.TimeChanged:
                    break;
                case MediaPlayer.Event.Buffering:
                    if (onListener != null) {
                        if (event.getBuffering() == 100) {
                            DebugLog.i(TAG, "onEvent: Buffered");
                            if (path.contains("rtsp") || path.contains("udp") || path.contains("rtp")) {
                                time = System.currentTimeMillis() + 15000;
                                rePlay();
                            }
                        }
                    }
                    break;
                case MediaPlayer.Event.Playing:
                    break;
                case MediaPlayer.Event.EncounteredError:
                    if (onListener != null) {
                        onListener.onError(mMediaPlayer, 0, 0);
                        DebugLog.i(TAG, "onEvent: EncounteredError");
                    }
                    break;
                case MediaPlayer.Event.EndReached:
                    if (mMediaPlayer.getPlayerState() == Media.State.Ended && onListener != null) {
                        onListener.onCompletion(mMediaPlayer);
                        DebugLog.i(TAG, "onEvent: EndReached");
                    }
                    break;
                case MediaPlayer.Event.Vout:
                    DebugLog.i(TAG, "onEvent: Vout");
                    mMediaPlayer.setVolume(100);
                    onListener.onPrepared(mMediaPlayer);
                    break;
            }
        }
    };

    public void setDataSource(Uri uri)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        setDataSource(uri, null);
    }

    public void setDataSource(Uri uri, Map<String, String> headers)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        path = uri.toString();
        IMedia mCurrentMedia = new Media(mILibVLC, uri);
        for (String vlcOp : mediaOps) {
            mCurrentMedia.addOption(":" + vlcOp);
        }
        mCurrentMedia.setHWDecoderEnabled(true, true);
        mMediaPlayer.setMedia(mCurrentMedia);
        mMediaPlayer.setVolume(0);
        mMediaPlayer.play();
    }

    public void setDataSource(String path)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {

    }

    public void setDataSource(FileDescriptor fd)
            throws IOException, IllegalArgumentException, IllegalStateException {
    }

    public void prepareAsync() throws IllegalStateException {
        mMediaPlayer.play();
    }

    public void setDisplay(SurfaceHolder sh) {
        try {
            IVLCVout vlcVout = mMediaPlayer.getVLCVout();
            vlcVout.setVideoSurface(sh.getSurface(), sh);
            vlcVout.attachViews(onNewVideoLayoutListener);
        } catch (Exception ignored) {

        }
    }

    public void setSurface(Surface surface) {
        IVLCVout vlcVout = mMediaPlayer.getVLCVout();
        vlcVout.setVideoSurface(surface, null);
        vlcVout.attachViews(onNewVideoLayoutListener);
    }

    public void play() throws IllegalStateException {
        mMediaPlayer.play();
    }

    public void stop() throws IllegalStateException {
        mMediaPlayer.stop();
    }

    public void pause() throws IllegalStateException {
        mMediaPlayer.pause();
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public void release() {
        mMediaPlayer.setEventListener(null);
        mMediaPlayer.setVolume(0);
        mMediaPlayer.getVLCVout().detachViews();
        new Thread(new Runnable() {
            @Override
            public void run() {
                mMediaPlayer.release();
                mILibVLC.release();
            }
        }).start();
    }

    public void reset() {
    }

    public interface OnListener {
        boolean onInfo(org.videolan.libvlc.MediaPlayer mp, int what, int extra);

        boolean onError(org.videolan.libvlc.MediaPlayer mp, int what, int extra);

        void onVideoSizeChanged(org.videolan.libvlc.MediaPlayer mp, int width, int height);

        void onBufferingUpdate(org.videolan.libvlc.MediaPlayer mp, int percent);

        void onCompletion(org.videolan.libvlc.MediaPlayer mp);

        void onPrepared(org.videolan.libvlc.MediaPlayer mp);
    }

    public void setOnListener(OnListener onListener) {
        this.onListener = onListener;
    }

    private synchronized void rePlay() {
        sum++;
        if (sum > 2 && System.currentTimeMillis() < time) {
            DebugLog.i(TAG, "onEvent: rePlay");
            sum = 0;
            mMediaPlayer.stop();
            mMediaPlayer.play();
            time = System.currentTimeMillis() + 15000;
        }
    }
}
