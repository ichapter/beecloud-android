/**
 * BCHttpClientUtil.java
 *
 * Created by xuanzhui on 2015/7/27.
 * Copyright (c) 2015 BeeCloud. All rights reserved.
 */
package cn.beecloud;

import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * 网络请求工具类
 */
class BCHttpClientUtil {

    //主机地址
    private static final String BEECLOUD_HOST = "https://apidynamic.beecloud.cn";
    private static final String TAG = "BCHttpClientUtil";

    //Rest API版本号
    private static final String HOST_API_VERSION = "/2/";

    //订单支付部分URL 和 获取扫码信息
    private static final String BILL_PAY_URL = "rest/bill";

    //测试模式对应的订单支付部分URL和获取扫码信息URL
    private static final String BILL_PAY_SANDBOX_URL = "rest/sandbox/bill";

    //测试模式的异步通知(通知服务端支付成功)
    private static final String NOTIFY_PAY_RESULT_SANDBOX_URL = "rest/sandbox/notify";

    //支付订单列表查询部分URL
    private static final String BILLS_QUERY_URL = "rest/bills?para=";

    //支付订单列表查询部分测试模式URL
    private static final String BILLS_QUERY_SANDBOX_URL = "rest/sandbox/bills?para=";

    //支付订单数目查询部分URL
    private static final String BILLS_COUNT_QUERY_URL = "rest/bills/count?para=";

    //支付订单数目查询部分测试模式URL
    private static final String BILLS_COUNT_QUERY_SANDBOX_URL = "rest/sandbox/bills/count?para=";

    //退款订单查询部分URL
    private static final String REFUND_QUERY_URL = "rest/refund";

    //退款订单列表查询部分URL
    private static final String REFUNDS_QUERY_URL = "rest/refunds?para=";

    //退款订单数目查询部分URL
    private static final String REFUNDS_COUNT_QUERY_URL = "rest/refunds/count?para=";

    //退款订单查询部分URL
    private static final String REFUND_STATUS_QUERY_URL = "rest/refund/status?para=";

    //线下支付
    private static final String BILL_OFFLINE_PAY_URL = "rest/offline/bill";

    //线下订单查询
    private static final String OFFLINE_BILL_STATUS_URL = "rest/offline/bill/status";

    private final static String PAYPAL_LIVE_BASE_URL = "https://api.paypal.com/v1/";
    private final static String PAYPAL_SANDBOX_BASE_URL = "https://api.sandbox.paypal.com/v1/";

    private final static String PAYPAL_ACCESS_TOKEN_URL= "oauth2/token";

    /**
     * 随机获取主机, 并加入API版本号
     */
    private static String getRandomHost() {
        return BEECLOUD_HOST + HOST_API_VERSION;
    }

    /**
     * @return  支付请求URL
     */
    public static String getBillPayURL() {
        if (BCCache.getInstance().isTestMode) {
            return getRandomHost() + BILL_PAY_SANDBOX_URL;
        } else {
            return getRandomHost() + BILL_PAY_URL;
        }
    }

    public static String getNotifyPayResultSandboxUrl() {
        return getRandomHost() + NOTIFY_PAY_RESULT_SANDBOX_URL
                + "/" + BCCache.getInstance().appId;
    }

    /**
     * @return  获取扫码信息URL
     */
    public static String getQRCodeReqURL() {
        return getRandomHost() + BILL_PAY_URL;
    }

    /**
     * @return  查询支付订单部分URL
     */
    public static String getBillQueryURL() {
        if (BCCache.getInstance().isTestMode) {
            return getRandomHost() + BILL_PAY_SANDBOX_URL;
        } else {
            return getRandomHost() + BILL_PAY_URL;
        }
    }

    /**
     * @return  查询支付订单列表URL
     */
    public static String getBillsQueryURL() {
        if (BCCache.getInstance().isTestMode) {
            return getRandomHost() + BILLS_QUERY_SANDBOX_URL;
        } else {
            return getRandomHost() + BILLS_QUERY_URL;
        }
    }

    /**
     * @return  查询支付订单数目URL
     */
    public static String getBillsCountQueryURL() {
        if (BCCache.getInstance().isTestMode) {
            return getRandomHost() + BILLS_COUNT_QUERY_SANDBOX_URL;
        } else {
            return getRandomHost() + BILLS_COUNT_QUERY_URL;
        }
    }

    /**
     * @return  查询退款订单部分URL
     */
    public static String getRefundQueryURL() {
        return getRandomHost() + REFUND_QUERY_URL;
    }

    /**
     * @return  查询退款订单列表URL
     */
    public static String getRefundsQueryURL() {
        return getRandomHost() + REFUNDS_QUERY_URL;
    }

    /**
     * @return  查询退款订单数目URL
     */
    public static String getRefundsCountQueryURL() {
        return getRandomHost() + REFUNDS_COUNT_QUERY_URL;
    }

    /**
     * @return  查询退款订单状态URL
     */
    public static String getRefundStatusURL() {
        return getRandomHost() + REFUND_STATUS_QUERY_URL;
    }

    public static String getPayPalAccessTokenUrl() {
        if (BCCache.getInstance().paypalPayType == BCPay.PAYPAL_PAY_TYPE.LIVE)
            return PAYPAL_LIVE_BASE_URL + PAYPAL_ACCESS_TOKEN_URL;
        else
            return PAYPAL_SANDBOX_BASE_URL + PAYPAL_ACCESS_TOKEN_URL;
    }

    /**
     * @return  线下支付
     */
    public static String getBillOfflinePayURL() {
        return getRandomHost() + BILL_OFFLINE_PAY_URL;
    }

    /**
     * @return  线下订单查询
     */
    public static String getOfflineBillStatusURL() {
        return getRandomHost() + OFFLINE_BILL_STATUS_URL;
    }

    /**
     * http get 请求
     * @param url   请求uri
     * @return      HttpResponse请求结果实例
     */
    public static Response httpGet(String url) {

        Response response = new Response();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(BCCache.getInstance().connectTimeout, TimeUnit.MILLISECONDS)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        proceedRequest(client, request, response);

        return response;
    }

    private static void proceedRequest(OkHttpClient client, Request request, Response response) {
        try {
            okhttp3.Response temp = client.newCall(request).execute();
            response.code = temp.code();
            ResponseBody body = temp.body();
            if (temp.isSuccessful()) {
                //call string auto close body
                response.content = body.string();
            } else {
                response.content = "网络请求失败";
                temp.body().close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, e.getMessage() == null ? " " : e.getMessage());
            response.code = -1;
            response.content = e.getMessage();
        }
    }

    /**
     * http post 请求
     * @param url       请求url
     * @param jsonStr    post参数
     * @return          HttpResponse请求结果实例
     */
    public static Response httpPost(String url, String jsonStr) {
        Response response = new Response();

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(BCCache.getInstance().connectTimeout, TimeUnit.MILLISECONDS)
                .build();

        RequestBody body = RequestBody.create(JSON, jsonStr);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        proceedRequest(client, request, response);

        return response;
    }

    /**
     * http post 请求
     * @param url       请求url
     * @param para      post参数
     * @return          HttpResponse请求结果实例
     */
    public static Response httpPost(String url, Map<String, Object> para) {
        Gson gson = new Gson();
        String param = gson.toJson(para);

        return httpPost(url, param);
    }

    public static Response getPayPalAccessToken() {
        Response response = new Response();

        try {
            //PayPal needs TLS v1.2
            OkHttpClient client =
                    new OkHttpClient.Builder()
                            .connectTimeout(BCCache.getInstance().connectTimeout, TimeUnit.MILLISECONDS)
                            .sslSocketFactory(new BCTLSSocketFactory()).build();

            FormBody.Builder form = new FormBody.Builder();
            form.add("grant_type", "client_credentials");
            RequestBody body = form.build();

            Request request = new Request.Builder()
                    .url(getPayPalAccessTokenUrl())
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Authorization", BCSecurityUtil.getB64Auth(
                            BCCache.getInstance().paypalClientID, BCCache.getInstance().paypalSecret))
                    .post(body)
                    .build();

            proceedRequest(client, request, response);
        } catch (KeyManagementException e) {
            e.printStackTrace();
            Log.w(TAG, e.getMessage() == null ? " " : e.getMessage());
            response.code = -1;
            response.content = e.getMessage();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Log.w(TAG, e.getMessage() == null ? " " : e.getMessage());
            response.code = -1;
            response.content = e.getMessage();
        }

        return response;
    }

    public static class Response {
        public Integer code;
        public String content;
    }

}
