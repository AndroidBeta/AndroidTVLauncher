package com.stv.launcher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Utils {
    private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    static final Map<String, Integer> sFilterAppIconMap = new HashMap<String, Integer>();
    static final Map<String, Integer> sFilterAppTitleMap = new HashMap<String, Integer>();

    public static final String[] sFilterAppPackage = new String[] {
            "com.stv.signalsourcemanager",
            "sina.mobile.tianqitongstv",
            "com.stv.launcher",
            "com.stv.globalsetting",
            "com.stv.systemupgrade",
            "eu.chainfire.supersu",
            "eu.chainfire.adbd",
    };

    static {
        sFilterAppIconMap.put("com.stv.signalsourcemanager", R.drawable.icon_tv);
        sFilterAppIconMap.put("sina.mobile.tianqitongstv", R.drawable.icon_weather);
        sFilterAppIconMap.put("com.stv.launcher", R.drawable.icon_file_manager);
        sFilterAppIconMap.put("com.stv.globalsetting", R.drawable.icon_settings);
        sFilterAppIconMap.put("com.stv.systemupgrade", R.drawable.icon_updater);
        sFilterAppIconMap.put("eu.chainfire.supersu", R.drawable.icon_supersu);
    }

    static {
        sFilterAppTitleMap.put("com.stv.signalsourcemanager", R.string.title_tv);
    }

    public static String getFormattedTime() {
        return df.format(System.currentTimeMillis());
    }

    public static ArrayList<AppBean> getLaunchAppList(Context context) {
        ArrayList<AppBean> list = new ArrayList<AppBean>();
        HashSet<String> addedPkgs = new HashSet<String>();
        Intent sourceIntent = new Intent();
        sourceIntent.setComponent(new ComponentName("com.stv.signalsourcemanager", "com.stv.signalsourcemanager.MainActivity"));
        list.addAll(0, getSingleAppList(context, sourceIntent, addedPkgs));
        addedPkgs.add("com.stv.signalsourcemanager");

        Intent localIntent = new Intent(Intent.ACTION_MAIN);
        localIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        for (int i = 0; i < sFilterAppPackage.length; i++) {
            localIntent.setPackage(sFilterAppPackage[i]);
            list.addAll(getSingleAppList(context, localIntent, addedPkgs));
            addedPkgs.add(sFilterAppPackage[i]);
        }

        localIntent.setPackage(null);
        list.addAll(1, getSingleAppList(context, localIntent, addedPkgs));
        return list;
    }

    private static ArrayList<AppBean> getSingleAppList(Context context, Intent intent, HashSet<String> addedPkgs) {
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> infos = pm.queryIntentActivities(intent, 0);
        ArrayList<AppBean> list = new ArrayList<AppBean>();
        for (ResolveInfo rinfo : infos) {
            String pkgName = rinfo.activityInfo.packageName;
            String className = rinfo.activityInfo.name;
            if (!addedPkgs.contains(pkgName)) {
                if (context.getPackageName().equals(pkgName) && MainActivity.class.getName().equals(className)) {
                    continue;
                }
                AppBean bean = new AppBean();
                if (sFilterAppIconMap.containsKey(pkgName)) {
                    bean.setIcon(context.getDrawable(sFilterAppIconMap.get(pkgName)));
                } else {
                    bean.setIcon(rinfo.activityInfo.applicationInfo.loadIcon(pm));
                }
                if (sFilterAppTitleMap.containsKey(pkgName)) {
                    bean.setTitle(context.getString(sFilterAppTitleMap.get(pkgName)));
                } else {
                    bean.setTitle(rinfo.loadLabel(pm).toString());
                }
                bean.setComponent(new ComponentName(pkgName, className));
                list.add(bean);
            }
        }
        return list;
    }
}
