/**
 * BCCache.java
 *
 * Created by xuanzhui on 2015/7/27.
 * Copyright (c) 2015 BeeCloud. All rights reserved.
*/
package cn.beecloud;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 配置缓存类
 * 单例模式
 */
public class BCCache {

    private static BCCache instance;

    private static Activity contextActivity;

    /**
     * BeeCloud控制台注册的App Id
     */
    public String appId;

    /**
     * BeeCloud控制台注册的App Secret
     */
    public String appSecret;

    /**
     * 微信App Id
     */
    public String wxAppId;

    /**
     * PayPal
     */
    public String paypalClientID;
    public String paypalSecret;
    public BCPay.PAYPAL_PAY_TYPE paypalPayType;
    public Boolean retrieveShippingAddresses;
    public static final String BC_PAYPAL_SHARED_PREFERENCE_NAME = "BC_CACHE_PAYPAL_SHARED_PREFERENCE";
    public static final String BC_PAYPAL_UNSYNCED_STRSET = "BC_CACHE_PAYPAL_UNSYNCED";

    /**
     * 网络请求timeout时间
     * 以毫秒为单位
     */
    public Integer networkTimeout;

    /**
     * 线程池
     */
    public static ExecutorService executorService = Executors.newCachedThreadPool();

    private BCCache() {
    }

    /**
     * 唯一获取实例的方法
     * @return  BCCache实例
     */
    public synchronized static BCCache getInstance(Activity contextActivity) {
        if (instance == null) {
            instance = new BCCache();

            instance.appId = null;
            instance.appSecret = null;

            instance.wxAppId = null;

            instance.networkTimeout = 10000;
        }

        if (contextActivity != null)
            BCCache.contextActivity = contextActivity;

        return instance;
    }

    /**
     * to retrieve paypal not synced records
     */
    public Set<String> getUnSyncedPayPalRecords() {
        if (BCCache.contextActivity == null) {
            Log.e("BCCache", "NPE: can not get context activity");
            return null;
        }

        return BCCache.contextActivity.getSharedPreferences(BC_PAYPAL_SHARED_PREFERENCE_NAME, 0).getStringSet(BC_PAYPAL_UNSYNCED_STRSET, null);
    }

    /**
     * clear un-synced cache
     */
    public void clearUnSyncedPayPalRecords() {
        if (BCCache.contextActivity == null) {
            Log.e("BCCache", "NPE: can not get context activity");
            return;
        }

        final SharedPreferences prefs =
                BCCache.contextActivity.getSharedPreferences(BC_PAYPAL_SHARED_PREFERENCE_NAME, 0);
        SharedPreferences.Editor spEditor = prefs.edit();
        spEditor.clear();
        spEditor.commit();
    }

    /**
     * remove synced record
     */
    public void removeSyncedPalPalRecords(Set<String> rmRecords) {
        if (BCCache.contextActivity == null) {
            Log.e("BCCache", "NPE: can not get context activity");
            return;
        }

        final SharedPreferences prefs =
                BCCache.contextActivity.getSharedPreferences(BC_PAYPAL_SHARED_PREFERENCE_NAME, 0);

        Set<String> records = prefs.getStringSet(BC_PAYPAL_UNSYNCED_STRSET, null);

        //must allocate new object to make sure changes will be stored
        Set<String> leftRecords = new HashSet<String>();
        if (records != null)
            leftRecords.addAll(records);

        leftRecords.removeAll(rmRecords);

        SharedPreferences.Editor spEditor = prefs.edit();
        spEditor.putStringSet(BC_PAYPAL_UNSYNCED_STRSET, leftRecords);
        spEditor.commit();
    }

    /**
     * to store paypal not synced records
     */
    public void storeUnSyncedPayPalRecords(String newRecord) {
        if (BCCache.contextActivity == null) {
            Log.e("BCCache", "NPE: can not get context activity");
            return;
        }

        final SharedPreferences prefs =
                BCCache.contextActivity.getSharedPreferences(BC_PAYPAL_SHARED_PREFERENCE_NAME, 0);

        Set<String> records = prefs.getStringSet(BC_PAYPAL_UNSYNCED_STRSET, null);

        //must allocate new object to make sure changes will be stored
        Set<String> totalRecords = new HashSet<String>();
        if (records != null)
            totalRecords.addAll(records);

        totalRecords.add(newRecord);

        SharedPreferences.Editor spEditor = prefs.edit();
        spEditor.putStringSet(BC_PAYPAL_UNSYNCED_STRSET, totalRecords);
        spEditor.commit();
    }
}
