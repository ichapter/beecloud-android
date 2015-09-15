/**
 * BillListActivity.java
 *
 * Created by xuanzhui on 2015/7/29.
 * Copyright (c) 2015 BeeCloud. All rights reserved.
 */
package cn.beecloud.demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.beecloud.BCQuery;
import cn.beecloud.async.BCCallback;
import cn.beecloud.async.BCResult;
import cn.beecloud.entity.BCBillOrder;
import cn.beecloud.entity.BCQueryBillOrderResult;
import cn.beecloud.entity.BCReqParams;

/**
 * 用于展示订单查询
 */
public class BillListActivity extends Activity {
    public static final String TAG = "BillListActivity";

    Button btnWeChatOrder;
    Button btnAliPayOrder;
    Button btnUNPayOrder;
    Button btnBDPayOrder;
    Button btnPayPalPayOrder;
    Button btnAllPayOrder;
    ListView listViewOrder;

    private Handler mHandler;
    private List<BCBillOrder> bills;

    private ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_list);

        loadingDialog = new ProgressDialog(BillListActivity.this);
        loadingDialog.setMessage("正在请求服务器, 请稍候...");
        loadingDialog.setIndeterminate(true);
        loadingDialog.setCancelable(true);

        // Defines a Handler object that's attached to the UI thread.
        // 通过Handler.Callback()可消除内存泄漏警告
        mHandler = new Handler(new Handler.Callback() {
            /**
             * Callback interface you can use when instantiating a Handler to
             * avoid having to implement your own subclass of Handler.
             *
             * handleMessage() defines the operations to perform when the
             * Handler receives a new Message to process.
             */
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == 1) {

                    BillListAdapter adapter = new BillListAdapter(
                            BillListActivity.this, bills);
                    listViewOrder.setAdapter(adapter);
                }
                return true;
            }
        });


        listViewOrder = (ListView) findViewById(R.id.listViewOrder);

        //回调入口
        final BCCallback bcCallback = new BCCallback() {
            @Override
            public void done(BCResult bcResult) {

                //此处关闭loading界面
                loadingDialog.dismiss();

                final BCQueryBillOrderResult bcQueryResult = (BCQueryBillOrderResult) bcResult;

                //resultCode为0表示请求成功
                //count包含返回的订单个数
                if (bcQueryResult.getResultCode() == 0) {

                    //订单列表
                    bills = bcQueryResult.getBills();

                    Log.i(BillListActivity.TAG, "bill count: " + bcQueryResult.getCount());

                } else {
                    //订单列表
                    bills = null;

                    BillListActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(BillListActivity.this, "err code:" + bcQueryResult.getResultCode() +
                                    "; err msg: " + bcQueryResult.getResultMsg() +
                                    "; err detail: " + bcQueryResult.getErrDetail(), Toast.LENGTH_LONG).show();
                        }
                    });

                }

                Message msg = mHandler.obtainMessage();
                msg.what = 1;
                mHandler.sendMessage(msg);
            }
        };

        btnWeChatOrder = (Button) findViewById(R.id.btnWeChatOrder);
        btnWeChatOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 如果调起支付太慢，可以在这里开启动画，表示正在loading
                //以progressdialog为例
                loadingDialog.show();

                BCQuery.getInstance().queryBillsAsync(
                        BCReqParams.BCChannelTypes.WX_APP,  //此处表示微信app端支付的查询
                        bcCallback);
            }
        });

        btnAliPayOrder = (Button) findViewById(R.id.btnAliPayOrder);
        btnAliPayOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loadingDialog.show();

                BCQuery.getInstance().queryBillsAsync(
                        BCReqParams.BCChannelTypes.ALI_APP, //渠道
                        "20150820102712150", //订单号
                        bcCallback);
            }
        });

        btnUNPayOrder = (Button) findViewById(R.id.btnUNPayOrder);
        btnUNPayOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loadingDialog.show();

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

                Date startTime, endTime;
                try {
                    startTime = sdf.parse("2015-08-01 00:00");
                    endTime = sdf.parse("2015-08-31 23:59");
                } catch (ParseException e) {
                    startTime = new Date();
                    endTime = new Date();
                    e.printStackTrace();
                }

                BCQuery.getInstance().queryBillsAsync(
                        BCReqParams.BCChannelTypes.UN,          //渠道, 此处表示所有的银联支付
                        null,                                   //订单号
                        startTime.getTime(),                    //订单生成时间
                        endTime.getTime(),                      //订单完成时间
                        2,                                      //忽略满足条件的前2条数据
                        15,                                      //只返回满足条件的15条数据
                        bcCallback);
            }
        });

        btnBDPayOrder = (Button) findViewById(R.id.btnBDPayOrder);
        btnBDPayOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 如果调起支付太慢，可以在这里开启动画，表示正在loading
                //以progressdialog为例
                loadingDialog.show();

                BCQuery.getInstance().queryBillsAsync(
                        BCReqParams.BCChannelTypes.BD,  //此处表示百度钱包支付的查询
                        bcCallback);
            }
        });

        btnPayPalPayOrder = (Button) findViewById(R.id.btnPayPalPayOrder);
        btnPayPalPayOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 如果调起支付太慢，可以在这里开启动画，表示正在loading
                //以progressdialog为例
                loadingDialog.show();

                BCQuery.getInstance().queryBillsAsync(
                        BCReqParams.BCChannelTypes.PAYPAL,  //此处表示PAYPAL支付的查询
                        bcCallback);
            }
        });

        btnAllPayOrder = (Button) findViewById(R.id.btnAllPayOrder);
        btnAllPayOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loadingDialog.show();

                BCQuery.getInstance().queryBillsAsync(
                        BCReqParams.BCChannelTypes.ALL, //全渠道查询
                        bcCallback);
            }
        });
    }

}
