package com.lzlown.iptv.videocache;

import java.io.File;

public interface CacheListener {

    void onCacheAvailable(File cacheFile, String url, int percentsAvailable);
}
