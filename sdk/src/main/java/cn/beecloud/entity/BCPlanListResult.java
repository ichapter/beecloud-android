/**
 * BCPlanListResult.java
 * 用于plan按添加查询
 * Created by xuanzhui on 2016/7/28.
 * Copyright (c) 2016 BeeCloud. All rights reserved.
 */
package cn.beecloud.entity;

import java.util.List;

public class BCPlanListResult extends BCRestfulCommonResult {
    private List<BCPlan> plans;

    public BCPlanListResult(Integer resultCode, String resultMsg, String errDetail) {
        super(resultCode, resultMsg, errDetail);
    }

    /**
     * @return 订阅计划列表
     */
    public List<BCPlan> getPlans() {
        return plans;
    }
}
