/**
 * ShoppingCartActivity.java
 *
 * Created by xuanzhui on 2015/7/29.
 * Copyright (c) 2015 BeeCloud. All rights reserved.
 */
package cn.beecloud.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.unionpay.UPPayAssistEx;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import cn.beecloud.BCPay;
import cn.beecloud.BeeCloud;
import cn.beecloud.async.BCCallback;
import cn.beecloud.async.BCResult;
import cn.beecloud.entity.BCPayResult;


public class ShoppingCartActivity extends Activity {

    Button btnQueryOrders;

    private ProgressDialog loadingDialog;
    private ListView payMethod;

    //支付结果返回入口
    BCCallback bcCallback = new BCCallback() {
        @Override
        public void done(final BCResult bcResult) {
            final BCPayResult bcPayResult = (BCPayResult)bcResult;
            //此处关闭loading界面
            loadingDialog.dismiss();

            //如果想通过Toast通知用户结果，请使用如下方式，
            // 直接makeText有可能会造成java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()
            ShoppingCartActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    String result = bcPayResult.getResult();

                    /*
                      注意！
                      所有支付渠道建议以服务端的状态为准，此处返回的RESULT_SUCCESS仅仅代表手机端支付成功，
                      该操作并非强制性要求，虽然属于小概率事件，但渠道官方推荐以防止欺诈手段

                        对于PayPal的每一次支付，sdk会自动帮你与服务端同步，
                        如果与服务端同步失败，记录会被自动保存，此时你可以调用batchSyncPayPalPayment方法手动同步
                        虽然这种情况比较少，但是建议参考PayPalUnSyncedListActivity做好同步，否则服务器将无法查阅到订单
                    */
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
                            msg.what = 1;
                            mHandler.sendMessage(msg);
                        }
                    } else if (result.equals(BCPayResult.RESULT_UNKNOWN)) {
                        //可能出现在支付宝8000返回状态
                        Toast.makeText(ShoppingCartActivity.this, "订单状态未知", Toast.LENGTH_LONG).show();
                    } else{
                        Toast.makeText(ShoppingCartActivity.this, "invalid return", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    };

    // Defines a Handler object that's attached to the UI thread.
    // 通过Handler.Callback()可消除内存泄漏警告
    private Handler mHandler = new Handler(new Handler.Callback() {
        /**
         * Callback interface you can use when instantiating a Handler to avoid
         * having to implement your own subclass of Handler.
         *
         * handleMessage() defines the operations to perform when
         * the Handler receives a new Message to process.
         */
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 1) {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);

        // 推荐在主Activity里的onCreate函数中初始化BeeCloud.
        //BeeCloud.setAppIdAndSecret("c5d1cba1-5e3f-4ba0-941d-9b0a371fe719", "39a7a518-9ac8-4a9e-87bc-7885f33cf18c");
        BeeCloud.setAppIdAndSecret("c37d661d-7e61-49ea-96a5-68c34e83db3b", "c37d661d-7e61-49ea-96a5-68c34e83db3b");
        //BeeCloud.setAppIdAndSecret("6e49a763-ff8c-4e18-903c-23bbc8c6b8f4", "52690b4e-faef-4cb9-9a59-7e4427f5fa45");

        // 如果用到微信支付，在用到微信支付的Activity的onCreate函数里调用以下函数.
        // 第二个参数需要换成你自己的微信AppID.
        BCPay.initWechatPay(ShoppingCartActivity.this, "wx19433a59b15fe84d");

        // 如果使用PayPal需要在支付之前设置client id和应用secret
        // BCPay.PAYPAL_PAY_TYPE.SANDBOX用于测试，BCPay.PAYPAL_PAY_TYPE.LIVE用于生产环境
        BCPay.initPayPal("AVT1Ch18aTIlUJIeeCxvC7ZKQYHczGwiWm8jOwhrREc4a5FnbdwlqEB4evlHPXXUA67RAAZqZM0H8TCR",
                "EL-fkjkEUyxrwZAmrfn46awFXlX-h2nRkyCVhhpeVdlSRuhPJKXx3ZvUTTJqPQuAeomXA8PZ2MkX24vF",
                BCPay.PAYPAL_PAY_TYPE.SANDBOX, Boolean.FALSE);

        payMethod = (ListView) this.findViewById(R.id.payMethod);
        Integer[] payIcons = new Integer[]{R.drawable.wechat, R.drawable.alipay,
                R.drawable.unionpay, R.drawable.baidupay ,R.drawable.paypal, R.drawable.scan};
        String[] payNames = new String[]{"微信支付", "支付宝支付",
                "银联在线", "百度钱包", "PayPal支付", "二维码支付"};
        String[] payDescs = new String[]{"使用微信支付，以人民币CNY计费", "使用支付宝支付，以人民币CNY计费",
                "使用银联在线支付，以人民币CNY计费", "使用百度钱包支付，以人民币CNY计费",
                "使用PayPal支付，以美元USD计费", "通过扫描二维码支付"};
        PayMethodListItem adapter = new PayMethodListItem(this, payIcons, payNames, payDescs);
        payMethod.setAdapter(adapter);

        // 如果调起支付太慢, 可以在这里开启动画, 以progressdialog为例
        loadingDialog = new ProgressDialog(ShoppingCartActivity.this);
        loadingDialog.setMessage("启动第三方支付，请稍候...");
        loadingDialog.setIndeterminate(true);
        loadingDialog.setCancelable(true);

        payMethod.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {

                    case 0: //微信
                        loadingDialog.show();
                        //对于微信支付, 手机内存太小会有OutOfResourcesException造成的卡顿, 以致无法完成支付
                        //这个是微信自身存在的问题
                        Map<String, String> mapOptional = new HashMap<String, String>();

                        mapOptional.put("testkey1", "测试value值1");   //map的key暂时不支持中文

                        if (BCPay.isWXAppInstalledAndSupported() &&
                                BCPay.isWXPaySupported()) {

                            BCPay.getInstance(ShoppingCartActivity.this).reqWXPaymentAsync(
                                    "微信支付测试",               //订单标题
                                    1,                           //订单金额(分)
                                    UUID.randomUUID().toString().replace("-", ""),  //订单流水号
                                    mapOptional,            //扩展参数(可以null)
                                    bcCallback);            //支付完成后回调入口
                        }
                        break;

                    case 1: //支付宝支付
                        loadingDialog.show();

                        mapOptional = new HashMap<String, String>();
                        mapOptional.put("paymentid", "");
                        mapOptional.put("consumptioncode", "consumptionCode");
                        mapOptional.put("money", "2");

                        BCPay.getInstance(ShoppingCartActivity.this).reqAliPaymentAsync("支付宝支付测试", 1,
                                UUID.randomUUID().toString().replace("-", ""), mapOptional, bcCallback);
                        break;

                    case 2: //银联支付
                        loadingDialog.show();

                        BCPay.getInstance(ShoppingCartActivity.this).reqUnionPaymentAsync("银联支付测试", 1,
                                UUID.randomUUID().toString().replace("-", ""), null, bcCallback);
                        break;
                    case 3: //通过百度钱包支付
                        loadingDialog.show();

                        HashMap<String, String> hashMapOptional = new HashMap<String, String>();
                        hashMapOptional.put("goods desc", "商品详细描述");

                        BCPay.getInstance(ShoppingCartActivity.this).reqBaiduPaymentAsync("Baidu钱包支付测试", 1,
                                UUID.randomUUID().toString().replace("-", ""), hashMapOptional, bcCallback);
                        break;
                    case 4: //通过PayPal支付
                        loadingDialog.show();

                        hashMapOptional = new HashMap<String, String>();
                        hashMapOptional.put("PayPal key1", "PayPal value1");
                        hashMapOptional.put("PayPal key2", "PayPal value2");

                        BCPay.getInstance(ShoppingCartActivity.this).reqPayPalPaymentAsync("PayPal payment test", 1,
                                hashMapOptional, bcCallback);
                        break;
                    default:
                        Intent intent = new Intent(ShoppingCartActivity.this, GenQRCodeActivity.class);
                        startActivity(intent);
                }
            }
        });

        btnQueryOrders = (Button) findViewById(R.id.btnQueryOrders);
        btnQueryOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShoppingCartActivity.this, OrdersEntryActivity.class);
                startActivity(intent);
            }
        });
    }

}
