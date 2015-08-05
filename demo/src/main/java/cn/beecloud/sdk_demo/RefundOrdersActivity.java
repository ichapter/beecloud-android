package cn.beecloud.sdk_demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.beecloud.BCQuery;
import cn.beecloud.async.BCCallback;
import cn.beecloud.async.BCResult;
import cn.beecloud.entity.BCBill;
import cn.beecloud.entity.BCQueryOrderResult;
import cn.beecloud.entity.BCReqParams;

public class RefundOrdersActivity extends Activity {

    public static final String TAG = "RefundOrdersActivity";

    Button btnWeChatRefundOrder;
    Button btnAliPayRefundOrder;
    Button btnUNPayRefundOrder;
    ListView listViewRefundOrder;

    private Handler mHandler;
    private List<BCBill> refundOrders;

    private ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refund_orders);

        loadingDialog = new ProgressDialog(RefundOrdersActivity.this);
        loadingDialog.setMessage("正在请求服务器, 请稍候...");
        loadingDialog.setIndeterminate(true);
        loadingDialog.setCancelable(true);

        // Defines a Handler object that's attached to the UI thread.
        // 通过Handler.Callback()可消除内存泄漏警告
        mHandler = new Handler(new Handler.Callback() {
            /**
             * Callback interface you can use when instantiating a Handler to
             * avoid having to implement your own subclass of Handler.
             *
             * handleMessage() defines the operations to perform when the
             * Handler receives a new Message to process.
             */
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == 1) {

                    RefundOrdersAdapter adapter = new RefundOrdersAdapter(
                            RefundOrdersActivity.this, refundOrders);
                    listViewRefundOrder.setAdapter(adapter);
                }
                return true;
            }
        });


        listViewRefundOrder = (ListView) findViewById(R.id.listViewRefundOrder);
        listViewRefundOrder.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        //回调入口
        final BCCallback bcCallback = new BCCallback() {
            @Override
            public void done(BCResult bcResult) {

                //此处关闭loading界面
                loadingDialog.dismiss();

                final BCQueryOrderResult bcQueryResult = (BCQueryOrderResult) bcResult;

                //resultCode为0表示请求成功
                //count包含返回的订单个数
                if (bcQueryResult.getResultCode() == 0) {

                    //订单列表
                    refundOrders = bcQueryResult.getOrders();

                    Log.i(BillListActivity.TAG, "bill count: " + bcQueryResult.getCount());

                } else {
                    //订单列表
                    refundOrders = null;

                    RefundOrdersActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(RefundOrdersActivity.this, "err code:" + bcQueryResult.getResultCode() +
                                    "; err msg: " + bcQueryResult.getResultMsg() +
                                    "; err detail: " + bcQueryResult.getErrDetail(), Toast.LENGTH_LONG).show();
                        }
                    });

                }

                Message msg = mHandler.obtainMessage();
                msg.what = 1;
                mHandler.sendMessage(msg);
            }
        };

        btnWeChatRefundOrder = (Button) findViewById(R.id.btnWeChatRefundOrder);
        btnWeChatRefundOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 如果调起支付太慢，可以在这里开启动画，表示正在loading
                //以progressdialog为例
                loadingDialog.show();

                BCQuery.getInstance().queryRefundsAsync(
                        BCReqParams.BCChannelTypes.WX_APP,      //直接以渠道方式查询
                        bcCallback);
            }
        });

        btnAliPayRefundOrder = (Button) findViewById(R.id.btnAliPayRefundOrder);
        btnAliPayRefundOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loadingDialog.show();

                BCQuery.getInstance().queryRefundsAsync(
                        BCReqParams.BCChannelTypes.ALI_APP,     //渠道
                        "5385eae6eb9b41e298ed982953c99b67",     //订单号
                        "201507290a2a36a9",                     //商户退款流水号
                        bcCallback);
            }
        });

        btnUNPayRefundOrder = (Button) findViewById(R.id.btnUNPayRefundOrder);
        btnUNPayRefundOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loadingDialog.show();

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

                Date startTime, endTime;
                try {
                    startTime = sdf.parse("2015-07-01 12:00");
                    endTime = sdf.parse("2015-07-30 12:00");
                } catch (ParseException e) {
                    startTime = new Date();
                    endTime = new Date();
                    e.printStackTrace();
                }

                BCQuery.getInstance().queryRefundsAsync(
                        BCReqParams.BCChannelTypes.UN,          //渠道
                        null,                                   //订单号
                        null,                                   //商户退款流水号
                        startTime.getTime(),                    //退款订单生成时间
                        endTime.getTime(),                      //退款订单完成时间
                        1,                                      //忽略满足条件的前2条数据
                        15,                                      //只返回满足条件的15条数据
                        bcCallback);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bill_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
