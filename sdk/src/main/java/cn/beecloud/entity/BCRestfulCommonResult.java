/**
 * BCRestfulCommonResult.java
 *
 * Created by xuanzhui on 2015/7/29.
 * Copyright (c) 2015 BeeCloud. All rights reserved.
 */
package cn.beecloud.entity;

import java.util.Map;

import cn.beecloud.async.BCResult;

/**
 * 服务端返回的通用结果信息
 *
 * @see cn.beecloud.async.BCResult
 */
public abstract class BCRestfulCommonResult implements BCResult {
    /**
     * APP内部错误编号
     */
    public static final Integer APP_INNER_FAIL_NUM = 20;
    /**
     * APP内部错误
     */
    public static final String APP_INNER_FAIL = "APP_INNER_FAIL";

    //返回码, 0为正常
    protected Integer resultCode;

    //返回信息, OK为正常
    protected String resultMsg;

    //具体错误信息
    protected String errDetail;

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
     * 无参构造
     */
    public BCRestfulCommonResult(){}

    /**
     * 构造函数
     * @param resultCode    返回码
     * @param resultMsg     返回信息
     * @param errDetail     具体错误信息
     */
    public BCRestfulCommonResult(Integer resultCode, String resultMsg, String errDetail) {
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
        this.errDetail = errDetail;
    }

    /**
     * 将json串转化为BCQueryResult实例
     * @param responseMap   包含result信息的map
     * @param bcRestfulCommonResult BCQueryResult实例
     */
    protected static void transJsonToResultObject(Map<String, Object> responseMap, BCRestfulCommonResult bcRestfulCommonResult){

        bcRestfulCommonResult.resultCode = ((Double)responseMap.get("result_code")).intValue();
        bcRestfulCommonResult.resultMsg = String.valueOf(responseMap.get("result_msg"));
        bcRestfulCommonResult.errDetail = String.valueOf(responseMap.get("err_detail"));

    }

    /**
     * 将json串转化为BCQueryResult实例
     * @param jsonStr   json串
     * @return          BCQueryResult实例
     */
    public abstract BCRestfulCommonResult transJsonToResultObject(String jsonStr);
}
