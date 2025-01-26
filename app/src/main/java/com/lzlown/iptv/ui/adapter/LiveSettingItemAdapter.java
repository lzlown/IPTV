package com.lzlown.iptv.ui.adapter;

import android.graphics.Color;
import android.widget.TextView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.lzlown.iptv.R;
import com.lzlown.iptv.base.BaseActivity;
import com.lzlown.iptv.bean.LiveSettingItem;
import com.lzlown.iptv.ui.view.MyRadioButton;

import java.util.ArrayList;

public class LiveSettingItemAdapter extends BaseQuickAdapter<LiveSettingItem, BaseViewHolder> {
    private int focusedItemIndex = -1;

    public LiveSettingItemAdapter() {
        super(R.layout.item_setting, new ArrayList<>());
    }


    @Override
    protected void convert(BaseViewHolder holder, LiveSettingItem item) {
        TextView tvItemName = holder.getView(R.id.tvSettingItemName);
        MyRadioButton tvItemSelect = holder.getView(R.id.tvSettingItemSelect);
        tvItemName.setText(item.getItemName().split("&")[0]);
        if (item.isItemSelected()) {
            tvItemName.setTextColor(mContext.getResources().getColor(R.color.color_selected));
            tvItemSelect.setState(1);
        } else {
            if (focusedItemIndex==item.getItemIndex()) {
                tvItemName.setTextColor(mContext.getResources().getColor(R.color.color_focused));
                tvItemSelect.setState(-1);
            }else {
                tvItemName.setTextColor(Color.WHITE);
                tvItemSelect.setState(0);
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