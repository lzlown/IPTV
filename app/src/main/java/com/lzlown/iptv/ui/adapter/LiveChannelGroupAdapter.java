package com.lzlown.iptv.ui.adapter;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.lzlown.iptv.R;
import com.lzlown.iptv.base.BaseActivity;
import com.lzlown.iptv.base.MyBaseViewHolder;
import com.lzlown.iptv.bean.LiveChannelGroup;

import java.util.ArrayList;

public class LiveChannelGroupAdapter extends BaseQuickAdapter<LiveChannelGroup, MyBaseViewHolder> {
    private int selectedGroupIndex = -1;
    private int focusedGroupIndex = -1;

    public LiveChannelGroupAdapter() {
        super(R.layout.item, new ArrayList<>());
    }

    @Override
    protected void convert(MyBaseViewHolder holder, LiveChannelGroup item) {
        TextView tvItem = holder.getView(R.id.tvItem);
        tvItem.setGravity(Gravity.CENTER);
        tvItem.setText(item.getGroupName());
        int groupIndex = item.getGroupIndex();
        if (selectedGroupIndex == groupIndex) {
            tvItem.setTextColor(BaseActivity.selectedTextColor);
        } else {
            if (focusedGroupIndex == groupIndex) {
                tvItem.setTextColor(BaseActivity.focusedTextColor);
            } else {
                tvItem.setTextColor(Color.WHITE);
            }
        }


    }

    public void setSelectedGroupIndex(int selectedGroupIndex) {
        if (selectedGroupIndex == this.selectedGroupIndex) return;
        int preSelectedGroupIndex = this.selectedGroupIndex;
        this.selectedGroupIndex = selectedGroupIndex;
        if (preSelectedGroupIndex != -1)
            notifyItemChanged(preSelectedGroupIndex);
        if (this.selectedGroupIndex != -1)
            notifyItemChanged(this.selectedGroupIndex);
    }

    public int getSelectedGroupIndex() {
        return selectedGroupIndex;
    }

    public void setFocusedGroupIndex(int focusedGroupIndex) {
        this.focusedGroupIndex = focusedGroupIndex;
        if (this.focusedGroupIndex != -1)
            notifyItemChanged(this.focusedGroupIndex);
        else if (this.selectedGroupIndex != -1)
            notifyItemChanged(this.selectedGroupIndex);
    }

}