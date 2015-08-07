/**
 * ShoppingCartActivity.java
 *
 * Created by xuanzhui on 2015/7/29.
 * Copyright (c) 2015 BeeCloud. All rights reserved.
 */
package cn.beecloud.sdk_demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.Holder;
import com.orhanobut.dialogplus.ListHolder;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.OnItemClickListener;
import com.unionpay.UPPayAssistEx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cn.beecloud.BCPay;
import cn.beecloud.BeeCloud;
import cn.beecloud.async.BCCallback;
import cn.beecloud.async.BCResult;
import cn.beecloud.entity.BCPayResult;


public class ShoppingCartActivity extends Activity {

    private static final String TAG = ShoppingCartActivity.class.getSimpleName();
    Button btnPay;
    Button btnReqQRCode;
    Button btnQueryBills;
    Button btnQueryRefunds;
    Button btnRefundStatus;

    private ProgressDialog loadingDialog;

    private String[] names = new String[]{
            "衣服", "裤子", "鞋子",
    };
    private String[] descs = new String[]{
            "我的新衣服", "我的新裤子", "我的新鞋子"
    };
    private int[] imageIds = new int[]{
            R.drawable.yifu, R.drawable.kuzi, R.drawable.xiezi
    };
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);

        // 推荐在主Activity里的onCreate函数中初始化BeeCloud.
        BeeCloud.setAppIdAndSecret("c5d1cba1-5e3f-4ba0-941d-9b0a371fe719", "39a7a518-9ac8-4a9e-87bc-7885f33cf18c");

        // 如果用到微信支付，在用到微信支付的Activity的onCreate函数里调用以下函数.
        // 第二个参数需要换成你自己的微信AppID.
        BCPay.initWechatPay(ShoppingCartActivity.this, "wx19433a59b15fe84d");

        // Defines a Handler object that's attached to the UI thread.
        // 通过Handler.Callback()可消除内存泄漏警告
        mHandler = new Handler(new Handler.Callback() {
            /**
             * Callback interface you can use when instantiating a Handler to avoid
             * having to implement your own subclass of Handler.
             *
             * handleMessage() defines the operations to perform when
             * the Handler receives a new Message to process.
             */
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == 3) {
                    //如果用户手机没有安装银联支付控件,则会提示用户安装
                    AlertDialog.Builder builder = new AlertDialog.Builder(ShoppingCartActivity.this);
                    builder.setTitle("提示");
                    builder.setMessage("完成支付需要安装银联支付控件，是否安装？");

                    builder.setNegativeButton("确定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    UPPayAssistEx.installUPPayPlugin(ShoppingCartActivity.this);
                                    dialog.dismiss();
                                }
                            });

                    builder.setPositiveButton("取消",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    builder.create().show();
                }
                return true;
            }
        });

        btnPay = (Button) findViewById(R.id.btnPay);
        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogPlus(Gravity.BOTTOM);
            }
        });

        btnReqQRCode = (Button) findViewById(R.id.btnReqQRCode);
        btnReqQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShoppingCartActivity.this, GenQRCodeActivity.class);
                startActivity(intent);
            }
        });

        btnQueryBills = (Button) findViewById(R.id.btnQueryBills);
        btnQueryBills.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShoppingCartActivity.this, BillListActivity.class);
                startActivity(intent);
            }
        });

        btnQueryRefunds = (Button) findViewById(R.id.btnQueryRefunds);
        btnQueryRefunds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShoppingCartActivity.this, RefundOrdersActivity.class);
                startActivity(intent);
            }
        });

        btnRefundStatus = (Button) findViewById(R.id.btnRefundStatus);
        btnRefundStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShoppingCartActivity.this, RefundStatusActivity.class);
                startActivity(intent);
            }
        });

        List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < names.length; i++) {
            Map<String, Object> listItem = new HashMap<String, Object>();
            listItem.put("icon", imageIds[i]);
            listItem.put("name", names[i]);
            listItem.put("desc", descs[i]);
            listItems.add(listItem);
        }

        ShoppingListAdapter adapter = new ShoppingListAdapter(this, listItems);
        ListView listView = (ListView) findViewById(R.id.lstViewShoppingCart);
        listView.setAdapter(adapter);

    }

    private void showDialogPlus(int gravity) {

        Holder holder = new ListHolder();

        OnClickListener clickListener = new OnClickListener() {
            @Override
            public void onClick(DialogPlus dialog, View view) {
                switch (view.getId()) {
                    case R.id.header_container:
                        Toast.makeText(ShoppingCartActivity.this, "Header clicked", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.footer_confirm_button:
                        Toast.makeText(ShoppingCartActivity.this, "Confirm button clicked", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.footer_close_button:
                        Toast.makeText(ShoppingCartActivity.this, "Close button clicked", Toast.LENGTH_SHORT).show();
                        break;
                }
                dialog.dismiss();
            }
        };

        OnItemClickListener itemClickListener = new OnItemClickListener() {
            @Override
            public void onItemClick(DialogPlus dialog, Object item, View view, int position) {
                TextView textView = (TextView) view.findViewById(R.id.text_view);
                String clickItem = textView.getText().toString();
                dialog.dismiss();

                BCCallback bcCallback = new BCCallback() {
                    @Override
                    public void done(final BCResult bcResult) {
                        final BCPayResult bcPayResult = (BCPayResult)bcResult;
                        //此处关闭loading界面
                        loadingDialog.dismiss();

                        //对于支付宝，如果想通过Toast通知用户结果，请使用如下方式，
                        // 直接makeText会造成java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()
                        ShoppingCartActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                String result = bcPayResult.getResult();

                                if (result.equals(BCPayResult.RESULT_SUCCESS))
                                    Toast.makeText(ShoppingCartActivity.this, "用户支付成功", Toast.LENGTH_LONG).show();
                                else if (result.equals(BCPayResult.RESULT_CANCEL))
                                    Toast.makeText(ShoppingCartActivity.this, "用户取消支付", Toast.LENGTH_LONG).show();
                                else if(result.equals(BCPayResult.RESULT_FAIL)) {
                                    Toast.makeText(ShoppingCartActivity.this, "支付失败, 原因: " + bcPayResult.getErrMsg()
                                                + ", " + bcPayResult.getDetailInfo(), Toast.LENGTH_LONG).show();

                                    if (bcPayResult.getErrMsg().equals(BCPayResult.FAIL_PLUGIN_NOT_INSTALLED) ||
                                            bcPayResult.getErrMsg().equals(BCPayResult.FAIL_PLUGIN_NEED_UPGRADE)) {
                                        //银联需要重新安装控件
                                        Message msg = mHandler.obtainMessage();
                                        msg.what = 3;
                                        mHandler.sendMessage(msg);
                                    }
                                }
                                else{
                                    Toast.makeText(ShoppingCartActivity.this, "invalid return", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                };

                // 如果调起支付太慢，可以在这里开启动画，表示正在loading
                //以progressdialog为例
                loadingDialog = new ProgressDialog(ShoppingCartActivity.this);
                loadingDialog.setMessage("启动第三方支付，请稍候...");
                loadingDialog.setIndeterminate(true);
                loadingDialog.setCancelable(true);
                loadingDialog.show();

                Map<String, String> mapOptional;

                if (clickItem.equals("微信支付")) {
                    //对于微信支付, 手机内存太小会有OutOfResourcesException造成的卡顿, 以致无法完成支付
                    //这个是微信自身存在的问题
                    mapOptional = new HashMap<String, String>();

                    mapOptional.put("testkey1", "测试value值1");   //map的key暂时不支持中文

                    if (BCPay.isWXAppInstalledAndSupported() &&
                            BCPay.isWXPaySupported()) {
                        //订单标题, 订单金额(分), 订单号, 扩展参数(可以null), 支付完成后回调入口
                        BCPay.getInstance(ShoppingCartActivity.this).reqWXPaymentAsync("微信支付测试", 1,
                                UUID.randomUUID().toString().replace("-", ""), mapOptional, bcCallback);
                    }
                }
                else if (clickItem.equals("支付宝支付")) {
                    mapOptional = new HashMap<String, String>();
                    mapOptional.put("paymentid", "");
                    mapOptional.put("consumptioncode", "consumptionCode");
                    mapOptional.put("money", "2");

                    BCPay.getInstance(ShoppingCartActivity.this).reqAliPaymentAsync("支付宝支付测试", 1,
                            UUID.randomUUID().toString().replace("-", ""), mapOptional, bcCallback);

                }
                else if (clickItem.equals("银联支付")){

                    BCPay.getInstance(ShoppingCartActivity.this).reqUnionPaymentAsync("银联支付测试", 1,
                            UUID.randomUUID().toString().replace("-", ""), null, bcCallback);

                }
            }
        };

        DialogPlusAdapter adapter = new DialogPlusAdapter(ShoppingCartActivity.this);
        showCompleteDialog(holder, gravity, adapter, clickListener, itemClickListener);
    }


    private void showCompleteDialog(Holder holder, int gravity, BaseAdapter adapter,
                                    OnClickListener clickListener, OnItemClickListener itemClickListener) {
        final DialogPlus dialog = DialogPlus.newDialog(this)
                .setContentHolder(holder)
                .setHeader(R.layout.header)
                .setFooter(R.layout.footer)
                .setCancelable(true)
                .setGravity(gravity)
                .setAdapter(adapter)
                .setOnClickListener(clickListener)
                .setOnItemClickListener(itemClickListener)
                .create();
        dialog.show();
    }
}
