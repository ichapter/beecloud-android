/**
 * ShoppingCartActivity.java
 *
 * Created by xuanzhui on 2015/7/29.
 * Copyright (c) 2015 BeeCloud. All rights reserved.
 */
package cn.beecloud.sdk_demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

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
    
    Button btnWXPay;
    Button btnAliPay;
    Button btnUNPay;
    
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

    @SuppressLint("NewApi")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);

        // 推荐在主Activity里的onCreate函数中初始化BeeCloud
        BeeCloud.setAppIdAndSecret("c5d1cba1-5e3f-4ba0-941d-9b0a371fe719", "39a7a518-9ac8-4a9e-87bc-7885f33cf18c");

        // 如果用到微信支付，比如在用到微信支付的Activity的onCreate函数里调用以下函数.
        // 第二个参数需要换成你自己的微信AppID.
        BCPay.initWechatPay(ShoppingCartActivity.this, "wxf1aa465362b4c8f1");

        // Defines a Handler object that's attached to the UI thread.
        // 通过Handler.Callback()可消除内存泄漏警告
        mHandler = new Handler(new Handler.Callback() {

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
                } else if (msg.what == 4) {

                } else if (msg.what == 5) {

                }
                return true;
            }
        });

        final BCCallback bcCallback = new BCCallback() {
            @Override
            public void done(final BCResult bcResult) {
            	//关闭loading动画
            	loadingDialog.dismiss();
            	
                final BCPayResult bcPayResult = (BCPayResult)bcResult;

                //对于支付宝，如果想通过Toast通知用户结果，请使用如下方式，
                // 直接makeText会造成java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()
                ShoppingCartActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        switch (bcPayResult.getResult()) {
                            case BCPayResult.RESULT_SUCCESS:
                                Toast.makeText(ShoppingCartActivity.this, "用户支付成功", Toast.LENGTH_LONG).show();
                                break;
                            case BCPayResult.RESULT_CANCEL:
                                Toast.makeText(ShoppingCartActivity.this, "用户取消支付", Toast.LENGTH_LONG).show();
                                break;
                            case BCPayResult.RESULT_FAIL:
                                Toast.makeText(ShoppingCartActivity.this, "支付失败, 原因: " + bcPayResult.getErrMsg()
                                        + ", " + bcPayResult.getDetailInfo(), Toast.LENGTH_LONG).show();

                                if (bcPayResult.getErrMsg().equals(BCPayResult.FAIL_PLUGIN_NOT_INSTALLED) ||
                                        bcPayResult.getErrMsg().equals(BCPayResult.FAIL_PLUGIN_NEED_UPGRADE)) {
                                    //银联需要重新安装控件
                                    Message msg = mHandler.obtainMessage();
                                    msg.what = 3;
                                    mHandler.sendMessage(msg);
                                }

                                break;
                        }
                    }
                });
            }
        };
        
        // 设置loading动画
        loadingDialog = new ProgressDialog(ShoppingCartActivity.this);
        loadingDialog.setMessage("启动第三方支付，请稍候...");
        loadingDialog.setIndeterminate(true);
        loadingDialog.setCancelable(true);
        
        btnWXPay = (Button) findViewById(R.id.btnWXPay);
        btnWXPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	//启动loading
            	loadingDialog.show();
            	
            	Map<String, String> mapOptional = new HashMap<>();
                String optionalKey = "testkey1";
                String optionalValue = "测试value值1";

                mapOptional.put(optionalKey, optionalValue);
                
                if (BCPay.isWXAppInstalledAndSupported() &&
                        BCPay.isWXPaySupported()) {
                    //订单标题, 订单金额(分), 订单号, 扩展参数(可以null), 支付完成后回调入口
                    BCPay.getInstance(ShoppingCartActivity.this).reqWXPaymentAsync("微信支付测试", "1",
                            UUID.randomUUID().toString().replace("-", ""), mapOptional, bcCallback);
                }
            }
        });
        
        btnAliPay = (Button) findViewById(R.id.btnAliPay);
        btnAliPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	//启动loading
            	loadingDialog.show();
            	
            	Map<String, String> mapOptional = new HashMap<>();
            	mapOptional = new HashMap<>();
                mapOptional.put("paymentid", "");
                mapOptional.put("consumptioncode", "consumptionCode");
                mapOptional.put("money", "2");

                BCPay.getInstance(ShoppingCartActivity.this).reqAliPaymentAsync("支付宝支付测试", "1",
                        UUID.randomUUID().toString().replace("-", ""), mapOptional, bcCallback);
            }
        });
        
        btnUNPay = (Button) findViewById(R.id.btnUNPay);
        btnUNPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	//启动loading
            	loadingDialog.show();
            	
            	BCPay.getInstance(ShoppingCartActivity.this).reqUnionPaymentAsync("银联支付测试", "1",
                        UUID.randomUUID().toString().replace("-", ""), null, bcCallback);
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


        List<Map<String, Object>> listItems = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            Map<String, Object> listItem = new HashMap<>();
            listItem.put("icon", imageIds[i]);
            listItem.put("name", names[i]);
            listItem.put("desc", descs[i]);
            listItems.add(listItem);
        }

        ShoppingListAdapter adapter = new ShoppingListAdapter(this, listItems);
        ListView listView = (ListView) findViewById(R.id.lstViewShoppingCart);
        listView.setAdapter(adapter);

    }
}
