package com.lzlown.iptv.ui.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.widget.TextView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.lzlown.iptv.R;
import com.lzlown.iptv.base.BaseActivity;
import com.lzlown.iptv.base.MyBaseViewHolder;
import com.lzlown.iptv.bean.LiveSettingGroup;

import java.util.ArrayList;

public class LiveSettingGroupAdapter extends BaseQuickAdapter<LiveSettingGroup, MyBaseViewHolder> {
    private int selectedGroupIndex = -1;
    private int focusedGroupIndex = -1;

    public LiveSettingGroupAdapter() {
        super(R.layout.item_setting_group, new ArrayList<>());
    }

    @Override
    protected void convert(MyBaseViewHolder holder, LiveSettingGroup group) {
        TextView tvGroupName = holder.getView(R.id.tvSettingGroupName);
        tvGroupName.setText(group.getGroupName());
        int groupIndex = group.getGroupIndex();
        if (groupIndex == focusedGroupIndex) {
            tvGroupName.setTextColor(BaseActivity.selectedTextColor);
//            StateListDrawable stateListDrawable = new StateListDrawable();
//            GradientDrawable gradientDrawable = new GradientDrawable();
//            gradientDrawable.setColor(BaseActivity.focusedColor);
//            gradientDrawable.setCornerRadius(15);
//            stateListDrawable.addState(new int[]{android.R.attr.state_focused}, gradientDrawable);
//            holder.itemView.setBackground(stateListDrawable);
        } else {
            tvGroupName.setTextColor(Color.WHITE);
//            GradientDrawable gradientDrawable = new GradientDrawable();
//            gradientDrawable.setCornerRadius(15);
//            gradientDrawable.setColor(mContext.getResources().getColor(R.color.color_0E0E0E_90));
//            holder.itemView.setBackground(gradientDrawable);
        }
    }

    public void setSelectedGroupIndex(int selectedGroupIndex) {
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
        int preSelectedGroupIndex = this.focusedGroupIndex;
        this.focusedGroupIndex = focusedGroupIndex;
        if (preSelectedGroupIndex != -1)
            notifyItemChanged(preSelectedGroupIndex);
        if (this.focusedGroupIndex != -1)
            notifyItemChanged(this.focusedGroupIndex);
        else if (this.selectedGroupIndex != -1)
            notifyItemChanged(this.selectedGroupIndex);
    }
}