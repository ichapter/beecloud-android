/**
 * DialogPlusAdapter.java
 * <p/>
 * Created by xuanzhui on 2015/7/29.
 * Copyright (c) 2015 BeeCloud. All rights reserved.
 */
package cn.beecloud.sdk_demo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DialogPlusAdapter  extends BaseAdapter {

    private LayoutInflater layoutInflater;

    public DialogPlusAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        View view = convertView;

        if (view == null) {
            view = layoutInflater.inflate(R.layout.dialogplus_list_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.textView = (TextView) view.findViewById(R.id.text_view);
            viewHolder.imageView = (ImageView) view.findViewById(R.id.image_view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        Context context = parent.getContext();
        switch (position) {
            case 0:
                viewHolder.textView.setText(context.getString(R.string.wechat_title));
                viewHolder.imageView.setImageResource(R.drawable.wechat);
                break;
            case 1:
                viewHolder.textView.setText(context.getString(R.string.alipay_title));
                viewHolder.imageView.setImageResource(R.drawable.alipay);
                break;
            case 2:
                viewHolder.textView.setText(context.getString(R.string.unionpay_title));
                viewHolder.imageView.setImageResource(R.drawable.unionpay);
                break;
        }

        return view;
    }

    class ViewHolder {
        TextView textView;
        ImageView imageView;
    }

}
