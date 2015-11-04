/**
 * BillListAdapter.java
 * <p/>
 * Created by xuanzhui on 2015/7/29.
 * Copyright (c) 2015 BeeCloud. All rights reserved.
 */
package cn.beecloud.demo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import cn.beecloud.entity.BCBillOrder;
import cn.beecloud.entity.BCReqParams;

public class BillListAdapter extends BaseAdapter {

    private List<BCBillOrder> bills;
    private LayoutInflater mInflater;

    public BillListAdapter(Context context, List<BCBillOrder> bills) {
        this.bills = bills;
        mInflater = LayoutInflater.from(context);
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        if (bills == null)
            return 0;

        return bills.size();
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
        return bills.get(position);
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link android.view.LayoutInflater#inflate(int, android.view.ViewGroup, boolean)}
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
            convertView = mInflater.inflate(R.layout.bill_list_item, null);

            viewHolder = new ViewHolder();

            viewHolder.txtBillNum = (TextView) convertView
                    .findViewById(R.id.txtBillNum);
            viewHolder.txtTradeNum = (TextView) convertView
                    .findViewById(R.id.txtTradeNum);
            viewHolder.txtTotalFee = (TextView) convertView
                    .findViewById(R.id.txtTotalFee);
            viewHolder.txtChannel = (TextView) convertView
                    .findViewById(R.id.txtChannel);
            viewHolder.txtSubChannel = (TextView) convertView
                    .findViewById(R.id.txtSubChannel);
            viewHolder.txtTitle = (TextView) convertView
                    .findViewById(R.id.txtTitle);
            viewHolder.txtPayResult = (TextView) convertView
                    .findViewById(R.id.txtPayResult);
            viewHolder.txtCreatedTime = (TextView) convertView
                    .findViewById(R.id.txtCreatedTime);
            viewHolder.txtOptional = (TextView) convertView
                    .findViewById(R.id.txtOptional);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        BCBillOrder bcBillOrder = bills.get(position);

        viewHolder.txtBillNum.setText("订单号: " + bcBillOrder.getBillNum());
        viewHolder.txtTradeNum.setText("渠道商的订单号: " + bcBillOrder.getTradeNum());
        viewHolder.txtTotalFee.setText("订单金额/元: " + (bcBillOrder.getTotalFee()/100.0));
        viewHolder.txtChannel.setText("支付渠道: " + BCReqParams.BCChannelTypes.getTranslatedChannelName(bcBillOrder.getChannel()));
        viewHolder.txtSubChannel.setText("子渠道: " + BCReqParams.BCChannelTypes.getTranslatedChannelName(bcBillOrder.getSubChannel()));
        viewHolder.txtTitle.setText("订单标题: " + bcBillOrder.getTitle());
        viewHolder.txtPayResult.setText("订单是否支付完成: " + (bcBillOrder.getPayResult()?"是":"否"));
        viewHolder.txtCreatedTime.setText("订单生成时间: " + new Date(bcBillOrder.getCreatedTime()));
        viewHolder.txtOptional.setText("扩展参数: " + bcBillOrder.getOptional());
        return convertView;
    }

    private class ViewHolder {
        public TextView txtBillNum;
        public TextView txtTradeNum;
        public TextView txtTotalFee;
        public TextView txtChannel;
        public TextView txtSubChannel;
        public TextView txtTitle;
        public TextView txtPayResult;
        public TextView txtCreatedTime;
        public TextView txtOptional;
    }
}
