package com.lzlown.iptv.videocache;

import com.lzlown.iptv.videocache.headers.HeaderInjector;
import com.lzlown.iptv.videocache.sourcestorage.SourceInfoStorage;


class Config {

    public final SourceInfoStorage sourceInfoStorage;
    public final HeaderInjector headerInjector;

    Config(SourceInfoStorage sourceInfoStorage, HeaderInjector headerInjector) {
        this.sourceInfoStorage = sourceInfoStorage;
        this.headerInjector = headerInjector;
    }
}
