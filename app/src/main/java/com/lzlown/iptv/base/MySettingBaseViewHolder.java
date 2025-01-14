package com.lzlown.iptv.base;

import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import com.chad.library.adapter.base.BaseViewHolder;
import me.jessyan.autosize.AutoSizeConfig;


public class MySettingBaseViewHolder extends BaseViewHolder {
    public MySettingBaseViewHolder(View view) {
        super(view);
        float targetDensity = (float) AutoSizeConfig.getInstance().getScreenWidth() / 1280;
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(BaseActivity.focusedColor);
        gradientDrawable.setCornerRadius(Math.round(targetDensity * 10));
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_focused}, gradientDrawable);
        GradientDrawable gradientDrawableDefault = new GradientDrawable();
        gradientDrawableDefault.setColor(BaseActivity.buttonColor);
        gradientDrawableDefault.setCornerRadius(Math.round(targetDensity * 10));
        stateListDrawable.addState(new int []{} , gradientDrawableDefault);
        view.setBackground(stateListDrawable);
    }
}
