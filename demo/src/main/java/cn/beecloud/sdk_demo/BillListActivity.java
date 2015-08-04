/**
 * BillListActivity.java
 *
 * Created by xuanzhui on 2015/7/29.
 * Copyright (c) 2015 BeeCloud. All rights reserved.
 */
package cn.beecloud.sdk_demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import cn.beecloud.entity.BCBill;
import cn.beecloud.entity.BCQueryOrderResult;
import cn.beecloud.entity.BCReqParams;


public class BillListActivity extends Activity {
    public static final String TAG = "BillListActivity";

    Button btnWeChatOrder;
    Button btnAliPayOrder;
    Button btnUNPayOrder;
    ListView listViewOrder;

    private Handler mHandler;
    private List<BCBill> bills;

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

                final BCQueryOrderResult bcQueryResult = (BCQueryOrderResult) bcResult;

                //resultCode为0表示请求成功
                //count包含返回的订单个数
                if (bcQueryResult.getResultCode() == 0) {

                    //订单列表
                    bills = bcQueryResult.getOrders();

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
                        BCReqParams.BCChannelTypes.WX_APP, bcCallback);
            }
        });

        btnAliPayOrder = (Button) findViewById(R.id.btnAliPayOrder);
        btnAliPayOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loadingDialog.show();

                BCQuery.getInstance().queryBillsAsync(
                        BCReqParams.BCChannelTypes.ALI_APP, //渠道
                        "5aca2865fbf348f9a415cf18da5f48f7", //订单号
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
                    startTime = sdf.parse("2015-07-29 12:00");
                    endTime = sdf.parse("2015-07-30 12:00");
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bill_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
