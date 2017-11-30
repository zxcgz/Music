package com.zxc.music.data;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.zxc.music.bean.Music;
import com.zxc.music.bean.Sync;
import com.zxc.music.bean.Update;

import java.io.File;
import java.util.List;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

/**
 * 查询
 * Created by zxc on 2017/11/2.
 */

public class Query<T> {

    public void query(final CallBack callBack) {
        BmobQuery<Music> music = new BmobQuery<>();
        music.findObjects(new FindListener<Music>() {
            @Override
            public void done(List<Music> list, BmobException e) {
                if (e==null){
                    callBack.done(list);
                }else {
                    callBack.done(null);
                }


            }
        });
    }

    public interface CallBack {
        void done(Object o);

        void done(BmobObject o);
    }
    /*public interface callBack{
        void done(T t) ;
    }*/

    //下载文件
    public void download(final Context context, final Music music, final TextView textView, final CallBack callBack) {
        //upload(context,music);
        final File musicFile = new File(context.getFilesDir().getAbsoluteFile().toString()+"/"+music.getObjectId()+".mus") ;
        final File lrcFile = new File(context.getFilesDir().getAbsoluteFile().toString()+"/"+music.getObjectId()+".lrc") ;
        //判断歌曲文件是否存在
        if (musicFile.exists()){
            //判断歌词文件是否存在
            if (lrcFile.exists()){
                //文件存在，返回
                callBack.done(null);
            }else {
                downloadLrc(music,lrcFile,textView,callBack);
            }
        }else {
            downloadMusic(music,musicFile,lrcFile,textView,callBack);
        }
    }

    private void upload(final Context context, final Music music) {
        final BmobFile file = new BmobFile(new File("/storage/emulated/0/1111111.lrc"));
        //判断文件是否存在
        file.upload(new UploadFileListener() {
            @Override
            public void done(BmobException e) {
                if (e==null){
                    music.setLrc(file);
                    music.update(new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            Toast.makeText(context, "设置成功", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    Toast.makeText(context, "设置失败", Toast.LENGTH_SHORT).show();
                    Log.e("设置",e.toString()) ;
                }

            }
        });
    }

    private void downloadMusic(final Music music, File musicFile, final File lrcFile, final TextView textView, final CallBack callBack) {
        final BmobFile bmobFile = new BmobFile();
        Log.e("歌曲文件",(music.getMusic()==null)+"") ;
        bmobFile.setUrl(music.getMusic().getUrl());

        bmobFile.download(musicFile, new DownloadFileListener() {
            @Override
            public void done(String s, BmobException e) {
                if (e == null) {
                    //下载歌词
                    downloadLrc(music, lrcFile, textView, callBack);
                }
            }

            @Override
            public void onProgress(Integer integer, long l) {
                textView.setText("正在下载歌曲");
            }
        });
    }

    private void downloadLrc(final Music music, File lrcFile, final TextView textView, final CallBack callBack) {
        BmobFile file = new BmobFile();
        file.setUrl(music.getLrc().getUrl());
        file.download(lrcFile, new DownloadFileListener() {
            @Override
            public void done(String s, BmobException e) {
                if (e == null) {
                    //下载成功没有信息返回

                    callBack.done(null);
                } else {
                    //下载失败
                    Log.e("下载", "失败1" + e.toString());
                }
            }

            @Override
            public void onProgress(Integer integer, long l) {
                textView.setText("正在下载歌词");
            }
        });
    }

    public void querySync(final String objectId, final CallBack callBack){
        BmobQuery<Sync> syncBmobQuery = new BmobQuery<>() ;
        //syncBmobQuery.addWhereEqualTo("objectId",objectId) ;
        syncBmobQuery.include("music") ;
        syncBmobQuery.findObjects(new FindListener<Sync>() {
            @Override
            public void done(List<Sync> list, BmobException e) {
                if (e==null&&list!=null&&list.size()>0){
                    for (Sync s :
                            list) {
                        if (s.getObjectId().equals(objectId)){
                            callBack.done(s);
                            //Log.e("时间",s.getStartTime());
                            return;
                        }
                    }
                    callBack.done(null);
                }else {
                    callBack.done(null);
                }
            }
        }) ;
    }
    //查询版本信息
    public void queryVersion(final int nowVersionCode , final VersionCallBack versionCallBack){
        BmobQuery<Update> updateBmobQuery = new BmobQuery<>() ;
        updateBmobQuery.findObjects(new FindListener<Update>() {
            @Override
            public void done(List<Update> list, BmobException e) {
                //Log.e("查找",(list==null)+"") ;
                if (list!=null&&list.size()>0){
                    Update update = list.get(0);
                    Log.e("查找",list.get(0).getVersionCode()+"") ;
                    int version = update.getVersionCode();
                    if (version>nowVersionCode){
                        //存在新版本
                        versionCallBack.done(true,update.getApp().getUrl());
                    }else {
                        versionCallBack.done(false,null);
                    }
                }else {
                    versionCallBack.done(false,null);
                }
            }
        }) ;
    }
    public interface VersionCallBack{
        void done(boolean hasNewVersion,@Nullable String url) ;
    }
}
