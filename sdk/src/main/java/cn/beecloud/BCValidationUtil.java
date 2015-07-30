/**
 * BCValidationUtil.java
 *
 * Created by xuanzhui on 2015/7/27.
 * Copyright (c) 2015 BeeCloud. All rights reserved.
 */
package cn.beecloud;

/**
 * 校验类
 */
public class BCValidationUtil {

    /**
     * 判断string是否为空
     */
    public static boolean isValidString(String str) {
        return str != null && str.length() != 0;
    }

    /**
     * 判断字符串是否为有效的正整数
     * @param str
     */
    public static boolean isStringValidPositiveInt(String str) {
        boolean res = false;

        if (isValidString(str)){

            int length = str.length();
            int i = 0;

            for (; i < length; i++) {
                char c = str.charAt(i);
                if (c <= '/' || c >= ':') {
                    break;
                }
            }

            if (i == length)
                res = true;
        }

        return res;
    }
}
