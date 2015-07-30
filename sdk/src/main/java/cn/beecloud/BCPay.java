/**
 * BCPay.java
 *
 * Created by xuanzhui on 2015/7/27.
 * Copyright (c) 2015 BeeCloud. All rights reserved.
 */
package cn.beecloud;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.alipay.sdk.app.PayTask;
import com.google.gson.Gson;
import com.tencent.mm.sdk.constants.Build;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.beecloud.async.BCCallback;
import cn.beecloud.entity.BCPayReqParams;
import cn.beecloud.entity.BCPayResult;
import cn.beecloud.entity.BCReqParams;

/**
 * 支付类
 * 单例模式
 */
public class BCPay {
    private static final String TAG = "BCPay";

    /**
     * 保留callback实例
     */
    public static BCCallback payCallback;

    private static Activity mContextActivity;

    // IWXAPI 是第三方app和微信通信的openapi接口
    public static IWXAPI wxAPI = null;

    private static BCPay instance;

    private BCPay() {}

    /**
     * 唯一获取BCPay实例的入口
     * @param context
     * @return          BCPay实例
     */
    public synchronized static BCPay getInstance(Context context) {
        mContextActivity = (Activity)context;
        if (instance == null) {
            instance = new BCPay();
            payCallback = null;
        }
        return instance;
    }

    /**
     * 初始化微信支付，必须在需要调起微信支付的Activity的onCreate函数中调用，例如：
     * BCPay.initWechatPay(XXActivity.this);
     * 微信支付只有经过初始化才能成功调起，其他支付渠道无此要求。
     *
     * @param context      需要在某Activity里初始化微信支付，此参数需要传递该Activity.this，不能为null
     * @return             返回出错信息，如果成功则为null
     */
    public static String initWechatPay(Context context, String wechatAppID) {
        String errMsg = null;

        if (context == null) {
            errMsg = "Error: initWechatPay里，context参数不能为null.";
            Log.e(TAG, errMsg);
            return errMsg;
        }

        if (wechatAppID == null || wechatAppID.length() == 0) {
            errMsg = "Error: initWechatPay里，wx_appid必须为合法的微信AppID.";
            Log.e(TAG, errMsg);
            return errMsg;
        }

        // 通过WXAPIFactory工厂，获取IWXAPI的实例
        wxAPI = WXAPIFactory.createWXAPI(context, null);

        BCCache.getInstance().wxAppId = wechatAppID;

        try {
            if (isWXPaySupported()) {
                // 将该app注册到微信
                wxAPI.registerApp(wechatAppID);
            } else {
                errMsg = "Error: 安装的微信版本不支持支付.";
                Log.d(TAG, errMsg);
            }
        } catch (Exception ignored) {
            errMsg = "Error: 无法注册微信 " + wechatAppID + ". Exception: " + ignored.getMessage();
            Log.e(TAG, errMsg);
        }

        return errMsg;
    }

    /**
     * 判断微信是否支持支付
     * @return true表示支持
     */
    public static boolean isWXPaySupported() {
        boolean isPaySupported = false;
        if (wxAPI != null) {
            isPaySupported = wxAPI.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;
        }
        return isPaySupported;
    }

    /**
     * 判断微信客户端是否安装并被支持
     * @return true表示支持
     */
    public static boolean isWXAppInstalledAndSupported() {
        boolean isWXAppInstalledAndSupported = false;
        if (wxAPI != null) {
            isWXAppInstalledAndSupported = wxAPI.isWXAppInstalled() && wxAPI.isWXAppSupportAPI();
        }
        return isWXAppInstalledAndSupported;
    }

    /**
     * 判断微信客户端是否安装
     * @return true表示已安装
     */
    public static boolean isWXAppInstalled() {
        boolean isWXAppInstalled = false;
        if (wxAPI != null) {
            isWXAppInstalled = wxAPI.isWXAppInstalled();
        }
        return isWXAppInstalled;
    }

    /**
     * 判断微信客户端是否被支持
     * @return true表示支持
     */
    public static boolean isWXAppSupported() {

        boolean isWXAppSupportAPI = false;
        if (wxAPI != null) {
            isWXAppSupportAPI = wxAPI.isWXAppSupportAPI();
        }
        return isWXAppSupportAPI;
    }

    /**
     * 校验bill参数
     * 设置公用参数
     *
     * @param billTitle       商品描述, UTF8编码格式, 32个字节内
     * @param billTotalFee    支付金额，以分为单位，必须是整数格式
     * @param billNum         商户自定义订单号
     * @param parameters      用于存储公用信息
     * @param optional        为扩展参数，可以传入任意数量的key/value对来补充对业务逻辑的需求
     * @return                返回校验失败信息, 为null则表明校验通过
     */
    private String prepareParametersForPay(final String billTitle, final String billTotalFee,
                                           final String billNum, final Map<String, String> optional,
                                           BCPayReqParams parameters) {

        if (!BCValidationUtil.isValidString(billTitle) ||
                !BCValidationUtil.isValidString(billTotalFee) ||
                !BCValidationUtil.isValidString(billNum)) {
            return "parameters: 不合法的参数";
        }

        if (!BCValidationUtil.isStringValidPositiveInt(billTotalFee)) {
            return "parameters: billTotalFee " + billTotalFee +
                    " 格式不正确, 必须是以分为单位的正整数, 比如100表示1元";
        }

        parameters.title = billTitle;
        parameters.totalFee = Integer.valueOf(billTotalFee);
        parameters.billNum = billNum;
        parameters.optional = optional;

        return null;
    }

    /**
     * 支付调用总接口
     *
     * @param channelType     支付类型
     *                        @see cn.beecloud.entity.BCReqParams.BCChannelTypes
     * @param billTitle       商品描述, UTF8编码格式, 32个字节内
     * @param billTotalFee    支付金额，以分为单位，必须是整数格式
     * @param billNum         商户自定义订单号
     * @param optional        为扩展参数，可以传入任意数量的key/value对来补充对业务逻辑的需求
     * @param callback        支付完成后的回调函数
     */
    private void reqPaymentAsync(final String channelType,
                                 final String billTitle, final String billTotalFee,
                                 final String billNum,final Map<String, String> optional,
                                 final BCCallback callback) {

        if (callback == null) {
            Log.w(TAG, "请初始化callback");
            return;
        }

        payCallback = callback;

        BCCache.executorService.execute(new Runnable() {
            @Override
            public void run() {

                //校验并准备公用参数
                BCPayReqParams parameters = null;
                try {
                    parameters = new BCPayReqParams(channelType);
                } catch (BCException e) {
                    callback.done(new BCPayResult(BCPayResult.RESULT_FAIL, BCPayResult.FAIL_EXCEPTION,
                            e.getMessage()));
                    return;
                }

                String paramValidRes = prepareParametersForPay(billTitle, billTotalFee,
                        billNum, optional, parameters);

                if (paramValidRes != null) {
                    callback.done(new BCPayResult(BCPayResult.RESULT_FAIL, BCPayResult.FAIL_INVALID_PARAMS,
                            paramValidRes));
                    return;
                }

                String payURL = BCHttpClientUtil.getBillPayURL();

                HttpResponse response = BCHttpClientUtil.httpPost(payURL, parameters.transToBillReqMapParams());
                if (null == response) {
                    callback.done(new BCPayResult(BCPayResult.RESULT_FAIL, BCPayResult.FAIL_NETWORK_ISSUE,
                            "Network Error"));
                    return;
                }
                if (response.getStatusLine().getStatusCode() == 200) {
                    String ret;
                    try {
                        ret = EntityUtils.toString(response.getEntity(), "UTF-8");

                        Gson res = new Gson();
                        Map<String, Object> responseMap = res.fromJson(ret, HashMap.class);

                        //判断后台返回结果
                        Double resultCode = (Double) responseMap.get("result_code");
                        if (resultCode == 0) {


                            if (mContextActivity != null) {

                                //针对不同的支付渠道调用不同的API
                                if (channelType.equals(BCReqParams.BCChannelTypes.WX_APP))
                                    reqWXPaymentViaAPP(responseMap);
                                else if (channelType.equals(BCReqParams.BCChannelTypes.ALI_APP))
                                    reqAliPaymentViaAPP(responseMap);
                                else if (channelType.equals(BCReqParams.BCChannelTypes.UN_APP))
                                    reqUnionPaymentViaAPP(responseMap);
                                else
                                    callback.done(new BCPayResult(BCPayResult.RESULT_FAIL, BCPayResult.FAIL_INVALID_PARAMS,
                                            "channelType参数不合法"));

                            } else {
                                callback.done(new BCPayResult(BCPayResult.RESULT_FAIL, BCPayResult.FAIL_EXCEPTION,
                                        "Context-Activity Exception in reqAliPayment"));
                            }
                        } else {
                            //返回后端传回的错误信息
                            callback.done(new BCPayResult(BCPayResult.RESULT_FAIL, BCPayResult.FAIL_ERR_FROM_SERVER,
                                    String.valueOf(responseMap.get("result_msg")) +
                                            String.valueOf(responseMap.get("err_detail"))));
                        }

                    } catch (IOException e) {
                        callback.done(new BCPayResult(BCPayResult.RESULT_FAIL, BCPayResult.FAIL_NETWORK_ISSUE,
                                "Invalid Response"));
                    }
                } else {
                    callback.done(new BCPayResult(BCPayResult.RESULT_FAIL, BCPayResult.FAIL_NETWORK_ISSUE,
                            "Network Error"));
                }

            }
        });
    }

    /**
     * 与服务器交互后下一步进入微信app支付
     *
     * @param responseMap     服务端返回参数
     */
    private void reqWXPaymentViaAPP(final Map<String, Object> responseMap) {

        //获取到服务器的订单参数后，以下主要代码即可调起微信支付。
        PayReq request = new PayReq();
        request.appId = String.valueOf(responseMap.get("app_id"));
        request.partnerId = String.valueOf(responseMap.get("partner_id"));
        request.prepayId = String.valueOf(responseMap.get("prepay_id"));
        request.packageValue = String.valueOf(responseMap.get("package"));
        request.nonceStr = String.valueOf(responseMap.get("nonce_str"));
        request.timeStamp = String.valueOf(responseMap.get("timestamp"));
        request.sign = String.valueOf(responseMap.get("pay_sign"));

        if (wxAPI != null) {
            wxAPI.sendReq(request);
        } else {
            payCallback.done(new BCPayResult(BCPayResult.RESULT_FAIL, BCPayResult.FAIL_EXCEPTION,
                    "Error: 微信API为空, 请确认已经在需要调起微信支付的Activity的onCreate函数中调用BCPay.initWechatPay(XXActivity.this)"));
        }
    }

    /**
     * 与服务器交互后下一步进入支付宝app支付
     *
     * @param responseMap     服务端返回参数
     */
    private void reqAliPaymentViaAPP(final Map<String, Object> responseMap) {

        String orderString = (String) responseMap.get("order_string");

        PayTask aliPay = new PayTask(mContextActivity);
        String aliResult = aliPay.pay(orderString);

        //解析ali返回结果
        Pattern pattern = Pattern.compile("resultStatus=\\{(\\d+?)\\}");
        Matcher matcher = pattern.matcher(aliResult);
        String resCode = "";
        if (matcher.find())
            resCode = matcher.group(1);

        String result;
        String errMsg;

        //9000-订单支付成功, 8000-正在处理中, 4000-订单支付失败, 6001-用户中途取消, 6002-网络连接出错
        if (resCode.equals("8000") || resCode.equals("9000")) {
            result = BCPayResult.RESULT_SUCCESS;
            errMsg = BCPayResult.RESULT_SUCCESS;
        } else if (resCode.equals("6001")) {
            result = BCPayResult.RESULT_CANCEL;
            errMsg = BCPayResult.RESULT_CANCEL;
        } else {
            result = BCPayResult.RESULT_FAIL;
            errMsg = BCPayResult.FAIL_ERR_FROM_CHANNEL;
        }

        payCallback.done(new BCPayResult(result, errMsg, aliResult));
    }

    /**
     * 与服务器交互后下一步进入银联app支付
     *
     * @param responseMap     服务端返回参数
     */
    private void reqUnionPaymentViaAPP(final Map<String, Object> responseMap) {

        String TN = (String) responseMap.get("tn");

        Intent intent = new Intent();
        intent.setClass(mContextActivity, BCUnionPaymentActivity.class);
        intent.putExtra("tn", TN);
        mContextActivity.startActivity(intent);
    }

    /**
     * 微信支付调用接口
     * 初始化billTitle,billTotalFee,billOutTradeNo后调用此接口发起微信支付，并跳转到微信。
     * 如果您申请的是新版本(V3)的微信支付，请使用此接口发起微信支付.
     * 您在BeeCloud控制台需要填写“微信Partner ID”、“微信Partner KEY”、“微信APP ID”.
     *
     * @param billTitle       商品描述, UTF8编码格式, 32个字节内
     * @param billTotalFee    支付金额，以分为单位，必须是整数格式
     * @param billNum         商户自定义订单号
     * @param optional        为扩展参数，可以传入任意数量的key/value对来补充对业务逻辑的需求
     * @param callback        支付完成后的回调函数
     */
    public void reqWXPaymentAsync(final String billTitle, final String billTotalFee,
                                  final String billNum,final Map<String, String> optional,
                                  final BCCallback callback) {
        this.reqPaymentAsync(BCReqParams.BCChannelTypes.WX_APP, billTitle, billTotalFee,
                billNum, optional, callback);
    }

    /**
     * 支付宝支付
     *
     * @param billTitle       商品描述, UTF8编码格式, 32个字节内
     * @param billTotalFee    支付金额，以分为单位，必须是整数格式
     * @param billNum         商户自定义订单号
     * @param optional        为扩展参数，可以传入任意数量的key/value对来补充对业务逻辑的需求
     * @param callback        支付完成后的回调函数
     */
    public void reqAliPaymentAsync(final String billTitle, final String billTotalFee,
                                   final String billNum,final Map<String, String> optional,
                                   final BCCallback callback) {
        this.reqPaymentAsync(BCReqParams.BCChannelTypes.ALI_APP, billTitle, billTotalFee,
                billNum, optional, callback);
    }

    /**
     * 银联在线支付，结果在onActivityResult中间获取
     *
     * @param billTitle       商品描述, UTF8编码格式, 32个字节内
     * @param billTotalFee    支付金额，以分为单位，必须是整数格式
     * @param billNum         商户自定义订单号
     * @param optional        为扩展参数，可以传入任意数量的key/value对来补充对业务逻辑的需求
     * @param callback        支付完成后的回调函数
     */
    public void reqUnionPaymentAsync(final String billTitle, final String billTotalFee,
                                          final String billNum,final Map<String, String> optional,
                                          final BCCallback callback) {
        this.reqPaymentAsync(BCReqParams.BCChannelTypes.UN_APP, billTitle, billTotalFee,
                billNum, optional, callback);
    }
}
