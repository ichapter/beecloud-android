/**
 * BCBill.java
 *
 * Created by xuanzhui on 2015/7/29.
 * Copyright (c) 2015 BeeCloud. All rights reserved.
 */
package cn.beecloud.entity;

/**
 *  支付订单信息
 */
public class BCBillOrder extends BCOrder{
    /**
     * 以下命名需要和restful API匹配
     * 以便于Gson反序列化
     * 请忽略命名规则
     */
    //订单是否成功
    private Boolean spay_result;

    /**
     * @return  true表示订单支付成功, false表示尚未支付
     */
    public Boolean getPayResult() {
        return spay_result;
    }

    /**
     * @return  订单创建时间, 毫秒时间戳, 13位
     */
    public Long getCreatedTime() {
        return created_time;
    }
}
