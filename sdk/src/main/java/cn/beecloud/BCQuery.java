/**
 * BCQuery.java
 *
 * Created by xuanzhui on 2015/7/29.
 * Copyright (c) 2015 BeeCloud. All rights reserved.
 */
package cn.beecloud;

import android.util.Log;

import java.util.Map;

import cn.beecloud.async.BCCallback;
import cn.beecloud.entity.BCBillStatus;
import cn.beecloud.entity.BCQueryBillResult;
import cn.beecloud.entity.BCQueryBillsResult;
import cn.beecloud.entity.BCQueryRefundResult;
import cn.beecloud.entity.BCQueryRefundsResult;
import cn.beecloud.entity.BCQueryReqParams;
import cn.beecloud.entity.BCRefundStatus;
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
                callback.done(new BCQueryBillsResult(errCode, errMsg, errDetail));
                break;
            case QUERY_REFUNDS:
                callback.done(new BCQueryRefundsResult(errCode, errMsg, errDetail));
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

                 if (channel == null) {
                     doErrCallBack(operation, BCRestfulCommonResult.APP_INNER_FAIL_NUM,
                             BCRestfulCommonResult.APP_INNER_FAIL,
                             "channel NPE, set ALL if you want to query all records",
                             callback);

                     return;
                 }

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

                 String queryURL = BCHttpClientUtil.getBillsQueryURL();

                 if (operation == QueryOrderType.QUERY_REFUNDS){
                     bcQueryReqParams.refundNum = refundNum;
                     queryURL = BCHttpClientUtil.getRefundsQueryURL();
                 }

                 //Log.w("BCQuery",queryURL + bcQueryReqParams.transToEncodedJsonString());

                 BCHttpClientUtil.Response response = BCHttpClientUtil.httpGet(queryURL +
                         bcQueryReqParams.transToEncodedJsonString());

                 if (response.code == 200) {


                     String ret = response.content;

                     switch (operation){
                         case QUERY_BILLS:
                             callback.done(BCQueryBillsResult.transJsonToResultObject(ret));
                             break;
                         case QUERY_REFUNDS:
                             callback.done(BCQueryRefundsResult.transJsonToResultObject(ret));
                             break;
                         default:
                             doErrCallBack(operation, BCRestfulCommonResult.APP_INNER_FAIL_NUM,
                                     BCRestfulCommonResult.APP_INNER_FAIL, "Invalid channel", callback);
                     }

                 } else {
                     doErrCallBack(operation, BCRestfulCommonResult.APP_INNER_FAIL_NUM,
                             BCRestfulCommonResult.APP_INNER_FAIL,
                             "Network Error:" + response.code + " # " + response.content,callback);
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
     * 查询支付订单
     * @param queryParams    @see cn.beecloud.BCQuery.QueryParams 查询参数
     * @param callback       回调接口
     */
    public void queryBillsAsync(final QueryParams queryParams, final BCCallback callback) {
        queryBillsAsync(queryParams.channel,
                queryParams.billNum,
                queryParams.startTime,
                queryParams.endTime,
                queryParams.skip,
                queryParams.limit,
                callback);
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
     * 查询退款订单列表
     * @param queryParams   @see cn.beecloud.BCQuery.QueryParams 查询参数
     * @param callback      回调接口
     */
    public void queryRefundsAsync(final QueryParams queryParams, final BCCallback callback) {
        queryRefundsAsync(queryParams.channel,
                queryParams.billNum,
                queryParams.refundNum,
                queryParams.startTime,
                queryParams.endTime,
                queryParams.skip,
                queryParams.limit,
                callback);
    }

    /**
     * 获取退款状态信息
     * @param channel       支付的渠道, 目前支持微信WX、YEE、KUAIQIAN、BD
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
                    callback.done(new BCRefundStatus(BCRestfulCommonResult.APP_INNER_FAIL_NUM,
                            BCRestfulCommonResult.APP_INNER_FAIL, "refundNum不能为null", null));
                    return;
                }

                if (!(channel.equals(BCReqParams.BCChannelTypes.WX) ||
                        channel.equals(BCReqParams.BCChannelTypes.YEE) ||
                        channel.equals(BCReqParams.BCChannelTypes.KUAIQIAN) ||
                        channel.equals(BCReqParams.BCChannelTypes.BD))) {
                    callback.done(new BCRefundStatus(BCRestfulCommonResult.APP_INNER_FAIL_NUM,
                            BCRestfulCommonResult.APP_INNER_FAIL, "退款状态查询的channel参数只能是WX、YEE、KUAIQIAN或BD", null));
                    return;
                }

                BCQueryReqParams bcQueryReqParams;
                try {
                    bcQueryReqParams = new BCQueryReqParams(channel);
                } catch (BCException e) {
                    callback.done(new BCRefundStatus(BCRestfulCommonResult.APP_INNER_FAIL_NUM,
                            BCRestfulCommonResult.APP_INNER_FAIL, e.getMessage(), null));
                    return;
                }

                bcQueryReqParams.refundNum = refundNum;

                String queryURL = BCHttpClientUtil.getRefundStatusURL();

                //Log.w("BCQuery", queryURL + bcQueryReqParams.transToEncodedJsonString());

                BCHttpClientUtil.Response response = BCHttpClientUtil.httpGet(queryURL +
                        bcQueryReqParams.transToEncodedJsonString());

                if (response.code == 200) {

                    String ret = response.content;
                    callback.done(BCRefundStatus.transJsonToObject(ret));

                } else {
                    callback.done(new BCRefundStatus(
                            BCRestfulCommonResult.APP_INNER_FAIL_NUM,
                            BCRestfulCommonResult.APP_INNER_FAIL,
                            "Network Error:" + response.code + " # " + response.content, null));
                }
            }
        });
    }

    /**
     * 根据ID查询支付订单
     * @param id        支付订单唯一标识，注意此处不是订单号
     * @param callback  回调入口
     */
    public void queryBillByIDAsync(final String id, final BCCallback callback) {
        if (callback == null) {
            Log.w(TAG, "请初始化callback");
            return;
        }

        BCCache.executorService.execute(new Runnable() {
            @Override
            public void run() {

                if (id == null) {
                    callback.done(new BCQueryBillResult(BCRestfulCommonResult.APP_INNER_FAIL_NUM,
                            BCRestfulCommonResult.APP_INNER_FAIL, "id NPE!"));

                    return;
                }

                BCQueryReqParams bcQueryReqParams;
                try {
                    bcQueryReqParams = new BCQueryReqParams(BCReqParams.BCChannelTypes.ALL);
                } catch (BCException e) {
                    callback.done(new BCQueryBillResult(BCRestfulCommonResult.APP_INNER_FAIL_NUM,
                            BCRestfulCommonResult.APP_INNER_FAIL, e.getMessage()));
                    return;
                }

                String queryURL = BCHttpClientUtil.getBillQueryURL() + "/"
                        + id + "?para=";

                //Log.w("BCQuery", queryURL + bcQueryReqParams.transToEncodedJsonString());

                BCHttpClientUtil.Response response = BCHttpClientUtil.httpGet(queryURL +
                        bcQueryReqParams.transToEncodedJsonString());

                if (response.code == 200) {
                    callback.done(BCQueryBillResult.transJsonToResultObject(response.content));
                } else {
                    callback.done(new BCQueryBillResult(BCRestfulCommonResult.APP_INNER_FAIL_NUM,
                            BCRestfulCommonResult.APP_INNER_FAIL,
                            "Network Error:" + response.code + " # " + response.content));
                }
            }
        });
    }

    /**
     * 根据ID查询退款订单
     * @param id        退款订单唯一标识，注意此处不是退款单号
     * @param callback  回调入口
     */
    public void queryRefundByIDAsync(final String id, final BCCallback callback) {
        if (callback == null) {
            Log.w(TAG, "请初始化callback");
            return;
        }

        BCCache.executorService.execute(new Runnable() {
            @Override
            public void run() {
                if (id == null) {
                    callback.done(new BCQueryRefundResult(BCRestfulCommonResult.APP_INNER_FAIL_NUM,
                            BCRestfulCommonResult.APP_INNER_FAIL, "id NPE!"));

                    return;
                }

                BCQueryReqParams bcQueryReqParams;
                try {
                    bcQueryReqParams = new BCQueryReqParams(BCReqParams.BCChannelTypes.ALL);
                } catch (BCException e) {
                    callback.done(new BCQueryRefundResult(BCRestfulCommonResult.APP_INNER_FAIL_NUM,
                            BCRestfulCommonResult.APP_INNER_FAIL, e.getMessage()));
                    return;
                }

                String queryURL = BCHttpClientUtil.getRefundQueryURL() + "/"
                        + id + "?para=";

                //Log.w("BCQuery", queryURL + bcQueryReqParams.transToEncodedJsonString());

                BCHttpClientUtil.Response response = BCHttpClientUtil.httpGet(queryURL +
                        bcQueryReqParams.transToEncodedJsonString());

                if (response.code == 200) {
                    callback.done(BCQueryRefundResult.transJsonToResultObject(response.content));
                } else {
                    callback.done(new BCQueryRefundResult(BCRestfulCommonResult.APP_INNER_FAIL_NUM,
                            BCRestfulCommonResult.APP_INNER_FAIL,
                            "Network Error:" + response.code + " # " + response.content));
                }
            }
        });
    }

    /**
     * 查询线下订单状态
     *
     * @param channelType     目前只支持WX_NATIVE, WX_SCAN, ALI_OFFLINE_QRCODE, ALI_SCAN
     *                        @see cn.beecloud.entity.BCReqParams.BCChannelTypes
     * @param billNum         订单号
     * @param callback        支付完成后的回调函数
     */
    public void queryOfflineBillStatusAsync(final BCReqParams.BCChannelTypes channelType,
                                          final String billNum,
                                          final BCCallback callback) {

        if (callback == null) {
            Log.w(TAG, "请初始化callback");
            return;
        }

        BCCache.executorService.execute(new Runnable() {
            @Override
            public void run() {

                //校验并准备公用参数
                BCReqParams parameters;
                try {
                    parameters = new BCReqParams(channelType, BCReqParams.ReqType.OFFLINE_PAY);
                } catch (BCException e) {
                    callback.done(new BCBillStatus(BCRestfulCommonResult.APP_INNER_FAIL_NUM,
                            BCRestfulCommonResult.APP_INNER_FAIL,
                            e.getMessage()));
                    return;
                }

                if (billNum == null ||
                        billNum.length() == 0){
                    callback.done(new BCBillStatus(BCRestfulCommonResult.APP_INNER_FAIL_NUM,
                            BCRestfulCommonResult.APP_INNER_FAIL,
                            "invalid bill number"));
                    return;
                }

                String queryURL = BCHttpClientUtil.getOfflineBillStatusURL();

                Map<String, Object> reqMap = parameters.transToReqMapParams();
                reqMap.put("bill_no", billNum);

                BCHttpClientUtil.Response response = BCHttpClientUtil.httpPost(queryURL, reqMap);

                if (response.code == 200) {

                    //返回后台结果
                    callback.done(BCBillStatus.transJsonToObject(response.content));

                } else {
                    callback.done(new BCBillStatus(BCRestfulCommonResult.APP_INNER_FAIL_NUM,
                            BCRestfulCommonResult.APP_INNER_FAIL,
                            "Network Error:" + response.code + " # " + response.content));
                }

            }
        });
    }

    /**
     * 查询外部参数类
     */
    public static class QueryParams {
        /**
         * 支付渠道类型
         */
        public BCReqParams.BCChannelTypes channel;

        /**
         * 发起支付时填写的订单号, 可为null
         */
        public String billNum;

        /**
         * 退款的单号, 查询退款订单的时候填写, 可为null
         */
        public String refundNum;

        /**
         * 查询起始时间, 毫秒时间戳, 13位, 可为null
         */
        public Long startTime;

        /**
         * 结束时间, 毫秒时间戳, 13位, 可为null
         */
        public Long endTime;

        /**
         * 跳过的记录个数, 默认为0, 设置为10表示跳过满足条件的前10条数据, 可为null
         */
        public Integer skip;

        /**
         * 本次抓取的记录数, 默认为10, [10, 50]之间, 设置为10表示只返回满足条件的10条数据, 可为null
         */
        public Integer limit;
    }
}
