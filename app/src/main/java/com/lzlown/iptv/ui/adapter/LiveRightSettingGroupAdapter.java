package com.lzlown.iptv.ui.adapter;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.widget.SwitchCompat;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.lzlown.iptv.R;
import com.lzlown.iptv.bean.LiveSettingGroup;

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
//        View tvItemSelect = holder.getView(R.id.tvSettingItemSelect);
        ImageView tvItemRightSelect = holder.getView(R.id.tvSettingGroupLine);
//        swh_status.setClickable(false);
        TextView val = holder.getView(R.id.tvSettingGroupVal);
        tvGroupName.setText(group.getGroupName());
        if (group.getType()==0){
            val.setVisibility(View.GONE);
            tvItemRightSelect.setVisibility(View.GONE);
            swh_status.setVisibility(View.GONE);
//            tvItemSelect.setVisibility(View.GONE);
        }else if (group.getType()==1){
            val.setVisibility(View.GONE);
            tvItemRightSelect.setVisibility(View.GONE);
//            tvItemSelect.setVisibility(View.VISIBLE);
            swh_status.setVisibility(View.VISIBLE);
            swh_status.setChecked(group.getSelect());
        }else  if (group.getType()==2){
            val.setVisibility(View.VISIBLE);
            tvItemRightSelect.setVisibility(View.VISIBLE);
            val.setText(group.getVal());
//            tvItemSelect.setVisibility(View.GONE);
            swh_status.setVisibility(View.GONE);
        }
        if (group.getGroupIndex() == focusedGroupIndex){
            tvGroupName.setTextColor(Color.BLACK);
            val.setTextColor(Color.BLACK);
            tvItemRightSelect.setImageResource(R.drawable.baseline_chevron_right_24_black);
//            if (group.getSelect()){
//                tvItemSelect.setBackgroundResource(R.drawable.radio_checked_shape);
//            }else {
//                tvItemSelect.setBackgroundResource(R.drawable.radio_disenable_shape);
//            }
        }else {
            tvGroupName.setTextColor(Color.WHITE);
            val.setTextColor(Color.WHITE);
            tvItemRightSelect.setImageResource(R.drawable.baseline_chevron_right_24_white);
//            if (group.getSelect()){
//                tvItemSelect.setBackgroundResource(R.drawable.radio_checked_shape);
//            }else {
//                tvItemSelect.setBackgroundResource(R.drawable.radio_unchecked_shape);
//            }
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