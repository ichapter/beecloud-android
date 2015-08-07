/**
 * BCQueryOrderResult.java
 * <p/>
 * Created by xuanzhui on 2015/8/3.
 * Copyright (c) 2015 BeeCloud. All rights reserved.
 */
package cn.beecloud.entity;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BCQueryOrderResult extends BCRestfulCommonResult {
    //实际返回订单结果数量
    private Integer count;

    //订单列表
    private List<BCBill> orders;

    /**
     * @return  实际返回订单结果数量
     */
    public Integer getCount() {
        return count;
    }

    /**
     * @return  订单列表
     * @see     BCBill
     */
    public List<BCBill> getOrders() {
        return orders;
    }

    /**
     * 无参构造
     */
    public BCQueryOrderResult(){}

    /**
     * 构造函数
     * @param resultCode    返回码
     * @param resultMsg     返回信息
     * @param errDetail     具体错误信息
     * @param count         实际返回订单结果数量
     * @param orders         订单列表
     */
    public BCQueryOrderResult(Integer resultCode, String resultMsg, String errDetail,
                         Integer count, List<BCBill> orders) {
        super(resultCode, resultMsg, errDetail);
        this.count = count;
        this.orders = orders;
    }

    /**
     * 将json串转化为BCQueryOrderResult实例
     * @param jsonStr   json串
     * @return          BCQueryOrderResult实例
     */
    @Override
    public BCRestfulCommonResult transJsonToResultObject(String jsonStr){
        Gson gson = new Gson();
        Map<String, Object> responseMap = gson.fromJson(jsonStr, HashMap.class);

        BCQueryOrderResult bcQueryResult = new BCQueryOrderResult();

        BCRestfulCommonResult.transJsonToResultObject(responseMap, bcQueryResult);

        if (responseMap.get("count") != null)
            bcQueryResult.count = ((Double)responseMap.get("count")).intValue();

        //如果是支付订单则bills可能包含列表
        //如果是退款订单则refunds可能包含列表
        List<Map<String, Object>> ordersMap = null;

        if (responseMap.get("bills") != null) {
            ordersMap = (List<Map<String, Object>>) responseMap.get("bills");
            bcQueryResult.orders = new ArrayList<BCBill>();

            for (Map<String, Object> bill : ordersMap){
                bcQueryResult.orders.add(BCBill.transMapToBill(bill));
            }
        } else if (responseMap.get("refunds") != null) {
            ordersMap = (List<Map<String, Object>>) responseMap.get("refunds");
            bcQueryResult.orders = new ArrayList<BCBill>();

            for (Map<String, Object> bill : ordersMap){
                bcQueryResult.orders.add(BCRefundOrder.transMapToRefundOrder(bill));
            }
        }

        return bcQueryResult;
    }
}
