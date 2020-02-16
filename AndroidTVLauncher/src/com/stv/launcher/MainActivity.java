package com.stv.launcher;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends Activity implements OnClickListener {
    private static final int MSG_TIME = 1;
    private AppAdapter mAdapter;
    private List<AppBean> mListData;
    private Context mContext;
    private TextView mTimeView;
    private ImageView mNetworkView;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mTimeView.setText(Utils.getFormattedTime());
            mHandler.sendEmptyMessageDelayed(MSG_TIME, 1000L);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);
        mTimeView = (TextView) findViewById(R.id.time_view);
        mNetworkView = (ImageView) findViewById(R.id.network_view);
        mNetworkView.setOnClickListener(this);
        GridView gridView = (GridView) findViewById(R.id.grid_view);

        Typeface tf = Typeface.createFromAsset(getAssets(), "font/helvetica_neueltpro_thex.otf");
        mTimeView.setTypeface(tf);

        mListData = Utils.getLaunchAppList(mContext);
        mAdapter = new AppAdapter(mContext, mListData);
        gridView.setAdapter(mAdapter);
        mHandler.sendEmptyMessage(MSG_TIME);
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        filter.addDataScheme("package");
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onClick(View v) {
        if (v == mNetworkView) {
            startActivity(new Intent(this, WifiActivity.class));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_PACKAGE_ADDED) || action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
                String packageName = intent.getData().getSchemeSpecificPart();
                mListData.clear();
                mListData.addAll(Utils.getLaunchAppList(mContext));
                mAdapter.notifyDataSetChanged();
            } else if (WifiManager.RSSI_CHANGED_ACTION.equals(action)) {
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                if (wifiInfo.getBSSID() != null) {
                    int signalLevel = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 4);
                    if (signalLevel == 0) {
                        mNetworkView.setImageResource(R.drawable.wifi_1);
                    } else if (signalLevel == 1) {
                        mNetworkView.setImageResource(R.drawable.wifi_2);
                    } else if (signalLevel == 2) {
                        mNetworkView.setImageResource(R.drawable.wifi_3);
                    } else if (signalLevel == 3) {
                        mNetworkView.setImageResource(R.drawable.wifi_4);
                    }
                } else {
                    mNetworkView.setImageResource(R.drawable.wifi_off);
                }
            }
        }
    };

    private class AppAdapter extends BaseAdapter {
        private List<AppBean> mData;
        private Context context;

        public AppAdapter(Context context, List<AppBean> appBeanList) {
            this.context = context;
            this.mData = appBeanList;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.app_item, null);
            }
            ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
            TextView name = (TextView) convertView.findViewById(R.id.title);
            final AppBean appBean = mData.get(position);
            icon.setImageDrawable(appBean.getIcon());
            name.setText(appBean.getTitle());

            convertView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setComponent(appBean.getComponent());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    mContext.startActivity(intent);
                }
            });
            convertView.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Uri packageURI = Uri.parse("package:" + appBean.getComponent().getPackageName());
                    mContext.startActivity(new Intent(Intent.ACTION_DELETE, packageURI));
                    return true;
                }
            });
            convertView.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    v.animate().scaleX(hasFocus ? 1.1f : 1f).scaleY(hasFocus ? 1.1f : 1f).translationZ(1).start();
                }
            });
            return convertView;
        }
    }
}