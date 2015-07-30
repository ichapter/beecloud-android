/**
 * BCQuery.java
 *
 * Created by xuanzhui on 2015/7/29.
 * Copyright (c) 2015 BeeCloud. All rights reserved.
 */
package cn.beecloud;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

import cn.beecloud.async.BCCallback;
import cn.beecloud.entity.BCQueryReqParams;
import cn.beecloud.entity.BCQueryResult;

/**
 * 订单查询接口
 * 单例模式
 */
public class BCQuery {
    private static final String TAG = "BCQuery";

    private static BCQuery instance;
    private BCQuery() {}

    /**
     * 唯一获取BCQuery实例的入口
     * @return  BCQuery实例
     */
    public synchronized static BCQuery getInstance() {
        if (instance == null) {
            instance = new BCQuery();
        }
        return instance;
    }

    /**
     * 查询订单主入口
     * @param channel       支付渠道类型
     * @param billNum       发起支付时填写的订单号, 可为null
     * @param startTime     订单生成时间, 毫秒时间戳, 13位, 可为null
     * @param endTime       订单完成时间, 毫秒时间戳, 13位, 可为null
     * @param skip          忽略的记录个数, 默认为0, 设置为10表示忽略满足条件的前10条数据, 可为null
     * @param limit         本次抓取的记录数, 默认为10, [10, 50]之间, 设置为10表示只返回满足条件的10条数据, 可为null
     * @param callback      回调函数
     */
    public void queryBillsAsync(final String channel, final String billNum,
                                    final Long startTime, final Long endTime,
                                    final Integer skip, final Integer limit, final BCCallback callback) {
        if (callback == null) {
            Log.w(TAG, "请初始化callback");
            return;
        }

        BCCache.executorService.execute(new Runnable() {
             @Override
             public void run() {
                 BCQueryReqParams bcQueryReqParams = null;
                 try {
                     bcQueryReqParams = new BCQueryReqParams(channel);
                 } catch (BCException e) {
                     callback.done(new BCQueryResult(BCQueryResult.APP_INNER_FAIL_NUM,
                             BCQueryResult.APP_INNER_FAIL, e.getMessage(), 0, null));
                 }

                 bcQueryReqParams.billNum = billNum;
                 bcQueryReqParams.startTime = startTime;
                 bcQueryReqParams.endTime = endTime;
                 bcQueryReqParams.skip = skip;
                 bcQueryReqParams.limit = limit;

                 String queryURL = BCHttpClientUtil.getBillQueryURL();
                 HttpResponse response = BCHttpClientUtil.httpGet(queryURL +
                    bcQueryReqParams.transToEncodedJsonString());

                 if (null == response) {
                     callback.done(new BCQueryResult(BCQueryResult.APP_INNER_FAIL_NUM, BCQueryResult.APP_INNER_FAIL,
                             "Network Error",0, null));
                     return;
                 }
                 if (response.getStatusLine().getStatusCode() == 200) {

                     try {
                         String ret = EntityUtils.toString(response.getEntity(), "UTF-8");

                        callback.done(BCQueryResult.transJsonToResultObject(ret));

                     } catch (IOException e) {
                         callback.done(new BCQueryResult(BCQueryResult.APP_INNER_FAIL_NUM, BCQueryResult.APP_INNER_FAIL,
                                 "Invalid Response",0, null));
                     }
                 } else {
                     callback.done(new BCQueryResult(BCQueryResult.APP_INNER_FAIL_NUM, BCQueryResult.APP_INNER_FAIL,
                             "Network Error",0, null));
                 }
             }
         });

    }

    /**
     * 根据支付渠道获取订单
     * @param channel       支付渠道
     * @param callback      回调接口
     */
    public void queryBillsAsync(final String channel, final BCCallback callback){
        queryBillsAsync(channel, null, null, null, null, null, callback);
    }

    /**
     * 根据支付渠道和订单号获取订单
     * @param channel       支付渠道
     * @param billNum       订单号
     * @param callback      回调接口
     */
    public void queryBillsAsync(final String channel, final String billNum, final BCCallback callback){
        queryBillsAsync(channel, billNum, null, null, null, null, callback);
    }
}
