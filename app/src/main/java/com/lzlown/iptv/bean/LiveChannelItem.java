package com.lzlown.iptv.bean;

import java.util.ArrayList;
import java.util.List;


public class LiveChannelItem implements Cloneable {
    private int channelGroupIndex;
    private int channelIndex;
    private int channelNum;
    private String channelName;
    private List<LiveChannelItemSource> liveChannelItemSources;
    public int sourceIndex = 0;
    public int sourceNum = 0;

    public int getChannelGroupIndex() {
        return channelGroupIndex;
    }

    public void setChannelGroupIndex(int channelGroupIndex) {
        this.channelGroupIndex = channelGroupIndex;
    }

    public void setChannelIndex(int channelIndex) {
        this.channelIndex = channelIndex;
    }

    public int getChannelIndex() {
        return channelIndex;
    }

    public void setChannelNum(int channelNum) {
        this.channelNum = channelNum;
    }

    public int getChannelNum() {
        return channelNum;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setLiveChannelItemSources(List<LiveChannelItemSource> liveChannelItemSources) {
        this.liveChannelItemSources = liveChannelItemSources;
        sourceNum = liveChannelItemSources.size();
    }


    public String getChannelCh() {
        return liveChannelItemSources.get(sourceIndex).cc;
    }

    public void preSource() {
        sourceIndex--;
        if (sourceIndex < 0) sourceIndex = sourceNum - 1;
    }

    public void nextSource() {
        sourceIndex++;
        if (sourceIndex == sourceNum) sourceIndex = 0;
    }

    public void setSourceIndex(int sourceIndex) {
        this.sourceIndex = sourceIndex;
    }

    public int getSourceIndex() {
        return sourceIndex;
    }

    public String getUrl() {
        return liveChannelItemSources.get(sourceIndex).url;
    }

    public int getSourceNum() {
        return sourceNum;
    }

    public ArrayList<String> getChannelSourceNames() {
        ArrayList<String> names = new ArrayList<>();
        for (LiveChannelItemSource liveChannelItemSource : liveChannelItemSources) {
            names.add(liveChannelItemSource.getName());
        }
        return names;
    }

    public String getSourceName() {
        return liveChannelItemSources.get(sourceIndex).getName();
    }


    public String getSocUrls() {
        if (sourceIndex < liveChannelItemSources.size()) {
            return liveChannelItemSources.get(sourceIndex).getBackUrl();
        } else {
            return null;
        }
    }

    @Override
    public LiveChannelItem clone() {
        try {
            LiveChannelItem clone = (LiveChannelItem) super.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }


}