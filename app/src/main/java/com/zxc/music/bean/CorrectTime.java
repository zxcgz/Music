package com.zxc.music.bean;

import com.instacart.library.truetime.TrueTime;

import java.io.IOException;
import java.util.Date;

/**
 * 矫正时间的类
 * Created by zxc on 2017/11/3.
 */

public class CorrectTime {

    private static TrueTime trueTime ;

    private static CorrectTime correctTime = new CorrectTime() ;
    //本机与网络时间的差值
    private static int nowTime ;
    //同步方与网络时间的差值
    private static int syncTime ;
    //同步方同步的时间
    private static long syncStartTime ;
    //音乐开始的时间
    private static int musicStartTime ;
    static {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    trueTime = TrueTime.build();
                    trueTime.withServerResponseDelayMax(500).initialize();
                    if (TrueTime.isInitialized()){
                        Date now = TrueTime.now();
                        nowTime = (int) now.getTime();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    private CorrectTime(){}
    public static CorrectTime getCorrectTime(){
        return correctTime ;
    }
    public void setTime( long syncStartTime1, int musicStartTime1){
        syncStartTime = syncStartTime1 ;
        musicStartTime = musicStartTime1;
    }
    public int getMusicNowTime(){
        //获取网络时间
        long netTime = TrueTime.now().getTime() ;
        //根据同步方同步的时间和差值计算出同步时的网络时间
        //计算出歌曲播放时长
        long musicLong = netTime-syncStartTime ;
        //根据歌曲开始时间计算出歌曲现在播放的位置
        int position = (int) (musicLong+musicStartTime);
        return position ;
    }

}
