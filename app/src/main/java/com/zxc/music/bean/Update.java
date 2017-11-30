package com.zxc.music.bean;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

/**
 * Created by lenovo on 2017/11/4.
 */

public class Update extends BmobObject {
    private Integer versionCode;
    private BmobFile app ;

    public Integer getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(Integer versionCode) {
        this.versionCode = versionCode;
    }

    public BmobFile getApp() {
        return app;
    }

    public void setApp(BmobFile app) {
        this.app = app;
    }
}
