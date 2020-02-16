package com.stv.launcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class WifiActivity extends Activity implements OnItemClickListener {
    private final int WIFI_INIT = 1;
    private final int WIFI_SCAN = 2;
    private final int WIFI_CLOSE = 3;
    private final int WIFI_INFO = 4;

    private Context mContext;
    private ListView mListView;
    private Switch mWifiSwitch;
    private TextView mDisplayMsg;
    private Dialog mConnectDialog;

    private WifiManager mWifiManager;
    private WifiInfo mWifiInfo;
    private List<ScanResult> mScanResults;
    private WifiAdapter mAdapter;

    final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case WIFI_INIT:
                    int wifiState = mWifiManager.getWifiState();
                    if (wifiState == WifiManager.WIFI_STATE_DISABLED) { // wifi不可用
                        mWifiSwitch.setChecked(false);
                        mDisplayMsg.setText("WiFi网卡未打开");
                    } else if (wifiState == WifiManager.WIFI_STATE_UNKNOWN) {// wifi 状态未知
                        mDisplayMsg.setText("WiFi网卡状态未知");
                    } else if (wifiState == WifiManager.WIFI_STATE_ENABLED) {// OK 可用
                        mWifiSwitch.setChecked(true);
                        mHandler.sendEmptyMessageDelayed(WIFI_SCAN, 1000);
                    }
                    break;
                case WIFI_SCAN:
                    startScan();
                    mDisplayMsg.setText("正在扫描附近的WIFI...");
                    if (mScanResults == null || mScanResults.size() == 0) {
                        mHandler.sendEmptyMessageDelayed(WIFI_SCAN, 1000);
                    } else {
                        mDisplayMsg.setText("附近WiFi");
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                case WIFI_INFO:
                    String ssid = mWifiInfo.getSSID();
                    if (TextUtils.isEmpty(ssid) || ssid.equals("<unknown ssid>")) {
                        mDisplayMsg.setText("无WIFI连接");
                        mHandler.sendEmptyMessageDelayed(WIFI_INFO, 2500);
                    } else {
                        if (mWifiInfo.getIpAddress() == 0) {
                            mHandler.sendEmptyMessageDelayed(WIFI_INFO, 2500);
                        }
                        mDisplayMsg.setText("已连接到" + ssid);
                        mConnectDialog.dismiss();
                        showToast("连接成功", true);
                    }
                    break;
                case WIFI_CLOSE:
                    mDisplayMsg.setText("WIFI已关闭");
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_wifi);
        initView();

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mWifiInfo = mWifiManager.getConnectionInfo();
        mScanResults = new ArrayList<ScanResult>();
        mAdapter = new WifiAdapter(mContext, mScanResults);
        mListView.setAdapter(mAdapter);
        mHandler.sendEmptyMessageDelayed(WIFI_INIT, 1000);
    }

    public void initView() {
        mListView = (ListView) findViewById(R.id.wifi_listview);
        mWifiSwitch = (Switch) findViewById(R.id.wifi_switch);
        mDisplayMsg = (TextView) findViewById(R.id.status_view);
        mListView.setOnItemClickListener(this);
        mWifiSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (mWifiSwitch.isChecked()) {
                    if (!mWifiManager.isWifiEnabled()) {
                        mWifiManager.setWifiEnabled(true);
                    }
                    mHandler.sendEmptyMessageDelayed(WIFI_SCAN, 1000);
                } else {
                    if (mWifiManager.isWifiEnabled()) {
                        mWifiManager.setWifiEnabled(false);
                    }
                    mHandler.sendEmptyMessage(WIFI_CLOSE);
                }
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, final int pos, long id) {
        if (!mWifiInfo.getSSID().equals("\"" + mScanResults.get(pos).SSID  + "\"")) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.wifi_dialog_layout, null);
            TextView wifiName = (TextView) view.findViewById(R.id.wifidialog_name);
            final EditText password = (EditText) view.findViewById(R.id.wifi_dialog_password);
            wifiName.setText(mScanResults.get(pos).SSID);

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("连接wifi").setView(view);
            builder.setPositiveButton("连接", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String WifiPassword = password.getText().toString();
                    int status = addNetwork(creatConfiguration(mScanResults.get(pos).SSID, WifiPassword, 3));
                    if (status == 0) {
                        showToast("无线网卡不可用", false);
                    } else if (status == 1) {
                        showToast("密码错误", false);
                    } else if (status == 2) {
                        showToast("正在连接", false);
                        mConnectDialog.dismiss();
                    } else if (status == -1) {
                        showToast("连接失败", false);
                    } else {
                        showToast("正在连接", false);
                        mConnectDialog.dismiss();
                    }
                    mHandler.sendEmptyMessageDelayed(WIFI_INFO, 2000);

                }
            })
            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mConnectDialog.dismiss();
                }
            });
            mConnectDialog = builder.create();
            mConnectDialog.show();
            password.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View arg0, boolean hasFocus) {
                    if (hasFocus) {
                        mConnectDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                    }
                }
            });
        }
    }

    public void startScan() {
        mWifiManager.startScan();
        mScanResults.clear();
        mScanResults.addAll(mWifiManager.getScanResults());
    }

    public int addNetwork(WifiConfiguration configuration) {
        int configurationId = mWifiManager.addNetwork(configuration);
        mWifiManager.enableNetwork(configurationId, true);
        return configurationId;
    }

    public WifiConfiguration creatConfiguration(String SSID, String password, int type) {
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.allowedAuthAlgorithms.clear();
        configuration.allowedGroupCiphers.clear();
        configuration.allowedKeyManagement.clear();
        configuration.allowedPairwiseCiphers.clear();
        configuration.allowedProtocols.clear();
        configuration.SSID = SSID;

        WifiConfiguration tempConfiguration = isExisted(SSID);
        if (tempConfiguration != null) {
            mWifiManager.removeNetwork(tempConfiguration.networkId);
        }

        if (type == 1) {    // WIFICIPHER_NOPASS
            configuration.wepKeys[0] = "";
            configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            configuration.wepTxKeyIndex = 0;
        } else if (type == 2) { // WIFICIPHER_WEP
            configuration.hiddenSSID = true;
            configuration.wepKeys[0] = password;
            configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            configuration.wepTxKeyIndex = 0;
        } else if (type == 3) { // WIFICIPHER_WPA
            configuration.preSharedKey = password;
            configuration.hiddenSSID = true;
            configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            configuration.status = WifiConfiguration.Status.ENABLED;
        }
        return configuration;
    }

    private WifiConfiguration isExisted(String SSID) {
        List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration conf : list) {
            if (conf.SSID.equals(SSID)) {
                return conf;
            }
        }
        return null;
    }

    public void showToast(String msg, boolean isShort) {
        Toast.makeText(mContext, msg, isShort ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show();
    }
}
