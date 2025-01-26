package com.lzlown.iptv.ui.adapter;

import android.graphics.Color;
import android.widget.TextView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.lzlown.iptv.R;
import com.lzlown.iptv.base.BaseActivity;
import com.lzlown.iptv.bean.LiveChannelItem;

import java.util.ArrayList;

public class LiveEpgChannelAdapter extends BaseQuickAdapter<LiveChannelItem, BaseViewHolder> {
    private int selectedChannelIndex = -1;
    private int focusedChannelIndex = -1;

    public LiveEpgChannelAdapter() {
        super(R.layout.item, new ArrayList<>());
    }

    @Override
    protected void convert(BaseViewHolder holder, LiveChannelItem item) {
        TextView tvItem = holder.getView(R.id.tvItem);
        tvItem.setText(String.format("%03d", item.getChannelNum()) + "  " + item.getChannelName());
        int channelIndex = item.getChannelNum() - 1;
        if (selectedChannelIndex == channelIndex) {
            tvItem.setTextColor(mContext.getResources().getColor(R.color.color_selected));
        } else {
            if (focusedChannelIndex == channelIndex) {
                tvItem.setTextColor(mContext.getResources().getColor(R.color.color_focused));
            } else {
                tvItem.setTextColor(Color.WHITE);
            }
        }
    }

    public void setSelectedChannelIndex(int selectedChannelIndex) {
        if (selectedChannelIndex == this.selectedChannelIndex) return;
        int preSelectedChannelIndex = this.selectedChannelIndex;
        this.selectedChannelIndex = selectedChannelIndex;
        if (preSelectedChannelIndex != -1)
            notifyItemChanged(preSelectedChannelIndex);
        if (this.selectedChannelIndex != -1)
            notifyItemChanged(this.selectedChannelIndex);
    }

    public void setFocusedChannelIndex(int focusedChannelIndex) {
        int preFocusedChannelIndex = this.focusedChannelIndex;
        this.focusedChannelIndex = focusedChannelIndex;
        if (preFocusedChannelIndex != -1)
            notifyItemChanged(preFocusedChannelIndex);
        if (this.focusedChannelIndex != -1)
            notifyItemChanged(this.focusedChannelIndex);
        else if (this.selectedChannelIndex != -1)
            notifyItemChanged(this.selectedChannelIndex);
    }

    public int getFocusedChannelIndex() {
        return focusedChannelIndex;
    }

    public int getSelectedChannelIndex() {
        return selectedChannelIndex;
    }
}