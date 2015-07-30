/**
 * BCBill.java
 *
 * Created by xuanzhui on 2015/7/29.
 * Copyright (c) 2015 BeeCloud. All rights reserved.
 */
package cn.beecloud.entity;

import java.util.Map;

/**
 *  订单信息
 */
public class BCBill {
    //订单号
    private String billNum;

    //订单金额, 单位为分
    private Integer totalFee;

    //渠道类型
    private String channel;

    //订单标题
    private String title;

    //订单是否成功
    private Boolean payResult;

    //订单创建时间, 毫秒时间戳, 13位
    private Long createdTime;

    /**
     * @return  订单号
     */
    public String getBillNum() {
        return billNum;
    }

    /**
     * @return  订单金额, 单位为分
     */
    public Integer getTotalFee() {
        return totalFee;
    }

    /**
     * @return  渠道类型
     */
    public String getChannel() {
        return channel;
    }

    /**
     * @return  订单标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return  true表示订单支付成功, false表示尚未支付
     */
    public Boolean getPayResult() {
        return payResult;
    }

    /**
     * @return  订单创建时间, 毫秒时间戳, 13位
     */
    public Long getCreatedTime() {
        return createdTime;
    }

    /**
     * 将后台返回的Map转化为BCBill实例
     * @param billMap   包含bill信息的map
     * @return          BCBill实例
     */
    public static BCBill transMapToBill(Map<String, Object> billMap) {
        BCBill bill = new BCBill();
        if (billMap.get("bill_no") != null)
            bill.billNum = (String) billMap.get("bill_no");

        if (billMap.get("total_fee") != null)
            bill.totalFee = ((Double)billMap.get("total_fee")).intValue();

        if (billMap.get("channel") != null)
            bill.channel = (String) billMap.get("channel");

        if (billMap.get("title") != null)
            bill.title = (String) billMap.get("title");

        if (billMap.get("spay_result") != null)
            bill.payResult = (Boolean) billMap.get("spay_result");
        else
            bill.payResult = Boolean.FALSE;

        if (billMap.get("created_time") != null)
            bill.createdTime = ((Double)billMap.get("created_time")).longValue();

        return bill;
    }
}
