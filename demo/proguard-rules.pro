# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\AndroidSDK/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

#for paypal
-dontwarn com.paypal.**
-dontwarn io.card.payment.**

-keepattributes Signature

#beecloud
-keep class cn.beecloud.** { *; }

-keep class com.google.** { *; }
#支付宝
-keep class com.alipay.** { *; }
#微信
-keep class com.tencent.** { *; }
#银联
-keep class com.unionpay.** { *; }
#百度
-keep class com.baidu.** { *; }
-keep class com.dianxinos.** { *; }
#PayPal
-keep class com.paypal.** { *; }