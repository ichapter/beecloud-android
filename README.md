# BeeCloud Android SDK (Open Source)

![pass](https://img.shields.io/badge/Build-pass-green.svg) ![license](https://img.shields.io/badge/license-MIT-brightgreen.svg) ![version](https://img.shields.io/badge/version-v1.3.0-blue.svg)

本SDK是根据[BeeCloud Rest API](https://github.com/beecloud/beecloud-rest-api) 开发的 Android SDK。可以作为调用BeeCloud Rest API的示例或者直接用于生产。
### [Android-SDK Changelog](https://github.com/beecloud/beecloud-android/blob/master/changelog.txt)
## 流程
![pic](http://7xavqo.com1.z0.glb.clouddn.com/UML.png)

## 安装
1. 添加依赖<br/>

>1. 对于通过添加`model`的方式（适用于`gradle`，推荐直接使用`Android Studio`）
引入`sdk model`，在`project`的`settings.gradle`中`include ':sdk'`，并在需要支付的`model`（比如本项目中的`demo`） `build.gradle`中添加依赖`compile project(':sdk')`。

>2. 对于需要以`jar`方式引入的情况<br/>
添加第三方的支付类，在`beecloud-android\sdk\libs`目录下<br/>
`gson-2.2.4.jar`为必须引入的jar，<br/>
`zxing-3.2.0.jar`为生成二维码必须引入的jar，<br/>
微信支付需要引入`libammsdk.jar`，<br/>
支付宝需要引入`alipaysdk.jar`、`alipayutdid.jar`、`alipaysecsdk.jar`，<br/>
银联需要引入`UPPayAssistEx.jar`、`UPPayPluginEx.jar`，<br/>
最后添加`beecloud android sdk`：`beecloud-android\sdk\beecloud.jar`

2.对于银联支付需要将银联插件`beecloud-android\demo\src\main\assets\UPPayPluginEx.apk`引入你的工程`assets`目录下

## 注册
1. 注册开发者：猛击[这里](http://www.beecloud.cn/register)注册成为BeeCloud开发者。  
2. 注册应用：使用注册的账号登陆[控制台](http://www.beecloud.cn/dashboard/)后，点击"+创建App"创建新应用，并配置支付参数。  

## 使用方法
>具体使用请参考项目中的`demo`

### 1.初始化支付参数
请参考`demo`中的`ShoppingCartActivity.java`
>1. 在主activity的onCreate函数中初始化BeeCloud账户中的AppID和AppSecret，例如
```java
BeeCloud.setAppIdAndSecret("c5d1cba1-5e3f-4ba0-941d-9b0a371fe719", "39a7a518-9ac8-4a9e-87bc-7885f33cf18c");
```
<br/>
>2. 如果用到微信支付，在用到微信支付的Activity的onCreate函数里调用以下函数,第二个参数需要换成你自己的微信AppID，例如
```java
BCPay.initWechatPay(ShoppingCartActivity.this, "wxf1aa465362b4c8f1");
```

### 2. 在`AndroidManifest.xml`中添加`permission`
```java
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

### 3. 在`AndroidManifest.xml`中注册`activity`

> 对于微信支付，需要添加
```java
<activity
    android:name="cn.beecloud.BCWechatPaymentActivity"
    android:launchMode="singleTop"
    android:theme="@android:style/Theme.Translucent.NoTitleBar" />
```
```java
<activity-alias
    android:name=".wxapi.WXPayEntryActivity"
    android:exported="true"
    android:targetActivity="cn.beecloud.BCWechatPaymentActivity" />
```
> 对于支付宝，需要添加
```java
<activity
    android:name="com.alipay.sdk.app.H5PayActivity"
    android:configChanges="orientation|keyboardHidden|navigation"
    android:exported="false"
    android:screenOrientation="behind"
    android:windowSoftInputMode="adjustResize|stateHidden" />
```
> 对于银联，需要添加
```java
<activity
    android:name="cn.beecloud.BCUnionPaymentActivity"
    android:configChanges="orientation|keyboardHidden"
    android:excludeFromRecents="true"
    android:launchMode="singleTop"
    android:screenOrientation="portrait"
    android:theme="@android:style/Theme.Translucent.NoTitleBar"
    android:windowSoftInputMode="adjustResize" />
```

### 4.支付

请查看`doc`中的`API`，支付类`BCPay`，参照`demo`中`ShoppingCartActivity`

**原型：** 
 
通过`BCPay`的实例，以`reqWXPaymentAsync`方法发起微信支付请求。 <br/>
通过`BCPay`的实例，以`reqAliPaymentAsync`方法发起支付宝支付请求。<br/>
通过`BCPay`的实例，以`reqUnionPaymentAsync`方法发起银联支付请求。<br/>

参数依次为
> billTitle       商品描述, UTF8编码格式, 32个字节内<br/>
> billTotalFee    支付金额，以分为单位，必须是正整数<br/>
> billNum         商户自定义订单号<br/>
> optional        为扩展参数，可以传入任意数量的key/value对来补充对业务逻辑<br/>
> callback        支付完成后的回调入口

在回调函数中将`BCResult`转化成`BCPayResult`之后做后续处理<br/>
**调用：（以微信为例）**

```java
//定义回调
BCCallback bcCallback = new BCCallback() {
    @Override
    public void done(final BCResult bcResult) {
        //此处根据业务需要处理支付结果
        final BCPayResult bcPayResult = (BCPayResult)bcResult;

        ShoppingCartActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (bcPayResult.getResult()) {
                    case BCPayResult.RESULT_SUCCESS:
                        Toast.makeText(ShoppingCartActivity.this, "用户支付成功", Toast.LENGTH_LONG).show();
                        break;
                    case BCPayResult.RESULT_CANCEL:
                        Toast.makeText(ShoppingCartActivity.this, "用户取消支付", Toast.LENGTH_LONG).show();
                        break;
                    case BCPayResult.RESULT_FAIL:
                        Toast.makeText(ShoppingCartActivity.this, "支付失败, 原因: " + bcPayResult.getErrMsg()
                                + ", " + bcPayResult.getDetailInfo(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
};

//调用支付接口
Map<String, String> mapOptional = new HashMap<>();
String optionalKey = "testkey1";    //对key暂时不支持中文
String optionalValue = "测试value值1";

mapOptional.put(optionalKey, optionalValue);

//订单标题, 订单金额(分), 订单号, 扩展参数(可以null), 支付完成后回调入口
BCPay.getInstance(ShoppingCartActivity.this).reqWXPaymentAsync("微信支付测试", 1, UUID.randomUUID().toString().replace("-", ""), mapOptional, bcCallback);
```
### 5.生成支付二维码
请查看`doc`中的`API`，支付类`BCPay`，参照`demo`中`GenQRCodeActivity`

**原型：** 
 
通过`BCPay`的实例，以`reqWXQRCodeAsync`方法请求生成微信支付二维码。 <br/>
通过`BCPay`的实例，以`reqAliQRCodeAsync`方法请求生成支付宝内嵌支付二维码。<br/>

公用参数依次为
> billTitle       商品描述, UTF8编码格式, 32个字节内<br/>
> billTotalFee    支付金额，以分为单位，必须是正整数<br/>
> billNum         商户自定义订单号<br/>
> optional        为扩展参数，可以传入任意数量的key/value对来补充对业务逻辑<br/>
> callback        支付完成后的回调入口

请求生成微信支付二维码的特有参数
> genQRCode       是否生成QRCode Bitmap
>>如果为false，请自行根据getQrCodeRawContent返回的URL，使用BCPay.generateBitmap方法生成支付二维码，你也可以使用自己熟悉的二维码生成工具

> qrCodeWidth     如果生成二维码(genQRCode为true), QRCode的宽度(以px为单位), null则使用默认参数360px

请求生成支付宝内嵌支付二维码的特有参数
> returnUrl       支付成功后的同步跳转页面, 必填
> qrPayMode       支付宝内嵌二维码类型
>>>null则支付宝生成默认类型, 不建议<br/>
   "0": 订单码-简约前置模式, 对应 iframe 宽度不能小于 600px, 高度不能小于 300px<br/>
   "1": 订单码-前置模式, 对应 iframe 宽度不能小于 300px, 高度不能小于 600px<br/>
   "3": 订单码-迷你前置模式, 对应 iframe 宽度不能小于 75px, 高度不能小于 75px<br/>

在回调函数中将`BCResult`转化成`BCQRCodeResult`之后做后续处理<br/>
**调用：（以微信为例）**
```java
BCPay.getInstance(GenQRCodeActivity.this).reqWXQRCodeAsync("微信二维码支付测试",                     //商品描述
    1,                      //订单金额
    UUID.randomUUID().toString().replace("-", ""),  //订单流水号
    mapOptional,            //扩展参数，可以null
    true,                   //是否生成二维码的bitmap,
                            //如果为false，请自行根据getQrCodeRawContent返回的结果
                            //使用BCPay.generateBitmap方法生成支付二维码
                            //你也可以使用自己熟悉的二维码生成工具
    300,                   //二维码的尺寸, 以px为单位, 如果为null则默认为360
    new BCCallback() {     //回调入口
        @Override
        public void done(BCResult bcResult) {

            final BCQRCodeResult bcqrCodeResult = (BCQRCodeResult) bcResult;

            //resultCode为0表示请求成功
            if (bcqrCodeResult.getResultCode() == 0) {
                wxQRBitmap = bcqrCodeResult.getQrCodeBitmap();
                Log.w(Tag, "weixin qrcode url: " + bcqrCodeResult.getQrCodeRawContent());

            } else {

                GenQRCodeActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(GenQRCodeActivity.this, "err code:" + bcqrCodeResult.getResultCode() +
                                "; err msg: " + bcqrCodeResult.getResultMsg() +
                                "; err detail: " + bcqrCodeResult.getErrDetail(), Toast.LENGTH_LONG).show();
                    }
                });

            }
        }
    });
```

### 6.查询

* **查询支付订单**

请查看`doc`中的`API`，支付类`BCQuery`，参照`demo`中`BillListActivity`

**原型：**

通过构造`BCQuery`的实例，使用`queryBillsAsync`方法发起支付查询，该方法仅`channel`为必填参数，指代何种支付方式；在回调函数中将`BCResult`转化成`BCQueryOrderResult`之后做后续处理

**调用：**

```java
//回调入口
final BCCallback bcCallback = new BCCallback() {
    @Override
    public void done(BCResult bcResult) {
    	//根据需求处理结果数据

        final BCQueryOrderResult bcQueryResult = (BCQueryOrderResult) bcResult;

        //resultCode为0表示请求成功
        //count包含返回的订单个数
        if (bcQueryResult.getResultCode() == 0) {

			//订单列表
	        bills = bcQueryResult.getOrders();
            Log.i(BillListActivity.TAG, "bill count: " + bcQueryResult.getCount());
        } else {
            bills = null;
            BillListActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //错误信息
                    Toast.makeText(BillListActivity.this, "err code:" + bcQueryResult.getResultCode() +
                            "; err msg: " + bcQueryResult.getResultMsg() +
                            "; err detail: " + bcQueryResult.getErrDetail(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
};

//发起查询请求
BCQuery.getInstance().queryBillsAsync(
    BCReqParams.BCChannelTypes.UN_APP,      //渠道
    null,                                   //订单号
    startTime.getTime(),                    //订单生成时间
    endTime.getTime(),                      //订单完成时间
    2,                                      //忽略满足条件的前2条数据
    15,                                     //最低返回满足条件的15条数据
    bcCallback);
```
* **查询退款订单**

请查看`doc`中的`API`，支付类`BCQuery`，参照`demo`中`RefundOrdersActivity`

**原型：**

通过构造`BCQuery`的实例，使用`queryRefundsAsync`方法发起支付查询，该方法仅`channel`为必填参数，指代何种支付方式；在回调函数中将`BCResult`转化成`BCQueryOrderResult`之后做后续处理

**调用：**<br/>
同上，首先初始化回调入口BCCallback
```java
BCQuery.getInstance().queryRefundsAsync(
    BCReqParams.BCChannelTypes.UN,          //渠道
    null,                                   //订单号
    null,                                   //商户退款流水号
    startTime.getTime(),                    //退款订单生成时间
    endTime.getTime(),                      //退款订单完成时间
    1,                                      //忽略满足条件的前2条数据
    15,                                     //只返回满足条件的15条数据
    bcCallback);
```
* **查询订单退款状态**

请查看`doc`中的`API`，支付类`BCQuery`，参照`demo`中`RefundStatusActivity`

**原型：**

通过构造`BCQuery`的实例，使用`queryRefundStatusAsync`方法发起支付查询，该方法所有参数都必填，`channel`指代何种支付方式，目前由于第三方API的限制仅支持微信；在回调函数中将`BCResult`转化成`BCQueryRefundStatusResult`之后做后续处理

**调用：**<br/>
同上，首先初始化回调入口BCCallback
```java
BCQuery.getInstance().queryRefundStatusAsync(
    BCReqParams.BCChannelTypes.WX_APP,     //目前仅支持微信
    "20150520refund001",                   //退款单号
    bcCallback);                           //回调入口
```
## Demo
考虑到个人的开发习惯，本项目提供了`Android Studio`和`Eclipse ADT`两种工程的`demo`，为了使demo顺利运行，请注意以下细节
>1. 对于使用`Android Studio`的开发人员，下载源码后可以将`demo_eclipse`移除，`Import Project`的时候选择`beecloud-android`，`sdk`为`demo`的依赖`model`，`gradle`会自动关联。
>2. 对于使用`Eclipse ADT`的开发人员，`Import Project`的时候选择`beecloud-android`下的`demo_eclipse`，该`demo`下面已经添加所有需要的`jar`。

## 测试
TODO

## 常见问题
- 关于weekhook的接收  
文档请阅读 [webhook](https://github.com/beecloud/beecloud-webhook)

## 代码贡献
我们非常欢迎大家来贡献代码，我们会向贡献者致以最诚挚的敬意。

一般可以通过在Github上提交[Pull Request](https://github.com/beecloud/beecloud-android)来贡献代码。

Pull Request要求

- 代码规范 

- 代码格式化 

- 必须添加测试！ - 如果没有测试（单元测试、集成测试都可以），那么提交的补丁是不会通过的。

- 记得更新文档 - 保证`README.md`以及其他相关文档及时更新，和代码的变更保持一致性。

- 创建feature分支 - 最好不要从你的master分支提交 pull request。

- 一个feature提交一个pull请求 - 如果你的代码变更了多个操作，那就提交多个pull请求吧。

- 清晰的commit历史 - 保证你的pull请求的每次commit操作都是有意义的。如果你开发中需要执行多次的即时commit操作，那么请把它们放到一起再提交pull请求。

## 联系我们
- 如果有什么问题，可以到QQ群-**321545822**`BeeCloud开发者大联盟`提问
- 更详细的文档，见源代码的注释以及[官方文档](https://beecloud.cn/doc/android.php)
- 如果发现了bug，欢迎提交[issue](https://github.com/beecloud/beecloud-android/issues)
- 如果有新的需求，欢迎提交[issue](https://github.com/beecloud/beecloud-android/issues)

## 代码许可
The MIT License (MIT).
