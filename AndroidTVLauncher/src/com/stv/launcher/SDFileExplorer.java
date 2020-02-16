package com.stv.launcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SDFileExplorer extends Activity {
    private TextView mPathText;
    private ListView mListView;
    private Button toParentBtn;
    private File mCurrentFile;   // 记录当前的父文件夹
    private File[] mCurrentFiles;// 记录当前文件下所有的文件数组
    private File mSDPath; // 得到内存卡的路径

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sdfile_explorer);
        mPathText = (TextView) findViewById(R.id.file_path);
        mListView = (ListView) findViewById(R.id.file_list);
        toParentBtn = (Button) findViewById(R.id.parent);
        // 第一次的时候判断如果有内存卡、那么父类为 sd卡、文件夹数组为SD卡下的所有文件
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            mCurrentFile = Environment.getExternalStorageDirectory();
            mCurrentFiles = mCurrentFile.listFiles();
            inflateListView( mCurrentFiles);
        }

        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mCurrentFiles[position].isFile()) {
                    String fileName = mCurrentFiles[position].getName();
                    String ext = fileName.substring(fileName.lastIndexOf(".") + 1);
                    if ("apk".equalsIgnoreCase(ext)) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(mCurrentFiles[position]), "application/vnd.android.package-archive");
                        startActivity(intent);
                    }
                } else {
                    // 获取用户点击的文件夹下的所有文件
                    File[] temp = mCurrentFiles[position].listFiles();
                    if (temp == null || temp.length == 0) {
                        Toast.makeText(SDFileExplorer.this, "当前路径不可访问或该路径下没有文件", Toast.LENGTH_SHORT).show();
                    } else {
                        // 用户当前点击的列表项作为父文件夹
                        mCurrentFile = mCurrentFiles [position];
                        // 保存当前文件夹下内的全部文件和文件夹
                        mCurrentFiles = temp;
                        // 更新ListView
                        inflateListView(mCurrentFiles);
                    }
                }
            }
        });

        mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showDeleteDialog(position);
                return true;
            }
        });

        toParentBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                upToParent();
            }
        });
    }

    private void inflateListView(File[] files) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < files.length; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            if (files[i].isDirectory()) {
                map.put("icon", R.drawable.folder);
            } else {
                String ext = files[i].getName().substring(files[i].getName().lastIndexOf(".") + 1);
                if ("apk".equalsIgnoreCase(ext)) {
                    map.put("icon", R.drawable.apk);
                } else if ("mp3".equalsIgnoreCase(ext)){
                    map.put("icon", R.drawable.music);
                } else if ("jpg".equalsIgnoreCase(ext) || "png".equalsIgnoreCase(ext) || "jpeg".equalsIgnoreCase(ext)) {
                    map.put("icon", R.drawable.photo);
                } else {
                    map.put("icon", R.drawable.file);
                }
            }
            map.put("fileName", files[i].getName());
            list.add(map);
        }
        SimpleAdapter adapter = new SimpleAdapter(this,
                list, R.layout.line, new String[] { "icon", "fileName"},
                new int[] { R.id.icon, R.id. file_name});
        mListView.setAdapter(adapter);
        try {
            mPathText.setText("当前路径为:" + mCurrentFile.getCanonicalPath());
        } catch (IOException e) {

        }
    }

    private void showDeleteDialog(final int position) {
        AlertDialog.Builder builder = new Builder(this);
        builder.setMessage("确定删除文件？");
        builder.setTitle("提示");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                File file = mCurrentFiles[position];
                if (file.isFile()) {
                    file.delete();
                } else {
                    deleteDirWithFile(file);
                }
                // refresh
                mCurrentFiles = mCurrentFile.listFiles();
                inflateListView(mCurrentFiles);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public static void deleteDirWithFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return;
        }
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                file.delete(); // 删除所有文件
            } else if (file.isDirectory()) {
                deleteDirWithFile(file); // 递规的方式删除文件夹
            }
        }
        dir.delete();// 删除目录本身
    }

    @Override
    public void onBackPressed() {
        upToParent();
    }

    private long exitTime = 0;
    private void upToParent() {
        try {
            if (mCurrentFile != null && !mCurrentFile.getCanonicalFile().equals(mSDPath)) {
                // 获取上一级目录
                mCurrentFile = mCurrentFile.getParentFile();
                // 列出当前文件夹下的所有文件
                if (mCurrentFile != null) {
                    mCurrentFiles = mCurrentFile.listFiles();
                    inflateListView( mCurrentFiles);
                }
            } else {
                if ((System.currentTimeMillis() - exitTime) > 2000) {
                    Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    exitTime = System.currentTimeMillis();
                } else {
                    finish();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}