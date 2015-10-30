/**
 * BCRevertStatus.java
 * <p/>
 * Created by xuanzhui on 2015/9/22.
 * Copyright (c) 2015 BeeCloud. All rights reserved.
 */
package cn.beecloud.entity;

public class BCRevertStatus extends BCRestfulCommonResult {
    private Boolean revert_status;

    /**
     * 构造函数
     *
     * @param resultCode 返回码
     * @param resultMsg  返回信息
     * @param errDetail  具体错误信息
     */
    public BCRevertStatus(Integer resultCode, String resultMsg, String errDetail) {
        super(resultCode, resultMsg, errDetail);
    }

    /**
     * 构造函数
     *
     * @param resultCode 返回码
     * @param resultMsg  返回信息
     * @param errDetail  具体错误信息
     * @param revertStatus  撤销订单结果
     */
    public BCRevertStatus(Integer resultCode, String resultMsg, String errDetail, Boolean revertStatus) {
        super(resultCode, resultMsg, errDetail);
        this.revert_status = revert_status;
    }

    public Boolean getRevertStatus() {
        return revert_status;
    }
}
