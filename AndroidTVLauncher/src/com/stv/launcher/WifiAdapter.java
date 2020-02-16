package com.stv.launcher;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class WifiAdapter extends BaseAdapter {
    private List<ScanResult> mData;
    private Context mContext;
    private WifiManager mWifiManager;

    public WifiAdapter(Context context, List<ScanResult> scanResults) {
        mData = scanResults;
        mContext = context;
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int arg0) {
        return arg0;
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.wifi_item, null);
        }
        TextView wifiName = (TextView) view.findViewById(R.id.wifi_name);
        TextView status = (TextView) view.findViewById(R.id.wifi_status);
        String ssid = mData.get(position).SSID;
        wifiName.setText(ssid);
        WifiInfo info = mWifiManager.getConnectionInfo();
        if (info != null && info.getSSID().equals("\""+ssid+"\"")) {
            status.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
            status.setText("已连接");
        } else {
            status.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.wifi_lock, 0, 0);
        }
        return view;
    }
}
