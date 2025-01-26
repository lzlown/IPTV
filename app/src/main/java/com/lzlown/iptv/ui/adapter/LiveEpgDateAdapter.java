package com.lzlown.iptv.ui.adapter;

import android.graphics.Color;
import android.view.Gravity;
import android.widget.TextView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.lzlown.iptv.R;
import com.lzlown.iptv.base.BaseActivity;
import com.lzlown.iptv.bean.LiveEpgDate;

import java.util.ArrayList;


public class LiveEpgDateAdapter extends BaseQuickAdapter<LiveEpgDate, BaseViewHolder> {

    private int selectedIndex = -1;
    private int focusedIndex = -1;

    public LiveEpgDateAdapter() {
        super(R.layout.item, new ArrayList<>());
    }

    @Override
    protected void convert(BaseViewHolder holder, LiveEpgDate item) {
        TextView tvItem = holder.getView(R.id.tvItem);
        tvItem.setGravity(Gravity.CENTER);
        tvItem.setText(item.getDatePresented());
        int index = item.getIndex();
        if (selectedIndex == index) {
            tvItem.setTextColor(mContext.getResources().getColor(R.color.color_selected));
        } else {
            if (focusedIndex == index) {
                tvItem.setTextColor(mContext.getResources().getColor(R.color.color_focused));
            } else {
                tvItem.setTextColor(Color.WHITE);
            }
        }
    }

    public void setSelectedIndex(int selectedIndex) {
        if (selectedIndex == this.selectedIndex) return;
        int preSelectedIndex = this.selectedIndex;
        this.selectedIndex = selectedIndex;
        if (preSelectedIndex != -1)
            notifyItemChanged(preSelectedIndex);
        if (this.selectedIndex != -1)
            notifyItemChanged(this.selectedIndex);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setFocusedIndex(int focusedIndex) {
        int preSelectedIndex = this.selectedIndex;
        this.focusedIndex = focusedIndex;
        if (preSelectedIndex != -1)
            notifyItemChanged(preSelectedIndex);
        if (this.focusedIndex != -1)
            notifyItemChanged(this.focusedIndex);
    }
}