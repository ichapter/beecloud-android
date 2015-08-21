/**
 * BCQueryOrderResult.java
 * <p/>
 * Created by xuanzhui on 2015/8/3.
 * Copyright (c) 2015 BeeCloud. All rights reserved.
 */
package cn.beecloud.entity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class BCQueryBillOrderResult extends BCRestfulCommonResult {

    //实际返回订单结果数量
    private Integer count;

    //支付订单列表
    private List<BCBillOrder> bills;

    /**
     * @return  实际返回订单结果数量
     */
    public Integer getCount() {
        return count;
    }

    /**
     * @return  支付订单列表
     * @see     BCBillOrder
     */
    public List<BCBillOrder> getBills() {
        return bills;
    }

    /**
     * 无参构造
     */
    public BCQueryBillOrderResult(){}

    /**
     * 构造函数
     *
     * @param resultCode 返回码
     * @param resultMsg  返回信息
     * @param errDetail  具体错误信息
     */
    public BCQueryBillOrderResult(Integer resultCode, String resultMsg, String errDetail) {
        super(resultCode, resultMsg, errDetail);
    }

    /**
     * 构造函数
     * @param resultCode    返回码
     * @param resultMsg     返回信息
     * @param errDetail     具体错误信息
     * @param count         实际返回订单结果数量
     * @param bills         订单列表
     */
    public BCQueryBillOrderResult(Integer resultCode, String resultMsg, String errDetail,
                                  Integer count, List<BCBillOrder> bills) {
        super(resultCode, resultMsg, errDetail);
        this.count = count;
        this.bills = bills;
    }

    /**
     * 将json串转化为BCQueryOrderResult实例
     * @param jsonStr   json串
     * @return          BCQueryOrderResult实例
     */
    public BCQueryBillOrderResult transJsonToResultObject(String jsonStr){
        //反序列化json
        Gson res = new Gson();

        return res.fromJson(jsonStr,new TypeToken<BCQueryBillOrderResult>() {}.getType() );
    }
}
