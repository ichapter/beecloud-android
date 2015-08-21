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
import cn.beecloud.entity.BCQueryBillOrderResult;
import cn.beecloud.entity.BCQueryRefundOrderResult;
import cn.beecloud.entity.BCQueryRefundStatusResult;
import cn.beecloud.entity.BCQueryReqParams;
import cn.beecloud.entity.BCReqParams;
import cn.beecloud.entity.BCRestfulCommonResult;

/**
 * 订单查询接口
 * 单例模式
 */
public class BCQuery {
    private static final String TAG = "BCQuery";

    private static BCQuery instance;
    private BCQuery() {
    }

    //查询订单类型-支付订单, 退款订单
    public enum QueryOrderType{QUERY_BILLS, QUERY_REFUNDS}

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

    private void doErrCallBack(final QueryOrderType operation, final Integer errCode, final String errMsg,
                                final String errDetail, final BCCallback callback){
        switch (operation){
            case QUERY_BILLS:
                callback.done(new BCQueryBillOrderResult(errCode, errMsg, errDetail));
            case QUERY_REFUNDS:
                callback.done(new BCQueryRefundOrderResult(errCode, errMsg, errDetail));
        }
    }

    /**
     * 查询订单主入口
     * @param channel       支付渠道类型
     * @param operation     发起的操作类型
     * @param billNum       发起支付时填写的订单号, 可为null
     * @param refundNum     退款的单号, 可为null
     * @param startTime     订单生成时间, 毫秒时间戳, 13位, 可为null
     * @param endTime       订单完成时间, 毫秒时间戳, 13位, 可为null
     * @param skip          忽略的记录个数, 默认为0, 设置为10表示忽略满足条件的前10条数据, 可为null
     * @param limit         本次抓取的记录数, 默认为10, [10, 50]之间, 设置为10表示只返回满足条件的10条数据, 可为null
     * @param callback      回调函数
     */
    protected void queryOrdersAsync(final BCReqParams.BCChannelTypes channel,
                                    final QueryOrderType operation,
                                    final String billNum, final String refundNum,
                                    final Long startTime, final Long endTime,
                                    final Integer skip, final Integer limit, final BCCallback callback) {
        if (callback == null) {
            Log.w(TAG, "请初始化callback");
            return;
        }

        BCCache.executorService.execute(new Runnable() {
             @Override
             public void run() {
                 BCQueryReqParams bcQueryReqParams;
                 try {
                     bcQueryReqParams = new BCQueryReqParams(channel);
                 } catch (BCException e) {
                     doErrCallBack(operation, BCRestfulCommonResult.APP_INNER_FAIL_NUM,
                             BCRestfulCommonResult.APP_INNER_FAIL, e.getMessage(), callback);
                     return;
                 }

                 //common
                 bcQueryReqParams.billNum = billNum;
                 bcQueryReqParams.startTime = startTime;
                 bcQueryReqParams.endTime = endTime;
                 bcQueryReqParams.skip = skip;
                 bcQueryReqParams.limit = limit;

                 String queryURL = BCHttpClientUtil.getBillQueryURL();

                 if (operation == QueryOrderType.QUERY_REFUNDS){
                     bcQueryReqParams.refundNum = refundNum;
                     queryURL = BCHttpClientUtil.getRefundQueryURL();
                 }

                 //Log.w("BCQuery",queryURL + bcQueryReqParams.transToEncodedJsonString());

                 HttpResponse response = BCHttpClientUtil.httpGet(queryURL +
                    bcQueryReqParams.transToEncodedJsonString());

                 if (null == response) {
                     doErrCallBack(operation, BCRestfulCommonResult.APP_INNER_FAIL_NUM,
                             BCRestfulCommonResult.APP_INNER_FAIL,
                             "Network Error", callback);
                     return;
                 }
                 if (response.getStatusLine().getStatusCode() == 200) {

                     try {
                         String ret = EntityUtils.toString(response.getEntity(), "UTF-8");

                         switch (operation){
                             case QUERY_BILLS:
                                 callback.done(new BCQueryBillOrderResult().transJsonToResultObject(ret));
                                 break;
                             case QUERY_REFUNDS:
                                 callback.done(new BCQueryRefundOrderResult().transJsonToResultObject(ret));
                                 break;
                             default:
                                 doErrCallBack(operation, BCRestfulCommonResult.APP_INNER_FAIL_NUM,
                                         BCRestfulCommonResult.APP_INNER_FAIL, "Invalid channel", callback);
                         }

                     } catch (IOException e) {
                         doErrCallBack(operation, BCRestfulCommonResult.APP_INNER_FAIL_NUM, BCRestfulCommonResult.APP_INNER_FAIL,
                                 "Invalid Response", callback);
                     }
                 } else {
                     doErrCallBack(operation, BCRestfulCommonResult.APP_INNER_FAIL_NUM, BCRestfulCommonResult.APP_INNER_FAIL,
                             "Network Error",callback);
                 }
             }
         });

    }

    /**
     * 查询支付订单主入口
     * @param channel       支付渠道类型, 若为ALL则查询全部订单
     * @param billNum       发起支付时填写的订单号, 可为null
     * @param startTime     支付订单生成时间, 毫秒时间戳, 13位, 可为null
     * @param endTime       支付订单完成时间, 毫秒时间戳, 13位, 可为null
     * @param skip          忽略的记录个数, 默认为0, 设置为10表示忽略满足条件的前10条数据, 可为null
     * @param limit         本次抓取的记录数, 默认为10, [10, 50]之间, 设置为10表示只返回满足条件的10条数据, 可为null
     * @param callback      回调函数
     */
    public void queryBillsAsync(final BCReqParams.BCChannelTypes channel, final String billNum,
                                final Long startTime, final Long endTime,
                                final Integer skip, final Integer limit, final BCCallback callback){
        queryOrdersAsync(channel, QueryOrderType.QUERY_BILLS, billNum, null, startTime, endTime, skip, limit, callback);
    }

    /**
     * 根据支付渠道获取订单
     * @param channel       支付渠道, 若为ALL则查询全部订单
     * @param callback      回调接口
     */
    public void queryBillsAsync(final BCReqParams.BCChannelTypes channel, final BCCallback callback){
        queryBillsAsync(channel, null, null, null, null, null, callback);
    }

    /**
     * 根据支付渠道和订单号获取订单
     * @param channel       支付渠道, 若为ALL则查询全部订单
     * @param billNum       订单号
     * @param callback      回调接口
     */
    public void queryBillsAsync(final BCReqParams.BCChannelTypes channel, final String billNum, final BCCallback callback){
        queryBillsAsync(channel, billNum, null, null, null, null, callback);
    }

    /**
     * 查询退款订单主入口
     * @param channel       支付渠道类型, 若为ALL则查询全部订单
     * @param billNum       发起支付时填写的订单号, 可为null
     * @param refundNum     发起退款时填写的订单号, 可为null
     * @param startTime     退款订单生成时间, 毫秒时间戳, 13位, 可为null
     * @param endTime       退款订单完成时间, 毫秒时间戳, 13位, 可为null
     * @param skip          忽略的记录个数, 默认为0, 设置为10表示忽略满足条件的前10条数据, 可为null
     * @param limit         本次抓取的记录数, 默认为10, [10, 50]之间, 设置为10表示只返回满足条件的10条数据, 可为null
     * @param callback      回调函数
     */
    public void queryRefundsAsync(final BCReqParams.BCChannelTypes channel, final String billNum, final String refundNum,
                                final Long startTime, final Long endTime,
                                final Integer skip, final Integer limit, final BCCallback callback){
        queryOrdersAsync(channel, QueryOrderType.QUERY_REFUNDS, billNum, refundNum, startTime, endTime, skip, limit, callback);
    }

    /**
     * 根据支付渠道获取退款订单列表
     * @param channel       支付渠道, 若为ALL则查询全部订单
     * @param callback      回调接口
     */
    public void queryRefundsAsync(final BCReqParams.BCChannelTypes channel, final BCCallback callback){
        queryRefundsAsync(channel, null, null, null, null, null, null, callback);
    }

    /**
     * 根据支付渠道和(支付订单号|退款单号)获取退款订单列表
     * @param channel       支付渠道, 若为ALL则查询全部订单
     * @param billNum       支付订单号, 可为null
     * @param refundNum     退款单号, 可为null
     * @param callback      回调接口
     */
    public void queryRefundsAsync(final BCReqParams.BCChannelTypes channel, final String billNum,
                                  final String refundNum, final BCCallback callback){
        queryRefundsAsync(channel, billNum, refundNum, null, null, null, null, callback);
    }

    /**
     * 获取退款状态信息
     * @param channel       支付的渠道, 目前只支持微信WX
     * @param refundNum     退款单号
     * @param callback      回调入口
     */
    public void queryRefundStatusAsync(final BCReqParams.BCChannelTypes channel, final String refundNum,
                                       final BCCallback callback){
        if (callback == null) {
            Log.w(TAG, "请初始化callback");
            return;
        }

        BCCache.executorService.execute(new Runnable() {
            @Override
            public void run() {
                if (refundNum == null) {
                    callback.done(new BCQueryRefundStatusResult(BCRestfulCommonResult.APP_INNER_FAIL_NUM,
                            BCRestfulCommonResult.APP_INNER_FAIL, "refundNum不能为null", null));
                    return;
                }

                if (!channel.equals(BCReqParams.BCChannelTypes.WX)) {
                    callback.done(new BCQueryRefundStatusResult(BCRestfulCommonResult.APP_INNER_FAIL_NUM,
                            BCRestfulCommonResult.APP_INNER_FAIL, "目前只支持微信退款状态查询", null));
                    return;
                }

                BCQueryReqParams bcQueryReqParams;
                try {
                    bcQueryReqParams = new BCQueryReqParams(channel);
                } catch (BCException e) {
                    callback.done(new BCQueryRefundStatusResult(BCRestfulCommonResult.APP_INNER_FAIL_NUM,
                            BCRestfulCommonResult.APP_INNER_FAIL, e.getMessage(), null));
                    return;
                }

                bcQueryReqParams.refundNum = refundNum;

                String queryURL = BCHttpClientUtil.getRefundStatusURL();

                //Log.w("BCQuery", queryURL + bcQueryReqParams.transToEncodedJsonString());

                HttpResponse response = BCHttpClientUtil.httpGet(queryURL +
                        bcQueryReqParams.transToEncodedJsonString());

                if (null == response) {
                    callback.done(new BCQueryRefundStatusResult(BCRestfulCommonResult.APP_INNER_FAIL_NUM, BCRestfulCommonResult.APP_INNER_FAIL,
                            "Network Error", null));
                    return;
                }
                if (response.getStatusLine().getStatusCode() == 200) {

                    try {
                        String ret = EntityUtils.toString(response.getEntity(), "UTF-8");

                        callback.done(new BCQueryRefundStatusResult().transJsonToResultObject(ret));

                    } catch (IOException e) {
                        callback.done(new BCQueryRefundStatusResult(BCRestfulCommonResult.APP_INNER_FAIL_NUM, BCRestfulCommonResult.APP_INNER_FAIL,
                                "Invalid Response", null));
                    }
                } else {
                    callback.done(new BCQueryRefundStatusResult(BCRestfulCommonResult.APP_INNER_FAIL_NUM, BCRestfulCommonResult.APP_INNER_FAIL,
                            "Network Error", null));
                }
            }
        });
    }
}
