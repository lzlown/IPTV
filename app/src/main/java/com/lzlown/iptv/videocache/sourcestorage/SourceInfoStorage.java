package com.lzlown.iptv.videocache.sourcestorage;

import com.lzlown.iptv.videocache.SourceInfo;

public interface SourceInfoStorage {

    SourceInfo get(String url);

    void put(String url, SourceInfo sourceInfo);

    void release();
}
