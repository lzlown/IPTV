package com.lzlown.iptv.base;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import com.chad.library.adapter.base.BaseViewHolder;
import me.jessyan.autosize.AutoSizeConfig;


public class MyBaseViewHolder extends BaseViewHolder {
    public MyBaseViewHolder(View view) {
        super(view);
        Drawable background = view.getBackground();
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(BaseActivity.focusedColor);
        float targetDensity = (float) AutoSizeConfig.getInstance().getScreenWidth() / 1280;
        gradientDrawable.setCornerRadius(Math.round(targetDensity * 10));
        if (background != null) {
            ((StateListDrawable) background).addState(new int[]{android.R.attr.state_focused}, gradientDrawable);
        } else {
            StateListDrawable stateListDrawable = new StateListDrawable();
            stateListDrawable.addState(new int[]{android.R.attr.state_focused}, gradientDrawable);
            view.setBackground(stateListDrawable);
        }
    }
}
