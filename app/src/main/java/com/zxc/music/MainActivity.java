package com.zxc.music;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.zxc.music.bean.Sync;
import com.zxc.music.bean.CorrectTime;
import com.zxc.music.data.Query;
import com.zxc.music.data.Set;

import java.io.File;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobObject;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mAdd;
    private Button mCreate;
    private Button mHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initBomb();
        initView();
        initData();
    }

    private void initData() {

        mCreate.setOnClickListener(this);
        mAdd.setOnClickListener(this);
        mHistory.setOnClickListener(this);
        //检查版本
        checkVersion();

    }

    private void checkVersion() {
        try {
            PackageManager packageManager = getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo("com.zxc.music", 0);
            int versionCode = packageInfo.versionCode;
            Log.e("版本号",versionCode+"") ;
            Query query = new Query();
            query.queryVersion(versionCode, new Query.VersionCallBack() {
                @Override
                public void done(boolean hasNewVersion, final String url) {
                    if (hasNewVersion) {
                        //弹出对话框
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("存在新版本，是否要更新");
                        builder.setPositiveButton("更新", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //下载文件并安装
                                Set.downloadApp(MainActivity.this,url, new Set.DownloadAppCallBack() {
                                    @Override
                                    public void done(boolean hasDownload) {
                                        if (hasDownload) {
                                            //下载成功
                                            //安装
                                            //跳转到系统安装页面
                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// 以新压入栈
                                            File file = new File(MainActivity.this.getExternalCacheDir().getAbsolutePath()+"/newApp.apk");
                                            intent.setDataAndType(Uri.fromFile(file),
                                                    "application/vnd.android.package-archive");
                                            startActivityForResult(intent, 0);// 如果用户取消安装的话,
                                            // 会返回结果,回调方法onActivityResult
                                        } else {
                                            //下载失败
                                        }
                                    }
                                });
                            }
                        });
                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        builder.show();
                    }else {
                        //没有新版本，检测cache目录中是否有文件
                        String s = getExternalCacheDir().getAbsolutePath() + "/newApp.apk";
                        File file = new File(s) ;
                        if (file.exists()){
                            //将文件删除
                            file.delete();
                        }
                    }
                }
            });
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        setContentView(R.layout.activity_main);
        mAdd = (Button) findViewById(R.id.add);
        mCreate = (Button) findViewById(R.id.create);
        mHistory = (Button) findViewById(R.id.history);
    }

    //初始化Bomb云
    private void initBomb() {
        Bmob.initialize(this, "13045b6ec580f1219153a055c4c0fa52");
    }

    @Override
    public void onClick(View v) {
        //判断点击的按钮
        switch (v.getId()) {
            case R.id.create:
                //判断是否存在创建记录
                if (checkSync()) {
                    //弹出对话框让用户选择
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("您曾经创建过同步，是否要再创建？");
                    builder.setPositiveButton("仍要创建", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(MainActivity.this, CreateActivity.class));
                        }
                    });
                    builder.setNegativeButton("查看历史记录", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(MainActivity.this, HistoryActivity.class));
                        }
                    });
                    builder.show();
                } else {
                    //创建
                    startActivity(new Intent(this, CreateActivity.class));
                }

                break;
            case R.id.add:
                //弹出对话框
                Log.e("对话框", "弹出");
                Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.dialog_input);
                dialog.setTitle("请输入Id号");
                dialog.show();
                Button ok = (Button) dialog.findViewById(R.id.ok);
                final EditText objectId = (EditText) dialog.findViewById(R.id.objectId);
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //查询
                        Query query = new Query();
                        query.querySync(objectId.getText().toString(), new Query.CallBack() {
                            @Override
                            public void done(Object o) {
                                if (o == null) {
                                    Toast.makeText(MainActivity.this, "查询Id失败1", Toast.LENGTH_SHORT).show();
                                } else {
                                    startAct((Sync) o);
                                }
                            }

                            @Override
                            public void done(BmobObject o) {
                                if (o == null) {
                                    Toast.makeText(MainActivity.this, "查询Id失败2", Toast.LENGTH_SHORT).show();
                                } else {
                                    startAct((Sync) o);

                                }
                            }


                        });
                    }
                });
                break;
            case R.id.history:
                //开启页面
                startActivity(new Intent(this, HistoryActivity.class));
                break;
        }

    }

    private boolean checkSync() {
        SharedPreferences sharedPreferences = getSharedPreferences("sync", MODE_PRIVATE);
        String sync = sharedPreferences.getString("sync", null);
        if (sync != null) {
            //存在数据
            return true;
        } else {
            return false;
        }
    }

    private void startAct(final Sync sync) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e("测试数据是否正确", sync.getTime() + "...." + Long.decode(sync.getStartTime()) + "...." + sync.getStartTimeInMusic());
                //设置时间矫正类
                CorrectTime correctTime = CorrectTime.getCorrectTime() ;
                correctTime.setTime(Long.decode(sync.getStartTime()), Integer.decode(sync.getStartTimeInMusic()));
                //new CorrectTime(sync.getTime(), Long.decode(sync.getStartTime()), Integer.decode(sync.getStartTimeInMusic()));
            }
        }).start();
        //将数据存储到Sp中
        SharedPreferences sharedPreferences = getSharedPreferences("sync", MODE_PRIVATE);
        sharedPreferences.edit().putString("join", sharedPreferences.getString("join", "") + "_" + sync.getObjectId()).commit();
        //开启页面
        Intent intent = new Intent();
        intent.putExtra("Music", sync.getMusic());
        intent.putExtra("Sync", sync);
        intent.putExtra("Status", MusicActivity.ADD);
        intent.setClass(MainActivity.this, MusicActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==0){
            //安装完成
            Log.e("安装","完成") ;
        }
    }
}
