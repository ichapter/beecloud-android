/**
 * BCHttpClientUtil.java
 *
 * Created by xuanzhui on 2015/7/27.
 * Copyright (c) 2015 BeeCloud. All rights reserved.
 */
package cn.beecloud;

import com.google.gson.Gson;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

/**
 * 网络请求工具类
 */
class BCHttpClientUtil {

    //主机地址
    private static final String[] BEECLOUD_HOSTS = {"https://apibj.beecloud.cn",
            "https://apisz.beecloud.cn",
            "https://apiqd.beecloud.cn",
            "https://apihz.beecloud.cn"
    };

    //Rest API版本号
    private static final String HOST_API_VERSION = "/2/";

    //订单支付部分URL 和 获取扫码信息
    private static final String BILL_PAY_URL = "rest/bill";

    //支付订单列表查询部分URL
    private static final String BILLS_QUERY_URL = "rest/bills?para=";

    //支付订单数目查询部分URL
    private static final String BILLS_COUNT_QUERY_URL = "rest/bills/count?para=";

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
        Random r = new Random();
        return BEECLOUD_HOSTS[r.nextInt(BEECLOUD_HOSTS.length)] + HOST_API_VERSION;
    }

    /**
     * @return  支付请求URL
     */
    public static String getBillPayURL() {
        return getRandomHost() + BILL_PAY_URL;
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
        return getRandomHost() + BILL_PAY_URL;
    }

    /**
     * @return  查询支付订单列表URL
     */
    public static String getBillsQueryURL() {
        return getRandomHost() + BILLS_QUERY_URL;
    }

    /**
     * @return  查询支付订单数目URL
     */
    public static String getBillsCountQueryURL() {
        return getRandomHost() + BILLS_COUNT_QUERY_URL;
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

        Response response = null;

        HttpsURLConnection httpsURLConnection = null;
        try {
            URL urlObj = new URL(url);
            httpsURLConnection = (HttpsURLConnection)urlObj.openConnection();
            httpsURLConnection.setConnectTimeout(BCCache.getInstance().connectTimeout);
            httpsURLConnection.setDoInput(true);

            response = readStream(httpsURLConnection);

        } catch (MalformedURLException e) {
            e.printStackTrace();

            response = new Response();
            response.content = e.getMessage();
            response.code = -1;
        } catch (IOException e) {
            e.printStackTrace();

            response = new Response();
            response.content = e.getMessage();
            response.code = -1;
        } catch (Exception ex) {
            ex.printStackTrace();
            response = new Response();
            response.content = ex.getMessage();
            response.code = -1;
        } finally {
            if (httpsURLConnection != null)
                httpsURLConnection.disconnect();
        }

        return response;
    }

    static Response readStream(HttpsURLConnection connection) {
        Response response = new Response();

        StringBuilder stringBuilder = new StringBuilder();

        BufferedReader reader = null;
        try {

            reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), "UTF-8"));

            int tmp;
            while ((tmp = reader.read()) != -1) {
                stringBuilder.append((char)tmp);
            }

            response.code = connection.getResponseCode();
            response.content = stringBuilder.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            response.code = -1;
            response.content = e.getMessage();
        } catch (IOException e) {
            e.printStackTrace();

            try {
                //it could be caused by 400 and so on

                reader = new BufferedReader(new InputStreamReader(
                        connection.getErrorStream(), "UTF-8"));

                //clear
                stringBuilder.setLength(0);

                int tmp;
                while ((tmp = reader.read()) != -1) {
                    stringBuilder.append((char)tmp);
                }

                response.code = connection.getResponseCode();
                response.content = stringBuilder.toString();

            } catch (IOException e1) {
                response.content = e1.getMessage();
                response.code = -1;
                e1.printStackTrace();
            } catch (Exception ex) {
                //if user directly shuts down network when trying to write to server
                //there could be NullPointerException or SSLException
                response.content = ex.getMessage();
                response.code = -1;
                ex.printStackTrace();
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return response;
    }

    //return null means successfully write to server
    static Response writeStream(HttpsURLConnection connection, String content) {
        BufferedOutputStream out=null;
        Response response = null;
        try {
            out = new BufferedOutputStream(connection.getOutputStream());
            out.write(content.getBytes("UTF-8"));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();

            try {
                //it could be caused by 400 and so on
                response = new Response();

                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        connection.getErrorStream(), "UTF-8"));

                StringBuilder stringBuilder = new StringBuilder();

                int tmp;
                while ((tmp = reader.read()) != -1) {
                    stringBuilder.append((char)tmp);
                }

                response.code = connection.getResponseCode();
                response.content = stringBuilder.toString();

            } catch (IOException e1) {
                response = new Response();
                response.content = e1.getMessage();
                response.code = -1;
                e1.printStackTrace();
            } catch (Exception ex) {
                //if user directly shutdowns network when trying to write to server
                //there could be NullPointerException or SSLException
                response = new Response();
                response.content = ex.getMessage();
                response.code = -1;
                ex.printStackTrace();
            }
        } finally {
            try {
                if (out!=null)
                    out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return response;
    }

    /**
     * http post 请求
     * @param url       请求url
     * @param jsonStr    post参数
     * @return          HttpResponse请求结果实例
     */
    public static Response httpPost(String url, String jsonStr) {
        Response response = null;

        HttpsURLConnection httpsURLConnection = null;
        try {
            URL urlObj = new URL(url);
            httpsURLConnection = (HttpsURLConnection)urlObj.openConnection();
            httpsURLConnection.setRequestMethod("POST");
            httpsURLConnection.setConnectTimeout(BCCache.getInstance().connectTimeout);
            httpsURLConnection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            httpsURLConnection.setDoOutput(true);
            httpsURLConnection.setChunkedStreamingMode(0);

            //start to post
            response = writeStream(httpsURLConnection, jsonStr);

            if (response == null) { //if post successfully

                response = readStream(httpsURLConnection);

            }
        } catch (MalformedURLException e) {
            e.printStackTrace();

            response = new Response();
            response.content = e.getMessage();
            response.code = -1;
        } catch (IOException e) {
            e.printStackTrace();

            response = new Response();
            response.content = e.getMessage();
            response.code = -1;
        } catch (Exception ex) {
            ex.printStackTrace();
            response = new Response();
            response.content = ex.getMessage();
            response.code = -1;
        } finally {
            if (httpsURLConnection != null)
                httpsURLConnection.disconnect();
        }

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

        Response response = null;

        HttpsURLConnection httpsURLConnection = null;
        try {
            URL urlObj = new URL(getPayPalAccessTokenUrl());
            httpsURLConnection = (HttpsURLConnection)urlObj.openConnection();
            httpsURLConnection.setConnectTimeout(BCCache.getInstance().connectTimeout);
            httpsURLConnection.setRequestMethod("POST");
            httpsURLConnection.setRequestProperty("Accept", "application/json");
            httpsURLConnection.setRequestProperty("Authorization", BCSecurityUtil.getB64Auth(
                    BCCache.getInstance().paypalClientID, BCCache.getInstance().paypalSecret));
            httpsURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpsURLConnection.setDoOutput(true);
            httpsURLConnection.setChunkedStreamingMode(0);

            response = writeStream(httpsURLConnection, "grant_type=client_credentials");
            if (response == null) {

                response = readStream(httpsURLConnection);

            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            response = new Response();
            response.content = e.getMessage();
            response.code = -1;
        } catch (IOException e) {
            e.printStackTrace();
            response = new Response();
            response.content = e.getMessage();
            response.code = -1;
        } finally {
            if (httpsURLConnection != null)
                httpsURLConnection.disconnect();
        }

        return response;
    }

    public static class Response {
        public Integer code;
        public String content;
    }

}
