package com.lzlown.iptv.ui.adapter;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.lzlown.iptv.R;
import com.lzlown.iptv.bean.LiveSettingItem;

import java.util.ArrayList;

public class LiveSettingItemAdapter extends BaseQuickAdapter<LiveSettingItem, BaseViewHolder> {
    private int focusedItemIndex = -1;

    public LiveSettingItemAdapter() {
        super(R.layout.item_setting, new ArrayList<>());
    }

    @Override
    protected void convert(BaseViewHolder holder, LiveSettingItem item) {
        TextView tvItemName = holder.getView(R.id.tvSettingItemName);
        View tvItemSelect = holder.getView(R.id.tvSettingItemSelect);
        tvItemName.setText(item.getItemName().split("&")[0]);
        int itemIndex = item.getItemIndex();
        if (focusedItemIndex == itemIndex) {
            if (item.isItemSelected()) {
                tvItemName.setTextColor(mContext.getResources().getColor(R.color.color_selected));
                tvItemSelect.setBackgroundResource(R.drawable.radio_checked_shape);
            } else {
                tvItemName.setTextColor(Color.BLACK);
                tvItemSelect.setBackgroundResource(R.drawable.radio_disenable_shape);
            }
        } else {
            if (item.isItemSelected()) {
                tvItemName.setTextColor(mContext.getResources().getColor(R.color.color_selected));
                tvItemSelect.setBackgroundResource(R.drawable.radio_checked_shape);
            }else {
                tvItemName.setTextColor(Color.WHITE);
                tvItemSelect.setBackgroundResource(R.drawable.radio_unchecked_shape);
            }
        }

    }

    public void selectItem(int selectedItemIndex, boolean select, boolean unselectPreItemIndex) {
        if (unselectPreItemIndex) {
            int preSelectedItemIndex = getSelectedItemIndex();
            if (preSelectedItemIndex != -1) {
                getData().get(preSelectedItemIndex).setItemSelected(false);
                notifyItemChanged(preSelectedItemIndex);
            }
        }
        if (selectedItemIndex != -1) {
            getData().get(selectedItemIndex).setItemSelected(select);
            notifyItemChanged(selectedItemIndex);
        }
    }

    public void setFocusedItemIndex(int focusedItemIndex) {
        int preFocusItemIndex = this.focusedItemIndex;
        this.focusedItemIndex = focusedItemIndex;
        if (preFocusItemIndex != -1)
            notifyItemChanged(preFocusItemIndex);
        if (this.focusedItemIndex != -1)
            notifyItemChanged(this.focusedItemIndex);
    }

    public int getSelectedItemIndex() {
        for (LiveSettingItem item : getData()) {
            if (item.isItemSelected())
                return item.getItemIndex();
        }
        return -1;
    }
}