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

public class LiveEpgAdapter extends BaseQuickAdapter<LiveEpgItem, BaseViewHolder> {
    private int selectedEpgIndex = -1;
    private int focusedEpgIndex = -1;
    private LiveEpgItem liveEpgItem;
    private int liveEpgItemIndex;

    public LiveEpgAdapter() {
        super(R.layout.item_live_epg, new ArrayList<>());
    }

    @Override
    protected void convert(BaseViewHolder holder, LiveEpgItem value) {
        TextView name = holder.getView(R.id.tv_epg_name);
        TextView time = holder.getView(R.id.tv_epg_time);
        TextView back = holder.getView(R.id.goback);
        name.setText(value.getTitle());
        if (value.index == selectedEpgIndex && value.index != focusedEpgIndex && value.currentEpgDate.equals(TimeUtil.getTime())) {
            name.setTextColor(mContext.getResources().getColor(R.color.color_1890FF));
            time.setTextColor(mContext.getResources().getColor(R.color.color_1890FF));
        } else {
            name.setTextColor(Color.WHITE);
            time.setTextColor(Color.WHITE);
        }
        name.setText(value.title);
        time.setText(String.format("%s--%s", value.start, value.end));
        if (value.equals(liveEpgItem)&&value.index==liveEpgItemIndex){
            back.setVisibility(View.VISIBLE);
            back.setBackgroundColor(Color.YELLOW);
            back.setText("回看中");
            back.setTextColor(Color.RED);
            return;
        }
        Date ctime = TimeUtil.getTime(value.currentEpgDate);
        Date nowTime = TimeUtil.getTime(TimeUtil.getTime());
        Date breTime = TimeUtil.getTime(TimeUtil.getTime(-1));
        if (nowTime.compareTo(ctime) < 0) {
            back.setVisibility(View.VISIBLE);
            back.setBackgroundColor(Color.GRAY);
            back.setTextColor(Color.BLACK);
            back.setText("预告");
//            back.setVisibility(View.GONE);
        } else if (nowTime.compareTo(ctime) > 0&&ctime.compareTo(breTime) >= 0) {
            back.setVisibility(View.VISIBLE);
            back.setBackgroundColor(Color.BLUE);
            back.setTextColor(Color.WHITE);
            back.setText("回看");
        } else if (nowTime.compareTo(ctime) == 0){
            Date date = new Date();
            if (date.compareTo(value.startdateTime) >= 0 && date.compareTo(value.enddateTime) <= 0) {
                back.setVisibility(View.VISIBLE);
                back.setBackgroundColor(Color.YELLOW);
                back.setText("直播中");
                back.setTextColor(Color.RED);
            } else if (date.compareTo(value.enddateTime) > 0) {
                back.setVisibility(View.VISIBLE);
                back.setBackgroundColor(Color.BLUE);
                back.setTextColor(Color.WHITE);
                back.setText("回看");
            } else if (date.compareTo(value.startdateTime) < 0) {
                back.setVisibility(View.VISIBLE);
                back.setBackgroundColor(Color.GRAY);
                back.setTextColor(Color.BLACK);
                back.setText("预告");
//                back.setVisibility(View.GONE);
            }
        }
        else {
            back.setVisibility(View.GONE);
        }
    }

    public int getSelectedIndex() {
        return selectedEpgIndex;
    }

    public void setSelectedEpgIndex(int selectedEpgIndex) {
        if (selectedEpgIndex == this.selectedEpgIndex) return;
        this.selectedEpgIndex = selectedEpgIndex;
        if (this.selectedEpgIndex != -1)
            notifyItemChanged(this.selectedEpgIndex);
    }


    public int getFocusedEpgIndex() {
        return focusedEpgIndex;
    }

    public void setFocusedEpgIndex(int focusedEpgIndex) {
        this.focusedEpgIndex = focusedEpgIndex;
        if (this.focusedEpgIndex != -1)
            notifyItemChanged(this.focusedEpgIndex);
    }

    public void setLiveEpgItemIndex(LiveEpgItem liveEpgItem) {
        if (liveEpgItem==null){
            if (liveEpgItemIndex!=-1){
                this.liveEpgItem=null;
                notifyItemChanged(liveEpgItemIndex);
                liveEpgItemIndex=-1;
            }
        }else {
            if (liveEpgItemIndex==liveEpgItem.index)return;
            this.liveEpgItem=liveEpgItem;
            notifyItemChanged(liveEpgItemIndex);
            liveEpgItemIndex = liveEpgItem.index;
            notifyItemChanged(liveEpgItem.index);
        }
    }
}
