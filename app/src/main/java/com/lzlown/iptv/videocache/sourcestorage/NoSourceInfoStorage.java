package com.lzlown.iptv.videocache.sourcestorage;

import com.lzlown.iptv.videocache.SourceInfo;

public class NoSourceInfoStorage implements SourceInfoStorage {

    @Override
    public SourceInfo get(String url) {
        return null;
    }

    @Override
    public void put(String url, SourceInfo sourceInfo) {
    }

    @Override
    public void release() {
    }
}
