/**
 * BCReqParams.java
 *
 * Created by xuanzhui on 2015/7/29.
 * Copyright (c) 2015 BeeCloud. All rights reserved.
 */
package cn.beecloud.entity;

import java.util.Date;

import cn.beecloud.BCCache;
import cn.beecloud.BCException;
import cn.beecloud.BCMD5Util;

/**
 * 向服务端请求的基类
 * 包含请求的公用参数
 */
public class BCReqParams {

    //BeeCloud应用APPID
    //BeeCloud的唯一标识
    private String appId;

    //签名生成时间
    //时间戳, 毫秒数
    private Long timestamp;

    //加密签名
    //算法: md5(app_id+timestamp+app_secret), 32位16进制格式, 不区分大小写
    private String appSign;

    /**
     * 渠道类型
     * 根据不同场景选择不同的支付方式
     */
    public String channel;

    /**
     * 渠道支付类型类
     */
    public static class BCChannelTypes {
        /**
         * 微信所有渠道
         * 仅用于查询订单
         */
        public static final String WX = "WX";

        /**
         * 微信公众号二维码支付
         * 仅用于查询订单
         */
        public static final String WX_NATIVE = "WX_NATIVE";

        /**
         * 微信公众号支付
         * 仅用于查询订单
         */
        public static final String WX_JSAPI = "WX_JSAPI";

        /**
         * 微信手机原生APP支付
         */
        public static final String WX_APP = "WX_APP";

        /**
         * 支付宝所有渠道
         * 仅用于查询订单
         */
        public static final String ALI = "ALI";

        /**
         * 支付宝手机原生APP支付
         */
        public static final String ALI_APP = "ALI_APP";

        /**
         * 支付宝PC网页支付
         * 仅用于查询订单
         */
        public static final String ALI_WEB = "ALI_WEB";

        /**
         * 支付宝内嵌二维码支付
         * 仅用于查询订单
         */
        public static final String ALI_QRCODE = "ALI_QRCODE";

        /**
         * 支付宝线下二维码支付
         * 仅用于查询订单
         */
        public static final String ALI_OFFLINE_QRCODE = "ALI_OFFLINE_QRCODE";

        /**
         * 支付宝移动网页支付
         * 仅用于查询订单
         */
        public static final String ALI_WAP = "ALI_WAP";

        /**
         * 银联所有渠道
         * 仅用于查询订单
         */
        public static final String UN = "UN";

        /**
         * 银联手机原生APP支付
         */
        public static final String UN_APP = "UN_APP";

        /**
         * 银联PC网页支付
         * 仅用于查询订单
         */
        public static final String UN_WEB = "UN_WEB";

        /**
         * 判断是否为有效的app端支付渠道类型
         * @param channel   支付渠道类型
         * @return          true表示有效
         */
        public static boolean isValidAPPPaymentChannelType(String channel){
            return channel.equals(WX_APP) ||
                    channel.equals(ALI_APP) ||
                    channel.equals(UN_APP);
        }

        /**
         * 判断是否为有效的支付渠道类型
         * @param channel   支付渠道类型
         * @return          true表示有效
         */
        public static boolean isValidPaymentChannelType(String channel){
            return channel.equals(WX) ||
                    channel.equals(WX_APP) ||
                    channel.equals(WX_NATIVE) ||
                    channel.equals(WX_JSAPI) ||
                    channel.equals(ALI) ||
                    channel.equals(ALI_APP) ||
                    channel.equals(ALI_WEB) ||
                    channel.equals(ALI_QRCODE) ||
                    channel.equals(ALI_OFFLINE_QRCODE) ||
                    channel.equals(ALI_WAP) ||
                    channel.equals(UN) ||
                    channel.equals(UN_APP) ||
                    channel.equals(UN_WEB);
        }

        /**
         * @param channel   支付渠道类型
         * @return          实际的渠道支付名
         */
        public static String getTranslatedChannelName(String channel){
            if (channel.equals(WX_APP))
                return "微信支付";
            else if (channel.equals(ALI_APP))
                return "支付宝支付";
            else if (channel.equals(UN_APP))
                return "银联支付";
            else
                return "非法的支付类型";
        }
    }

    /**
     * BeeCloud的唯一标识
     * @return  BeeCloud应用APPID
     */
    public String getAppId() {
        return appId;
    }

    /**
     * 时间戳, 毫秒数
     * @return  签名生成时间
     */
    public Long getTimestamp() {
        return timestamp;
    }

    /**
     * 算法: md5(app_id+timestamp+app_secret), 32位16进制格式, 不区分大小写
     * @return  加密签名
     */
    public String getAppSign() {
        return appSign;
    }

    /**
     * 初始化参数
     * @param channel 渠道类型
     * @param forPay  是否用于支付, 对于支付渠道的选择更严格
     */
    public BCReqParams(String channel, Boolean forPay) throws BCException{
        if (channel == null ||
                !BCChannelTypes.isValidPaymentChannelType(channel))
            throw new BCException("非法channel");

        if (forPay && !BCChannelTypes.isValidAPPPaymentChannelType(channel))
            throw new BCException("非法APP支付渠道");

        BCCache mCache = BCCache.getInstance();

        if (mCache.appId == null || mCache.appSecret == null) {
            throw new BCException("parameters: 请通过BeeCloud初始化appId和appSecret");
        } else {
            appId = mCache.appId;
            timestamp = (new Date()).getTime();
            appSign = BCMD5Util.getMessageDigest(appId +
                    timestamp + mCache.appSecret);
            this.channel = channel;
        }
    }
}
