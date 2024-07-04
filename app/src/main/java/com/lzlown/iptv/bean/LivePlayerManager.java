package com.lzlown.iptv.bean;

import androidx.annotation.NonNull;
import com.lzlown.iptv.util.HawkConfig;
import com.lzlown.iptv.util.PlayerHelper;
import com.orhanobut.hawk.Hawk;
import org.json.JSONException;
import org.json.JSONObject;
import xyz.doikki.videoplayer.player.VideoView;

public class LivePlayerManager {
    JSONObject defaultPlayerConfig = new JSONObject();
    JSONObject currentPlayerConfig;

    public void init(VideoView videoView) {
        try {
            defaultPlayerConfig.put("pl", Hawk.get(HawkConfig.PLAY_TYPE, 0));
            defaultPlayerConfig.put("pr", Hawk.get(HawkConfig.PLAY_RENDER, 1));
            defaultPlayerConfig.put("sc", Hawk.get(HawkConfig.PLAY_SCALE, 0));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        getDefaultLiveChannelPlayer(videoView);
    }

    public void getDefaultLiveChannelPlayer(VideoView videoView) {
        PlayerHelper.updateCfg(videoView, defaultPlayerConfig);
        try {
            currentPlayerConfig = new JSONObject(defaultPlayerConfig.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getLiveChannelPlayer(VideoView videoView, String channelName) {
        JSONObject playerConfig = Hawk.get(channelName, null);
        if (playerConfig == null) {
            if (!currentPlayerConfig.toString().equals(defaultPlayerConfig.toString()))
                getDefaultLiveChannelPlayer(videoView);
            return;
        }
        if (playerConfig.toString().equals(currentPlayerConfig.toString()))
            return;

        try {
            if (playerConfig.getInt("pl") == currentPlayerConfig.getInt("pl")
                    && playerConfig.getInt("pr") == currentPlayerConfig.getInt("pr")) {
                videoView.setScreenScaleType(playerConfig.getInt("sc"));
            } else {
                PlayerHelper.updateCfg(videoView, playerConfig);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        currentPlayerConfig = playerConfig;
    }

    public void changeLivePlayerScale(@NonNull VideoView videoView, int playerScale, String channelName){
        videoView.setScreenScaleType(playerScale);
        JSONObject playerConfig = currentPlayerConfig;
        try {
            playerConfig.put("sc", playerScale);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (playerConfig.toString().equals(defaultPlayerConfig.toString()))
            Hawk.delete(channelName);
        else
            Hawk.put(channelName, playerConfig);
        currentPlayerConfig = playerConfig;

    }

    public int getLivePlayerScale() {
        try {
            return currentPlayerConfig.getInt("sc");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void changeLivePlayerType(VideoView videoView, int position, String channelName) {
        JSONObject playerConfig = currentPlayerConfig;
        try {
            playerConfig.put("pl", position);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        PlayerHelper.updateCfg(videoView, playerConfig);
        if (playerConfig.toString().equals(defaultPlayerConfig.toString()))
            Hawk.delete(channelName);
        else
            Hawk.put(channelName, playerConfig);
        currentPlayerConfig = playerConfig;
    }

    public int getLivePlayerType() {
        try {
            return currentPlayerConfig.getInt("pl");
        } catch (JSONException e) {
            e.printStackTrace();
            return 0;
        }
    }
}