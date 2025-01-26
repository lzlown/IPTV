package com.lzlown.iptv.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import com.lzlown.iptv.R;
import com.lzlown.iptv.base.BaseActivity;

public class MyRadioButton extends View {
    float targetDensity = 1;
    Context context;

    public MyRadioButton(Context context) {
        this(context, null);
    }

    public MyRadioButton(Context context, @Nullable AttributeSet attrs) {

        this(context, attrs, 0);
    }

    public MyRadioButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    public void setState(int state) {
        LayerDrawable background = (LayerDrawable) this.getBackground();
        int round = Math.round(targetDensity * 3);
        if (state == 0) {
            ((GradientDrawable) background.getDrawable(0)).setStroke(round, Color.WHITE);
            ((GradientDrawable) background.getDrawable(1)).setColor(Color.TRANSPARENT);
        } else if (state == 1) {
            int color_selected = context.getResources().getColor(R.color.color_selected);
            ((GradientDrawable) background.getDrawable(0)).setStroke(round, color_selected);
            ((GradientDrawable) background.getDrawable(1)).setColor(color_selected);
        } else {
            int color_focused = context.getResources().getColor(R.color.color_focused);
            ((GradientDrawable) background.getDrawable(0)).setStroke(round, color_focused);
            ((GradientDrawable) background.getDrawable(1)).setColor(Color.TRANSPARENT);
        }

    }

    private void init() {
        int color_selected = context.getResources().getColor(R.color.color_selected);
        targetDensity = ((BaseActivity) context).getTargetDensity();
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.OVAL);
        gradientDrawable.setSize(Math.round(targetDensity * 26), Math.round(targetDensity * 26));
        gradientDrawable.setStroke(Math.round(targetDensity * 3), color_selected);
        GradientDrawable cen = new GradientDrawable();
        cen.setSize(Math.round(targetDensity * 26), Math.round(targetDensity * 26));
        cen.setShape(GradientDrawable.OVAL);
        cen.setColor(color_selected);
        cen.setStroke(Math.round(targetDensity * 12), Color.TRANSPARENT);
        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{gradientDrawable, cen});
        this.setBackground(layerDrawable);
    }

}
