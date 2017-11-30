package com.zxc.music.bean;

import java.io.Serializable;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.datatype.BmobPointer;

/**
 * Created by lenovo on 2017/11/2.
 */

public class Sync extends BmobObject implements Serializable {
    private Integer status ;
    private String startTime ;
    private String startTimeInMusic ;
    private Music music ;
    private Integer time ;

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getStartTimeInMusic() {
        return startTimeInMusic;
    }

    public void setStartTimeInMusic(String startTimeInMusic) {
        this.startTimeInMusic = startTimeInMusic;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Music getMusic() {
        return music;
    }

    public void setMusic(Music music) {
        this.music = music;
    }
}
