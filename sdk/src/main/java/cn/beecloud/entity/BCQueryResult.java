/**
 * BCQueryResult.java
 *
 * Created by xuanzhui on 2015/7/29.
 * Copyright (c) 2015 BeeCloud. All rights reserved.
 */
package cn.beecloud.entity;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.beecloud.async.BCResult;

/**
 * 查询结果返回类
 *
 * @see cn.beecloud.async.BCResult
 */
public class BCQueryResult implements BCResult {
    /**
     * APP内部错误编号
     */
    public static final Integer APP_INNER_FAIL_NUM = 20;
    /**
     * APP内部错误
     */
    public static final String APP_INNER_FAIL = "APP_INNER_FAIL";

    //返回码, 0为正常
    private Integer resultCode;

    //返回信息, OK为正常
    private String resultMsg;

    //具体错误信息
    private String errDetail;

    //实际返回订单结果数量
    private Integer count;

    //订单列表
    private List<BCBill> bills;

    /**
     * @return  0表示请求成功, 其他为错误编号
     */
    public Integer getResultCode() {
        return resultCode;
    }

    /**
     * @return  OK表示请求成功, 其他为错误信息
     */
    public String getResultMsg() {
        return resultMsg;
    }

    /**
     * @return  详细错误信息
     */
    public String getErrDetail() {
        return errDetail;
    }

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
    public List<BCBill> getBills() {
        return bills;
    }

    /**
     * 无参构造
     */
    public BCQueryResult(){}

    /**
     * 构造函数
     * @param resultCode    返回码
     * @param resultMsg     返回信息
     * @param errDetail     具体错误信息
     * @param count         实际返回订单结果数量
     * @param bills         订单列表
     */
    public BCQueryResult(Integer resultCode, String resultMsg, String errDetail,
                         Integer count, List<BCBill> bills) {
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
        this.errDetail = errDetail;
        this.count = count;
        this.bills = bills;
    }

    /**
     * 将json串转化为BCQueryResult实例
     * @param jsonStr   json串
     * @return          BCQueryResult实例
     */
    public static BCQueryResult transJsonToResultObject(String jsonStr){
        Gson gson = new Gson();
        Map<String, Object> responseMap = gson.fromJson(jsonStr, HashMap.class);

        BCQueryResult bcQueryResult = new BCQueryResult();
        bcQueryResult.resultCode = ((Double)responseMap.get("result_code")).intValue();
        bcQueryResult.resultMsg = (String) responseMap.get("result_msg");
        bcQueryResult.errDetail = (String) responseMap.get("err_detail");

        if (responseMap.get("count") != null)
            bcQueryResult.count = ((Double)responseMap.get("count")).intValue();

        if (responseMap.get("bills") != null) {
            List<Map<String, Object>> billsMap = (List<Map<String, Object>>) responseMap.get("bills");

            bcQueryResult.bills = new ArrayList<BCBill>();

            for (Map<String, Object> bill : billsMap){
                bcQueryResult.bills.add(BCBill.transMapToBill(bill));
            }
        }

        return bcQueryResult;
    }
}
