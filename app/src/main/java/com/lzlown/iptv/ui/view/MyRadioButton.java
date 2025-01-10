package com.lzlown.iptv.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import com.lzlown.iptv.R;
import com.lzlown.iptv.base.BaseActivity;

public class MyRadioButton extends View {
    Context context;


    public MyRadioButton(Context context) {
        super(context);
        this.context = context;
        this.setBackgroundResource(R.drawable.radio_checked_shape);
    }

    public MyRadioButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.setBackgroundResource(R.drawable.radio_checked_shape);
    }

    public MyRadioButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        this.setBackgroundResource(R.drawable.radio_checked_shape);
    }

    public void setState(int state) {
        LayerDrawable background = (LayerDrawable) this.getBackground();
        if (state == 0) {
            ((GradientDrawable) background.getDrawable(0)).setStroke(3, Color.WHITE);
            ((GradientDrawable) background.getDrawable(1)).setColor(Color.TRANSPARENT);
        } else if (state == 1) {
            ((GradientDrawable) background.getDrawable(0)).setStroke(3, ((BaseActivity) context).getThemeColor());
            ((GradientDrawable) background.getDrawable(1)).setColor(((BaseActivity) context).getThemeColor());
        } else {
            ((GradientDrawable) background.getDrawable(0)).setStroke(3, Color.BLACK);
            ((GradientDrawable) background.getDrawable(1)).setColor(Color.TRANSPARENT);
        }

    }

}
