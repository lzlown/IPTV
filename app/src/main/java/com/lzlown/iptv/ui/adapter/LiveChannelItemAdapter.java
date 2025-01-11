package com.lzlown.iptv.ui.adapter;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.lzlown.iptv.R;
import com.lzlown.iptv.base.BaseActivity;
import com.lzlown.iptv.base.MyBaseViewHolder;
import com.lzlown.iptv.config.EpgConfig;
import com.lzlown.iptv.base.App;
import com.lzlown.iptv.bean.LiveChannelItem;
import com.lzlown.iptv.ui.tv.widget.MarqueeTextView;

import java.util.ArrayList;

public class LiveChannelItemAdapter extends BaseQuickAdapter<LiveChannelItem, MyBaseViewHolder> {
    private int selectedChannelIndex = -1;
    private int focusedChannelIndex = -1;

    public LiveChannelItemAdapter() {
        super(R.layout.item_channel_item, new ArrayList<>());
    }

    @Override
    protected void convert(MyBaseViewHolder holder, LiveChannelItem item) {
        TextView tvChannel = holder.getView(R.id.tvChannelName);
        MarqueeTextView tvChannelEpg = holder.getView(R.id.tvChannelEpg);
        tvChannel.setText(String.format("%03d", item.getChannelNum()) + "  " + item.getChannelName());
        if (App.LIVE_SHOW_EPG) {
            tvChannelEpg.setText(EpgConfig.get().getLiveEpgItem(item).getTitle());
        } else {
            tvChannelEpg.setVisibility(View.GONE);
        }
        int channelIndex = item.getChannelIndex();
        if (selectedChannelIndex == channelIndex) {
            tvChannel.setTextColor(BaseActivity.selectedTextColor);
            tvChannelEpg.setTextColor(BaseActivity.selectedTextColor);
        } else {
            if (focusedChannelIndex == channelIndex) {
                tvChannel.setTextColor(BaseActivity.focusedTextColor);
                tvChannelEpg.setTextColor(BaseActivity.focusedTextColor);
            } else {
                tvChannel.setTextColor(Color.WHITE);
                tvChannelEpg.setTextColor(Color.WHITE);
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

    public int getSelectedChannelIndex() {
        return selectedChannelIndex;
    }


}