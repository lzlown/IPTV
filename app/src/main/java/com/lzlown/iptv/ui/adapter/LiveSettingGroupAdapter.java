package com.lzlown.iptv.ui.adapter;

import android.graphics.Color;
import android.widget.TextView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.lzlown.iptv.R;
import com.lzlown.iptv.base.BaseActivity;
import com.lzlown.iptv.bean.LiveSettingGroup;

import java.util.ArrayList;

public class LiveSettingGroupAdapter extends BaseQuickAdapter<LiveSettingGroup, BaseViewHolder> {
    private int selectedGroupIndex = -1;
    private int focusedGroupIndex = -1;

    public LiveSettingGroupAdapter() {
        super(R.layout.item_setting_group, new ArrayList<>());
    }

    @Override
    protected void convert(BaseViewHolder holder, LiveSettingGroup group) {
        TextView tvGroupName = holder.getView(R.id.tvSettingGroupName);
        tvGroupName.setText(group.getGroupName());
        int groupIndex = group.getGroupIndex();
        if (groupIndex == focusedGroupIndex) {
            tvGroupName.setTextColor(mContext.getResources().getColor(R.color.color_selected));
        } else {
            tvGroupName.setTextColor(Color.WHITE);
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