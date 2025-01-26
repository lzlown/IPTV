package com.lzlown.iptv.ui.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.widget.SwitchCompat;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.lzlown.iptv.R;
import com.lzlown.iptv.base.BaseActivity;
import com.lzlown.iptv.bean.LiveSettingGroup;
import com.lzlown.iptv.config.SettingConfig;
import com.lzlown.iptv.util.HawkConfig;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;

public class LiveRightSettingGroupAdapter extends BaseQuickAdapter<LiveSettingGroup, BaseViewHolder> {
    private int selectedGroupIndex = -1;
    private int focusedGroupIndex = -1;

    public LiveRightSettingGroupAdapter() {
        super(R.layout.item_right_setting_group, new ArrayList<>());
    }

    @Override
    protected void convert(BaseViewHolder holder, LiveSettingGroup group) {
        TextView tvGroupName = holder.getView(R.id.tvSettingGroupName);
        SwitchCompat swh_status = holder.getView(R.id.swh_status);
        ImageView tvItemRightSelect = holder.getView(R.id.tvSettingGroupLine);
        TextView val = holder.getView(R.id.tvSettingGroupVal);
        tvGroupName.setText(group.getGroupName());
        int color = mContext.getResources().getColor(R.color.color_selected);
        try {
            ((GradientDrawable)swh_status.getThumbDrawable()).setColor(color);
        }catch (Exception ignored){}

        if (group.getType()== SettingConfig.BUTTON){
            val.setVisibility(View.GONE);
            tvItemRightSelect.setVisibility(View.GONE);
            swh_status.setVisibility(View.GONE);
        }else if (group.getType()==SettingConfig.SWITCH){
            val.setVisibility(View.GONE);
            tvItemRightSelect.setVisibility(View.GONE);
            swh_status.setVisibility(View.VISIBLE);
            swh_status.setChecked(group.getSelect());
        }else  if (group.getType()==SettingConfig.SELECT){
            val.setVisibility(View.VISIBLE);
            tvItemRightSelect.setVisibility(View.VISIBLE);
            val.setText(group.getVal());
            swh_status.setVisibility(View.GONE);
        }

        if (group.getGroupIndex() == focusedGroupIndex){
            int color_focused = mContext.getResources().getColor(R.color.color_focused);
            tvGroupName.setTextColor(color_focused);
            val.setTextColor(color_focused);
            if (Hawk.get(HawkConfig.THEME_SELECT, 0) == 0){
                tvItemRightSelect.setImageResource(R.drawable.baseline_chevron_right_24_black);
            }
        }else {
            tvGroupName.setTextColor(Color.WHITE);
            val.setTextColor(Color.WHITE);
            if (Hawk.get(HawkConfig.THEME_SELECT, 0) == 0){
                tvItemRightSelect.setImageResource(R.drawable.baseline_chevron_right_24_white);
            }

        }
    }

    public int getSelectedGroupIndex() {
        return selectedGroupIndex;
    }

    public void setSelectedGroupIndex(int selectedGroupIndex) {
        int preSelectedGroupIndex = this.selectedGroupIndex;
        this.selectedGroupIndex = selectedGroupIndex;
        if (preSelectedGroupIndex != -1)
            notifyItemChanged(preSelectedGroupIndex);
        if (this.selectedGroupIndex != -1)
            notifyItemChanged(this.selectedGroupIndex);
    }

    public int getFocusedGroupIndex() {
        return focusedGroupIndex;
    }

    public void setFocusedGroupIndex(int focusedGroupIndex) {
        int preFocusItemIndex = this.focusedGroupIndex;
        this.focusedGroupIndex = focusedGroupIndex;
        if (preFocusItemIndex != -1)
            notifyItemChanged(preFocusItemIndex);
        if (this.focusedGroupIndex != -1)
            notifyItemChanged(this.focusedGroupIndex);
    }
}