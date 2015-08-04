/**
 * BCRefundOrder.java
 * <p/>
 * Created by xuanzhui on 2015/8/3.
 * Copyright (c) 2015 BeeCloud. All rights reserved.
 */
package cn.beecloud.entity;

import java.util.Map;

/**
 * 退款订单信息
 * @see cn.beecloud.entity.BCBill
 */
public class BCRefundOrder extends BCBill {
    //退款号
    private String refundNum;

    //退款金额, 单位为分
    private Integer refundFee;

    //退款受理是否完成--true表示渠道退款请求已经受理完成，但是并不代表接受退款
    private Boolean refundFinish;

    //是否接受并且退款成功--如果为true表示渠道接受退款请求并且已经完成退款，意味着refundFinish肯定为true
    private Boolean refundResult;

    //退款创建时间, 毫秒时间戳, 13位
    private Long refundCreatedTime;

    /**
     * getPayResult函数是专属于支付订单的, 在退款的类中请求抛出UnsupportedOperationException
     */
    @Override
    public Boolean getPayResult(){
        throw new UnsupportedOperationException("getPayResult method is not supported in BCRefundOrder");
    }

    /**
     * getCreatedTime函数是专属于支付订单的, 在退款的类中请求抛出UnsupportedOperationException
     */
    @Override
    public Long getCreatedTime() {
        throw new UnsupportedOperationException("getCreatedTime method is not supported in BCRefundOrder");
    }

    /**
     * @return  退款号
     */
    public String getRefundNum() {
        return refundNum;
    }

    /**
     * @return  退款金额, 单位为分
     */
    public Integer getRefundFee() {
        return refundFee;
    }

    /**
     * 退款受理是否完成
     * @return  true表示渠道退款请求已经受理完成，但是并不代表接受退款
     */
    public Boolean getRefundFinish() {
        return refundFinish;
    }

    /**
     *  是否接受并且退款成功
     * @return  如果为true表示渠道接受退款请求并且已经完成退款，意味着getRefundFinish肯定为true
     */
    public Boolean getRefundResult() {
        return refundResult;
    }

    /**
     * @return  退款创建时间, 毫秒时间戳, 13位
     */
    public Long getRefundCreatedTime() {
        return refundCreatedTime;
    }

    /**
     * 将后台返回的Map转化为BCRefundOrder实例
     * @param orderMap  包含refund order信息的map
     * @return          BCRefundOrder实例
     */
    public static BCRefundOrder transMapToRefundOrder(Map<String, Object> orderMap) {
        BCRefundOrder refundOrder = new BCRefundOrder();
        BCBill.transMapToBill(orderMap, refundOrder, true);

        if (orderMap.get("refund_no") != null)
            refundOrder.refundNum = (String) orderMap.get("refund_no");

        if (orderMap.get("refund_fee") != null)
            refundOrder.refundFee = ((Double)orderMap.get("refund_fee")).intValue();

        if (orderMap.get("finish") != null)
            refundOrder.refundFinish = (Boolean) orderMap.get("finish");
        else
            refundOrder.refundFinish = Boolean.FALSE;

        if (orderMap.get("result") != null)
            refundOrder.refundResult = (Boolean) orderMap.get("result");
        else
            refundOrder.refundResult = Boolean.FALSE;

        if (orderMap.get("created_time") != null)
            refundOrder.refundCreatedTime = ((Double)orderMap.get("created_time")).longValue();

        return refundOrder;
    }
}
