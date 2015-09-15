/**
 * GenQRCodeActivity.java
 *
 * Created by xuanzhui on 2015/8/6.
 * Copyright (c) 2015 BeeCloud. All rights reserved.
 */
package cn.beecloud.demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import cn.beecloud.BCPay;
import cn.beecloud.async.BCCallback;
import cn.beecloud.async.BCResult;
import cn.beecloud.entity.BCQRCodeResult;

/**
 * 用于展示如何生成二维码支付
 */
public class GenQRCodeActivity extends Activity {
    private static final String Tag = "GenQRCodeActivity";
    private ProgressDialog loadingDialog;

    Button btnReqWXQRCode;
    Button btnReqALIQRCode;
    private ImageView wxQRImg;

    private Handler mHandler;

    private Bitmap wxQRBitmap;
    private String aliQRHtml;
    private String aliQRURL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gen_qrcode);

        loadingDialog = new ProgressDialog(GenQRCodeActivity.this);
        loadingDialog.setMessage("正在请求服务器, 请稍候...");
        loadingDialog.setIndeterminate(true);
        loadingDialog.setCancelable(true);

        btnReqWXQRCode = (Button) findViewById(R.id.btnReqWXQRCode);
        wxQRImg = (ImageView) findViewById(R.id.wxQRImg);

        btnReqALIQRCode = (Button) findViewById(R.id.btnReqALIQRCode);

        // Defines a Handler object that's attached to the UI thread.
        // 通过Handler.Callback()可消除内存泄漏警告
        mHandler = new Handler(new Handler.Callback() {

            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        wxQRImg.setImageBitmap(wxQRBitmap);
                        break;
                    case 2:
                        //建议在新的activity显示ali内嵌二维码
                        Intent intent = new Intent(GenQRCodeActivity.this, ALIQRCodeActivity.class);
                        intent.putExtra("aliQRURL", aliQRURL);
                        intent.putExtra("aliQRHtml", aliQRHtml);
                        startActivity(intent);
                        break;
                }

                return true;
            }
        });

        btnReqWXQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingDialog.show();

                Map<String, String> mapOptional = new HashMap<String, String>();

                mapOptional.put("testkey1", "测试value值1");
                BCPay.getInstance(GenQRCodeActivity.this).reqWXQRCodeAsync("微信二维码支付测试", //商品描述
                        1,                          //总金额, 以分为单位, 必须是正整数
                        UUID.randomUUID().toString().replace("-", ""),          //流水号
                        mapOptional,            //扩展参数
                        true,                   //是否生成二维码的bitmap,
                                                //如果为false，请自行根据getQrCodeRawContent返回的结果
                                                //使用BCPay.generateBitmap方法生成支付二维码
                                                //你也可以使用自己熟悉的二维码生成工具
                        300,                   //二维码的尺寸, 以px为单位, 如果为null则默认为360
                        new BCCallback() {     //回调入口
                            @Override
                            public void done(BCResult bcResult) {

                                //此处关闭loading界面
                                loadingDialog.dismiss();

                                final BCQRCodeResult bcqrCodeResult = (BCQRCodeResult) bcResult;

                                //resultCode为0表示请求成功
                                if (bcqrCodeResult.getResultCode() == 0) {
                                    wxQRBitmap = bcqrCodeResult.getQrCodeBitmap();
                                    Log.w(Tag, "weixin qrcode url: " + bcqrCodeResult.getQrCodeRawContent());

                                } else {

                                    GenQRCodeActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(GenQRCodeActivity.this, "err code:" + bcqrCodeResult.getResultCode() +
                                                    "; err msg: " + bcqrCodeResult.getResultMsg() +
                                                    "; err detail: " + bcqrCodeResult.getErrDetail(), Toast.LENGTH_LONG).show();
                                        }
                                    });

                                }

                                Message msg = mHandler.obtainMessage();
                                msg.what = 1;
                                mHandler.sendMessage(msg);
                            }
                        });
            }
        });


        btnReqALIQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingDialog.show();

                Map<String, String> mapOptional = new HashMap<String, String>();

                mapOptional.put("testalikey1", "测试value值1");
                BCPay.getInstance(GenQRCodeActivity.this).reqAliQRCodeAsync("支付宝内嵌二维码支付测试",   //商品描述
                        1,                                                  //总金额, 以分为单位, 必须是正整数
                        UUID.randomUUID().toString().replace("-", ""),      //流水号
                        mapOptional,                                        //扩展参数
                        "https://beecloud.cn/",  //支付成功之后的返回url
                        "1",                          /* 注： 二维码类型含义
                                                        * null则支付宝生成默认类型, 不建议
                                                        * "0": 订单码-简约前置模式, 对应 iframe 宽度不能小于 600px, 高度不能小于 300px
                                                        * "1": 订单码-前置模式, 对应 iframe 宽度不能小于 300px, 高度不能小于 600px
                                                        * "3": 订单码-迷你前置模式, 对应 iframe 宽度不能小于 75px, 高度不能小于 75px
                                                        */
                        new BCCallback() {     //回调入口
                            @Override
                            public void done(BCResult bcResult) {

                                //此处关闭loading界面
                                loadingDialog.dismiss();

                                final BCQRCodeResult bcqrCodeResult = (BCQRCodeResult) bcResult;

                                //resultCode为0表示请求成功
                                if (bcqrCodeResult.getResultCode() == 0) {
                                    aliQRURL = bcqrCodeResult.getQrCodeRawContent();
                                    aliQRHtml = bcqrCodeResult.getAliQRCodeHtml();
                                    Log.w(Tag, "ali qrcode url: " + aliQRURL);
                                    Log.w(Tag, "ali qrcode html: " + aliQRHtml);

                                } else {

                                    GenQRCodeActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(GenQRCodeActivity.this, "err code:" + bcqrCodeResult.getResultCode() +
                                                    "; err msg: " + bcqrCodeResult.getResultMsg() +
                                                    "; err detail: " + bcqrCodeResult.getErrDetail(), Toast.LENGTH_LONG).show();
                                        }
                                    });

                                }

                                Message msg = mHandler.obtainMessage();
                                msg.what = 2;
                                mHandler.sendMessage(msg);
                            }
                        });
            }
        });

    }
}
