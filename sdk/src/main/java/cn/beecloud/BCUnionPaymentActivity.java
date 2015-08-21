/**
 * BCUnionPaymentActivity.java
 *
 * Created by xuanzhui on 2015/7/27.
 * Copyright (c) 2015 BeeCloud. All rights reserved.
 */
package cn.beecloud;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.unionpay.UPPayAssistEx;

import cn.beecloud.entity.BCPayResult;

/**
 * 用于银联支付
 */
public class BCUnionPaymentActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart(){
        super.onStart();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String tn= extras.getString("tn");
            int retPay = UPPayAssistEx.startPay(this, null, null, tn, "00");

            //插件问题 -1表示没有安装插件，2表示插件需要升级
            if (retPay==-1 || retPay==2) {

                BCPay instance = BCPay.getInstance(BCUnionPaymentActivity.this);

                if (instance != null && instance.payCallback != null) {
                    instance.payCallback.done(new BCPayResult(BCPayResult.RESULT_FAIL,
                         (retPay==-1)? BCPayResult.FAIL_PLUGIN_NOT_INSTALLED:BCPayResult.FAIL_PLUGIN_NEED_UPGRADE,
                         "银联插件问题, 需重新安装或升级"));
                } else {
                    Log.e("BCUnionPaymentActivity", "BCPay instance or payCallback NPE");
                }

                this.finish();
            }
        }
    }

    /**
     * 处理银联手机支付控件返回的支付结果
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }

        String result = null;
        String errMsg = null;
        String detailInfo = "银联支付:";
        /*
         * 支付控件返回字符串:success、fail、cancel 分别代表支付成功，支付失败，支付取消
         */
        String str = data.getExtras().getString("pay_result");
        if (str == null) {
            result = BCPayResult.RESULT_FAIL;
            errMsg = BCPayResult.FAIL_ERR_FROM_CHANNEL;
            detailInfo += "invalid pay_result";
        } else {
            if (str.equalsIgnoreCase("success")) {
                result = BCPayResult.RESULT_SUCCESS;
                errMsg = BCPayResult.RESULT_SUCCESS;
                detailInfo += "支付成功！";
            } else if (str.equalsIgnoreCase("fail")) {
                result = BCPayResult.RESULT_FAIL;
                errMsg = BCPayResult.RESULT_FAIL;
                detailInfo += "支付失败！";
            } else if (str.equalsIgnoreCase("cancel")) {
                result = BCPayResult.RESULT_CANCEL;
                errMsg = BCPayResult.RESULT_CANCEL;
                detailInfo += "用户取消了支付";
            }
        }

        BCPay instance = BCPay.getInstance(BCUnionPaymentActivity.this);

        if (instance != null && instance.payCallback != null) {
            instance.payCallback.done(new BCPayResult(result, errMsg, detailInfo));
        }

        this.finish();
    }
}
