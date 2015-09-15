/**
 * PayPalUnSyncedListAdapter.java
 *
 * Created by xuanzhui on 2015/9/1.
 * Copyright (c) 2015 BeeCloud. All rights reserved.
 */
package cn.beecloud.demo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class PayPalUnSyncedListAdapter extends BaseAdapter {
    private List<String> data;
    private LayoutInflater mInflater;

    public PayPalUnSyncedListAdapter(Context context, List<String> data) {
        this.data = data;
        mInflater = LayoutInflater.from(context);
    }

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        return data.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.unsynced_paypal_list_item, null);

            viewHolder = new ViewHolder();

            viewHolder.txtBillNum = (TextView) convertView
                    .findViewById(R.id.txtBillNum);
            viewHolder.txtStoreDate = (TextView) convertView
                    .findViewById(R.id.txtStoreDate);
            viewHolder.txtTotalFee = (TextView) convertView
                    .findViewById(R.id.txtTotalFee);
            viewHolder.txtChannel = (TextView) convertView
                    .findViewById(R.id.txtChannel);
            viewHolder.txtTitle = (TextView) convertView
                    .findViewById(R.id.txtTitle);
            viewHolder.txtCurrency = (TextView) convertView
                    .findViewById(R.id.txtCurrency);
            viewHolder.txtOptional = (TextView) convertView
                    .findViewById(R.id.txtOptional);


            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        UnSyncedPayPalItem item = new Gson().fromJson(data.get(position),
                new TypeToken<UnSyncedPayPalItem>() {
                }.getType());

        viewHolder.txtBillNum.setText("Bill Number: " + item.getBillNum());
        viewHolder.txtStoreDate.setText("Bill Stored Date: " + item.getStoreDate());
        viewHolder.txtTotalFee.setText("Bill Total Fee: " + (item.getBillTotalFee()==null? "" :
                Integer.valueOf(item.getBillTotalFee())/100.0));
        viewHolder.txtChannel.setText("Channel: " + item.getChannel());
        viewHolder.txtTitle.setText("Bill Title: " + item.getBillTitle());
        viewHolder.txtCurrency.setText("Bill Currency: " + item.getCurrency());
        viewHolder.txtOptional.setText("Optional Values: " + item.getOptional());

        return convertView;
    }

    private class UnSyncedPayPalItem {
        String billNum;
        String storeDate;
        String billTitle;
        String billTotalFee;
        String channel;
        String currency;
        String optional;

        public String getBillNum() {
            return billNum;
        }

        public String getStoreDate() {
            return storeDate;
        }

        public String getBillTitle() {
            return billTitle;
        }

        public String getBillTotalFee() {
            return billTotalFee;
        }

        public String getChannel() {
            return channel;
        }

        public String getCurrency() {
            return currency;
        }

        public String getOptional() {
            return optional;
        }
    }

    private class ViewHolder {
        public TextView txtBillNum;
        public TextView txtStoreDate;
        public TextView txtTotalFee;
        public TextView txtChannel;
        public TextView txtTitle;
        public TextView txtCurrency;
        public TextView txtOptional;
    }
}
