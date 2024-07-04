/*****************************************************************************
 * MediaPlayer.java
 *****************************************************************************
 * Copyright © 2015 VLC authors and VideoLAN
 *
 * Authors  Jean-Baptiste Kempf <jb@videolan.org>
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *****************************************************************************/

package xyz.doikki.videoplayer.player.vlc;

import android.content.Context;
import android.net.Uri;
import android.view.Surface;
import android.view.SurfaceHolder;
import androidx.annotation.NonNull;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.interfaces.ILibVLC;
import org.videolan.libvlc.interfaces.IMedia;
import org.videolan.libvlc.interfaces.IVLCVout;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class VlcMediaPlayer
{

    private IMedia mCurrentMedia = null;
    private final ILibVLC mILibVLC;
    private org.videolan.libvlc.MediaPlayer mMediaPlayer;
    private OnInfoListener onInfoListener;
    private OnErrorListener onErrorListener;
    private OnVideoSizeChangedListener onVideoSizeChangedListener;
    private OnBufferingUpdateListener onBufferingUpdateListener;
    private OnCompletionListener onCompletionListener;
    private OnPreparedListener onPreparedListener;

    public VlcMediaPlayer() {
        mILibVLC = new LibVLC(null);
        mMediaPlayer = new org.videolan.libvlc.MediaPlayer(mILibVLC);
        mMediaPlayer.setEventListener(eventListener);
    }
    public VlcMediaPlayer(Context context) {
        mILibVLC = new LibVLC(context);
        mMediaPlayer = new org.videolan.libvlc.MediaPlayer(mILibVLC);
        mMediaPlayer.setEventListener(eventListener);
    }
    public VlcMediaPlayer(Context context,List<String> args) {
        mILibVLC = new LibVLC(context,args);
        mMediaPlayer = new org.videolan.libvlc.MediaPlayer(mILibVLC);
        mMediaPlayer.setEventListener(eventListener);
    }

    private IVLCVout.OnNewVideoLayoutListener onNewVideoLayoutListener = new IVLCVout.OnNewVideoLayoutListener() {
        @Override
        public void onNewVideoLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
            try {
                onVideoSizeChangedListener.onVideoSizeChanged(mMediaPlayer,width, height);
            }catch (Exception e){

            }

        }
    };

    private org.videolan.libvlc.MediaPlayer.EventListener eventListener = new org.videolan.libvlc.MediaPlayer.EventListener() {
        @Override
        public void onEvent(org.videolan.libvlc.MediaPlayer.Event event) {
            if (event.type == org.videolan.libvlc.MediaPlayer.Event.Opening) {

            } else if (event.type == org.videolan.libvlc.MediaPlayer.Event.TimeChanged) {

            } else if (event.type == org.videolan.libvlc.MediaPlayer.Event.Buffering && event.getBuffering() > 99) {
                try {
                    onPreparedListener.onPrepared(mMediaPlayer);
                }catch (Exception e){

                }

            } else if (event.type == org.videolan.libvlc.MediaPlayer.Event.Playing) {

            } else if (event.type == org.videolan.libvlc.MediaPlayer.Event.Paused) {

            } else if (event.type == org.videolan.libvlc.MediaPlayer.Event.EncounteredError) {
                try {
                    onErrorListener.onError(mMediaPlayer,0,0);
                }catch (Exception e){

                }
            } else if (event.type == org.videolan.libvlc.MediaPlayer.Event.EndReached && mMediaPlayer.getPlayerState() == Media.State.Ended) {
                try {
                    onCompletionListener.onCompletion(mMediaPlayer);
                }catch (Exception e){

                }
            }
        }
    };
    public void setDataSource(Uri uri)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        setDataSource(uri, null);
    }

    public void setDataSource(Uri uri, Map<String, String> headers)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        mCurrentMedia = new Media(mILibVLC, uri);
        mCurrentMedia.setHWDecoderEnabled(true, false);
        mMediaPlayer.setMedia(mCurrentMedia);
    }

    public void setDataSource(String path)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        mCurrentMedia = new Media(mILibVLC, path);
        mCurrentMedia.setHWDecoderEnabled(true, false);
        mMediaPlayer.setMedia(mCurrentMedia);
    }

    public void setDataSource(FileDescriptor fd)
            throws IOException, IllegalArgumentException, IllegalStateException {
        mCurrentMedia = new Media(mILibVLC, fd);
        mCurrentMedia.setHWDecoderEnabled(true, false);
        mMediaPlayer.setMedia(mCurrentMedia);
    }

    public void prepare() throws IOException, IllegalStateException {
    }

    public void prepareAsync() throws IllegalStateException {
//        mCurrentMedia.addOption(":video-paused");
        mMediaPlayer.play();
    }

    public void setDisplay(SurfaceHolder sh) {
        try {
            IVLCVout vlcVout = mMediaPlayer.getVLCVout();
            vlcVout.setVideoSurface(sh.getSurface(), sh);
            vlcVout.attachViews(onNewVideoLayoutListener);
        }catch (Exception e){

        }

    }

    public void setSurface(Surface surface) {
        mMediaPlayer.getVLCVout().setVideoSurface(surface, null);
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
        mMediaPlayer.getVLCVout().detachViews();
        mMediaPlayer.release();
    }

    public void reset() {
    }

    public void setVolume(float leftVolume, float rightVolume) {
        mMediaPlayer.setVolume( (int)((leftVolume + rightVolume) * 100/2));
    }

    public interface OnPreparedListener
    {
        void onPrepared(org.videolan.libvlc.MediaPlayer mp);
    }

    public void setOnPreparedListener(OnPreparedListener listener) {
        this.onPreparedListener=listener;
    }

    public interface OnCompletionListener
    {
        void onCompletion(org.videolan.libvlc.MediaPlayer mp);
    }

    public void setOnCompletionListener(OnCompletionListener listener) {
        this.onCompletionListener=listener;
    }

    public interface OnBufferingUpdateListener
    {
        void onBufferingUpdate(org.videolan.libvlc.MediaPlayer mp, int percent);
    }

    public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
        this.onBufferingUpdateListener=listener;
    }


    public interface OnVideoSizeChangedListener
    {
         void onVideoSizeChanged(org.videolan.libvlc.MediaPlayer mp, int width, int height);
    }

    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener) {
        this.onVideoSizeChangedListener=listener;
    }


    public interface OnErrorListener
    {
        boolean onError(org.videolan.libvlc.MediaPlayer mp, int what, int extra);
    }

    public void setOnErrorListener(OnErrorListener listener) {
        this.onErrorListener=listener;
    }

    public interface OnInfoListener
    {
        boolean onInfo(org.videolan.libvlc.MediaPlayer mp, int what, int extra);
    }

    public void setOnInfoListener(OnInfoListener listener) {
        this.onInfoListener=listener;
    }
}
