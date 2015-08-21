/**
 * BCOrder.java
 * <p/>
 * Created by xuanzhui on 2015/8/19.
 * Copyright (c) 2015 BeeCloud. All rights reserved.
 */
package cn.beecloud.entity;

class BCOrder {
    /**
     * 以下命名需要和restful API匹配
     * 以便于Gson反序列化
     * 请忽略命名规则
     */
    //订单号
    private String bill_no;

    //订单金额, 单位为分
    private Integer total_fee;

    //渠道类型
    private String channel;

    //订单标题
    private String title;

    //订单创建时间, 毫秒时间戳, 13位
    protected Long created_time;

    /**
     * @return  订单号
     */
    public String getBillNum() {
        return bill_no;
    }

    /**
     * @return  订单金额, 单位为分
     */
    public Integer getTotalFee() {
        return total_fee;
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
}
