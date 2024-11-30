package com.lzlown.iptv.config;

import com.google.gson.JsonElement;

public interface Config {

    void init(JsonElement jsonElement, AppConfig.LoadCallback callback);
}
