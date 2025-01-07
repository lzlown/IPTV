package com.lzlown.iptv.util;

import android.os.Handler;
import android.view.View;

public class FastClickCheckUtil {
    /**
     * 相同视图点击必须间隔0.5s才能有效
     *
     * @param view 目标视图
     */
    public static void check(View view) {
        check(view, 500);
    }

    public static void check200(View view) {
        check(view, 200);
    }
    /**
     * 设置间隔点击规则，配置间隔点击时长
     *
     * @param view  目标视图
     * @param mills 点击间隔时间（毫秒）
     */
    public static void check(final View view, int mills) {
        view.setClickable(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setClickable(true);
            }
        }, mills);
    }
}