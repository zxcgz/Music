package com.zxc.music.bean;

import java.io.Serializable;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

/**
 * Created by lenovo on 2017/11/1.
 */

public class Music extends BmobObject implements Serializable{
    private BmobFile music ;
    private BmobFile lrc ;
    private String md5ForMusic ;
    private String md5ForLrc;
    private String musicName ;

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public String getMd5ForMusic() {
        return md5ForMusic;
    }

    public void setMd5ForMusic(String md5ForMusic) {
        this.md5ForMusic = md5ForMusic;
    }

    public String getMd5ForLrc() {
        return md5ForLrc;
    }

    public void setMd5ForLrc(String md5ForLrc) {
        this.md5ForLrc = md5ForLrc;
    }

    public BmobFile getMusic() {
        return music;
    }

    public void setMusic(BmobFile music) {
        this.music = music;
    }

    public BmobFile getLrc() {
        return lrc;
    }

    public void setLrc(BmobFile lrc) {
        this.lrc = lrc;
    }
}
