/**
 * PayPalUnSyncedListActivity.java
 *
 * Created by xuanzhui on 2015/9/1.
 * Copyright (c) 2015 BeeCloud. All rights reserved.
 */
package cn.beecloud.demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.beecloud.BCCache;
import cn.beecloud.BCPay;
import cn.beecloud.demo.util.DisplayUtils;
import cn.beecloud.entity.BCPayResult;

public class PayPalUnSyncedListActivity extends Activity {

    private String result;

    TextView syncTip;
    ListView unSyncedListView;
    Button btnBatchSync;

    PayPalUnSyncedListAdapter unSyncedAdapter;
    List<String> adapterData;

    private ProgressDialog loadingDialog;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 1) {
                unSyncedAdapter.notifyDataSetChanged();
            }else if (msg.what == 2) {
                syncTip.setVisibility(View.VISIBLE);
                unSyncedListView.setVisibility(View.GONE);
                btnBatchSync.setVisibility(View.GONE);
            }else if (msg.what == 3) {
                Toast.makeText(PayPalUnSyncedListActivity.this, "sync failed: " + result, Toast.LENGTH_LONG).show();
            }

            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paypal_unsynced_list);

        DisplayUtils.initBack(this);

        syncTip = (TextView) findViewById(R.id.syncTip);
        unSyncedListView = (ListView)findViewById(R.id.unSyncedListView);
        btnBatchSync = (Button) findViewById(R.id.btnBatchSync);

        loadingDialog = new ProgressDialog(PayPalUnSyncedListActivity.this);
        loadingDialog.setMessage("sync with server, please wait...");
        loadingDialog.setIndeterminate(true);
        loadingDialog.setCancelable(true);

        adapterData = BCCache.getInstance(this).getUnSyncedPayPalRecords();

        if (adapterData == null || adapterData.size() == 0) {
            syncTip.setVisibility(View.VISIBLE);
            unSyncedListView.setVisibility(View.GONE);
            btnBatchSync.setVisibility(View.GONE);
        }
        else {
            unSyncedAdapter= new PayPalUnSyncedListAdapter(this, adapterData);

            unSyncedListView.setAdapter(unSyncedAdapter);

            unSyncedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    loadingDialog.show();

                    BCCache.executorService.execute(new Runnable() {
                        @Override
                        public void run() {

                            //sync for each json string
                            result = BCPay.getInstance(PayPalUnSyncedListActivity.this).
                                    syncPayPalPayment(adapterData.get(position), null);

                            if (result.equals(BCPayResult.RESULT_SUCCESS)) {
                                adapterData.remove(position);

                                if (adapterData.size() == 0) {
                                    Message msg = mHandler.obtainMessage();
                                    msg.what = 2;
                                    mHandler.sendMessage(msg);
                                } else {
                                    Message msg = mHandler.obtainMessage();
                                    msg.what = 1;
                                    mHandler.sendMessage(msg);
                                }

                            } else {
                                Message msg = mHandler.obtainMessage();
                                msg.what = 3;
                                mHandler.sendMessage(msg);

                            }

                            loadingDialog.dismiss();
                        }
                    });
                }
            });

            btnBatchSync.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadingDialog.show();

                    BCCache.executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            //batch sync
                            Map<String, Integer> result = BCPay.getInstance(PayPalUnSyncedListActivity.this).
                                    batchSyncPayPalPayment();

                            //total cached number
                            Integer allCached = result.get("cachedNum");
                            //total successfully synced number
                            Integer synced = result.get("syncedNum");

                            if (allCached.equals(synced)) {
                                Message msg = mHandler.obtainMessage();
                                msg.what = 2;
                                mHandler.sendMessage(msg);
                            } else {

                                adapterData = new ArrayList<String>(BCCache.getInstance(PayPalUnSyncedListActivity.this).
                                        getUnSyncedPayPalRecords());

                                Message msg = mHandler.obtainMessage();
                                msg.what = 1;
                                mHandler.sendMessage(msg);
                            }

                            loadingDialog.dismiss();
                        }
                    });

                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BCPay.detach();
    }
}
