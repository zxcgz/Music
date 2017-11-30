package com.zxc.music;

import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.CallSuper;
import android.util.Log;

import com.zxc.music.data.Query;
import com.zxc.music.data.Set;

import java.io.File;

import cn.bmob.v3.Bmob;

/**
 * Created by lenovo on 2017/11/11.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        initBmob();
        //checkVersion();
    }

    private void initBmob() {
        Bmob.initialize(this, "13045b6ec580f1219153a055c4c0fa52");

    }
}
