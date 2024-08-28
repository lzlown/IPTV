package com.lzlown.iptv.ui.adapter;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.lzlown.iptv.R;
import com.lzlown.iptv.bean.LiveEpgItem;
import com.lzlown.iptv.util.TimeUtil;

import java.util.ArrayList;
import java.util.Date;

public class LiveEpgItemAdapter extends BaseQuickAdapter<LiveEpgItem, BaseViewHolder> {
    private int selectedIndex = -1;
    private int focusedIndex = -1;
    private LiveEpgItem liveEpgItem;
    private boolean isCanBack = true;

    public LiveEpgItemAdapter() {
        super(R.layout.item_live_epg, new ArrayList<>());
    }

    @Override
    protected void convert(BaseViewHolder holder, LiveEpgItem value) {
        TextView name = holder.getView(R.id.tv_epg_name);
        TextView time = holder.getView(R.id.tv_epg_time);
        TextView back = holder.getView(R.id.goback);
        if (value.index == selectedIndex && value.index != focusedIndex&& value.index != -1) {
            int color = mContext.getResources().getColor(R.color.color_selected);
            name.setTextColor(color);
            time.setTextColor(color);
        } else {
            name.setTextColor(Color.WHITE);
            time.setTextColor(Color.WHITE);
        }
        int color_epg_current = mContext.getResources().getColor(R.color.color_epg_current);
        int color_epg_none = mContext.getResources().getColor(R.color.color_epg_none);
        int color_epg_back_ing = mContext.getResources().getColor(R.color.color_epg_back_ing);
        int color_epg_back_can = mContext.getResources().getColor(R.color.color_epg_back_can);
        name.setText(value.title);
        time.setText(String.format("%s--%s", value.start, value.end));
        Date date = new Date();
        Date epgStartTime = TimeUtil.getEpgTime(value.currentEpgDate + value.start);
        if (date.compareTo(epgStartTime) < 0) {
            back.setVisibility(View.VISIBLE);
            back.setBackgroundColor(color_epg_none);
            back.setTextColor(Color.WHITE);
            back.setText("预告");
        } else if (TimeUtil.getTimeToDate(-7).compareTo(epgStartTime) > 0) {
            back.setVisibility(View.VISIBLE);
            back.setBackgroundColor(color_epg_none);
            back.setTextColor(Color.WHITE);
            back.setText("回看");
            if (value.index == -1) {
                back.setVisibility(View.GONE);
            }
        } else if (date.compareTo(epgStartTime) > 0 && date.compareTo(TimeUtil.getEpgTime(value.currentEpgDate + value.end)) < 0) {
            back.setVisibility(View.VISIBLE);
            back.setBackgroundColor(color_epg_current);
            back.setText("直播中");
            back.setTextColor(Color.WHITE);
        } else {
            if (value.equals(liveEpgItem)) {
                back.setVisibility(View.VISIBLE);
                back.setBackgroundColor(color_epg_back_ing);
                back.setText("回看中");
                back.setTextColor(Color.WHITE);
                return;
            }
            back.setVisibility(View.VISIBLE);
            back.setBackgroundColor(color_epg_back_can);
            back.setTextColor(Color.WHITE);
            back.setText("回看");
            if (!isCanBack){
                back.setBackgroundColor(Color.GRAY);
            }
        }
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        if (selectedIndex == this.selectedIndex) return;
        int preSelectedChannelIndex = this.selectedIndex;
        this.selectedIndex = selectedIndex;
        if (preSelectedChannelIndex != -1)
            notifyItemChanged(preSelectedChannelIndex);
        if (this.selectedIndex != -1)
            notifyItemChanged(this.selectedIndex);
    }


    public int getFocusedIndex() {
        return focusedIndex;
    }

    public void setFocusedIndex(int focusedIndex) {
        int preFocusedChannelIndex = this.focusedIndex;
        this.focusedIndex = focusedIndex;
        if (preFocusedChannelIndex != -1)
            notifyItemChanged(preFocusedChannelIndex);
        if (this.focusedIndex != -1)
            notifyItemChanged(this.focusedIndex);
        else if (this.focusedIndex != -1)
            notifyItemChanged(this.focusedIndex);
    }

    public void setLiveEpgItem(LiveEpgItem liveEpgItem) {
        this.liveEpgItem = liveEpgItem;
    }

    public void setCanBack(boolean canBack) {
        isCanBack = canBack;
    }
}
