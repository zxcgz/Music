package com.zxc.music.data;

import android.content.Context;
import android.support.annotation.IntDef;

import com.instacart.library.truetime.TrueTime;
import com.zxc.music.bean.Music;
import com.zxc.music.bean.Sync;

import java.io.File;
import java.io.IOException;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * 创建相关
 * Created by zxc on 2017/11/2.
 */

public class Set {


    public static final int SYNC = 0x1; //处于同步状态
    public static final int NOTSYNC = 0x1 << 1;//同步处于关闭状态
    public static final int STOP = SYNC + SYNC << 2;//处于停止状态
    public static final int RUN = SYNC + SYNC << 3;//处于运行状态

    public static void createMusic(final Music music, final Status status) {


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    TrueTime.build().withServerResponseDelayMax(500).initialize();//创建一个元组
                    final Sync sync = new Sync();
                    sync.setMusic(music);
                    sync.setStartTime(TrueTime.now().getTime() + "");
                    sync.setStatus(STOP);//同步但是没有开启，所以初始状态为STOP
                    sync.setStartTimeInMusic("0");
                    sync.save(new SaveListener<String>() {
                        @Override
                        public void done(String s, BmobException e) {
                            if (e == null) {
                                status.done(SUCCESS);
                                status.info(sync);
                            } else {
                                status.done(FAIL);
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }

    public static final int SUCCESS = 1;
    public static final int FAIL = 2;

    public interface Status {
        void done(@StatusType int type);

        void info(BmobObject object);
    }

    @IntDef({SUCCESS, FAIL})
    public @interface StatusType {
        int value() default SUCCESS;
    }


    public static void setSync(final Sync sync, final int status, final long timeInMusic, final Status callBack) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    TrueTime.build().withServerResponseDelayMax(500).initialize();
                    sync.setStatus(status);
                    sync.setStartTime(TrueTime.now().getTime()+ "");
                    sync.setStartTimeInMusic(timeInMusic + "");
                    sync.update(new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if (e == null) {
                                //更新成功
                                callBack.done(SUCCESS);
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    public static void downloadApp(Context context,String url, final DownloadAppCallBack callBack) {
        BmobFile bmobFile = new BmobFile();
        bmobFile.setUrl(url);
        File externalCacheDir = context.getExternalCacheDir();
        File file = new File(externalCacheDir.getAbsolutePath()+"/newApp.apk");
        bmobFile.download(file, new DownloadFileListener() {
            @Override
            public void done(String s, BmobException e) {
                if (e == null) {
                    //下载成功
                    callBack.done(true);
                } else {
                    callBack.done(false);
                }
            }

            @Override
            public void onProgress(Integer integer, long l) {

            }
        });
    }

    public interface DownloadAppCallBack {
        void done(boolean hasDownload);
    }
}
