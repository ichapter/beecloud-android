/**
 * BCSubscriptionListResult.java
 * 用于subscription按条件查询
 * Created by xuanzhui on 2016/7/28.
 * Copyright (c) 2016 BeeCloud. All rights reserved.
 */
package cn.beecloud.entity;

import java.util.List;

public class BCSubscriptionListResult extends BCRestfulCommonResult {
    private List<BCSubscription> subscriptions;

    public BCSubscriptionListResult(Integer resultCode, String resultMsg, String errDetail) {
        super(resultCode, resultMsg, errDetail);
    }

    /**
     * @return 订阅列表
     */
    public List<BCSubscription> getSubscriptions() {
        return subscriptions;
    }
}
