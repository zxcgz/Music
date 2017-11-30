package com.zxc.music;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.zxc.music.bean.CorrectTime;
import com.zxc.music.bean.Music;
import com.zxc.music.bean.Sync;
import com.zxc.music.data.Query;
import com.zxc.music.data.Set;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobRealTimeData;
import cn.bmob.v3.listener.ValueEventListener;
import me.wcy.lrcview.LrcView;

/**
 * Created by lenovo on 2017/11/2.
 */

public class MusicActivity extends Activity implements View.OnClickListener {

    private TextView mPoint;
    private RelativeLayout mContent;
    private LrcView mLrc;
    private Button mStopRun;
    private ProgressBar mProgressBar;
    private Music music;
    private Sync sync;
    private RelativeLayout mProcessLayout;
    private final int STOP = Set.STOP;
    private final int RUN = Set.RUN;
    private MediaPlayer mp;


    public static final String CREATE = "1";
    public static final String ADD = "2";
    private String startActivityStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    private void initView() {
        setContentView(R.layout.activity_music);
        mPoint = (TextView) findViewById(R.id.point);
        mContent = (RelativeLayout) findViewById(R.id.content);
        mLrc = (LrcView) findViewById(R.id.lrc);
        mStopRun = (Button) findViewById(R.id.stopRun);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProcessLayout = (RelativeLayout) findViewById(R.id.progressLayout);
    }

    private void initData() {
        //获取到相应的对象
        Intent intent = getIntent();
        music = (Music) intent.getSerializableExtra("Music");
        sync = (Sync) intent.getSerializableExtra("Sync");
        startActivityStatus = intent.getStringExtra("Status");
        //下载数据
        Query query = new Query();
        query.download(this, music, mPoint, new Query.CallBack() {
            @Override
            public void done(Object o) {
                if (o == null) {
                    //下载成功
                    downloadDone();
                }
            }

            @Override
            public void done(BmobObject o) {
                if (o == null) {
                    //下载成功
                    downloadDone();
                }

            }
        });
        Log.e("判断", startActivityStatus);
        if (startActivityStatus.equals(CREATE)) {
            mStopRun.setOnClickListener(this);
        } else {
            //是ADD，监听Bmob表
            realTime();

        }
    }

    private void realTime() {
        final BmobRealTimeData bmobRealTimeData = new BmobRealTimeData();
        bmobRealTimeData.start(new ValueEventListener() {

            @Override
            public void onConnectCompleted(Exception e) {
                // TODO Auto-generated method stub
                if (bmobRealTimeData.isConnected()) {
                    bmobRealTimeData.subTableUpdate("Sync");
                }
                //歌词可以拖动
                mLrc.setOnPlayClickListener(new LrcView.OnPlayClickListener() {
                    @Override
                    public boolean onPlayClick(long time) {
                        return false;
                    }
                });
            }

            @Override
            public void onDataChange(JSONObject arg0) {
                // TODO Auto-generated method stub
                if (BmobRealTimeData.ACTION_UPDATETABLE.equals(arg0.optString("action"))) {
                    JSONObject data = arg0.optJSONObject("data");
                    Gson g = new Gson();
                    Sync sync = g.fromJson(data.toString(), Sync.class);
                    Log.e("Sync", sync.getStartTime());
                    //处理数据
                    syncData(sync);
                }

            }
        });
    }

    private void syncData(final Sync sync) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //先获取状态
                Integer s = sync.getStatus();
                status = s;
                if (s == STOP) {
                    //判断是否正在播放
                    if (mp.isPlaying()) {
                        mp.pause();
                    }
                    //歌词可以滚动
                    mLrc.setOnPlayClickListener(new LrcView.OnPlayClickListener() {
                        @Override
                        public boolean onPlayClick(long time) {
                            return false;
                        }
                    });
                } else {
                    /*//获取开始播放的时间
                    long startTime = Long.decode(sync.getStartTime());
                    //获取当前时间
                    long webTime = CorrectTime.getWebTime();
                    //获取音乐开始的时间
                    long startTimeInMusic = Long.decode(sync.getStartTimeInMusic());
                    //获取音乐现在应该开始的时间*/
                    CorrectTime.getCorrectTime().setTime(Long.decode(sync.getStartTime()),Integer.decode(sync.getStartTimeInMusic()));
                    CorrectTime correctTime = CorrectTime.getCorrectTime() ;
                    long nowTime = correctTime.getMusicNowTime();
                    Log.e("计算出来的音乐应该开始的时间", nowTime + "");
                    Log.e("时间", nowTime + "");
                    //long nowTime = Long.decode(sync.getStartTimeInMusic())+500;
                    //设置播放器
                    //改变时间值，以减少网络延迟的影响
                    mp.seekTo((int) (nowTime ));
                    mp.start();
                    //设置歌词
                    mLrc.updateTime(nowTime + 16500);
                    //歌词滚动
                    mLrc.setOnPlayClickListener(new LrcView.OnPlayClickListener() {
                        @Override
                        public boolean onPlayClick(long time) {
                            return false;
                        }
                    });
                    Timer mTimer = new Timer();
                    TimerTask mTimerTask = new TimerTask() {
                        @Override
                        public void run() {
                            if (status == STOP) {
                                return;
                            }
                            mProgressBar.setProgress(mp.getCurrentPosition());
                            //设置歌词
                            mLrc.updateTime(mp.getCurrentPosition() + 16500);
                            //Log.e("播放器时间", mp.getCurrentPosition() + "");

                        }
                    };
                    mTimer.schedule(mTimerTask, 0, 10);
                }
            }
        }).start();

    }

    private void downloadDone() {
        mContent.setVisibility(View.VISIBLE);
        mProcessLayout.setVisibility(View.GONE);
        //设置歌词
        mLrc.loadLrc("{[ti:我们走在大路上]\n" +
                "[ar:红色摇滚]\n" +
                "[al:]\n" +
                "[by:K@]\n" +
                "[offset:500]\n" +
                "[00:25.18]我们走在大路上\n" +
                "[00:29.23]意气风发斗志昂扬\n" +
                "[00:33.47]共产党领导革命队伍\n" +
                "[00:37.64]披荆斩棘奔向前方\n" +
                "[00:41.84]向前进！向前进！\n" +
                "[00:46.02]革命气势不可阻挡\n" +
                "[00:50.25]向前进！向前进！\n" +
                "[00:54.37]朝着胜利的方向\n" +
                "[00:59.27]革命红旗迎风飘扬\n" +
                "[01:02.70]中华儿女奋发图强\n" +
                "[01:06.88]勤恳建设锦绣河山\n" +
                "[01:11.13]誓把祖国变成天堂\n" +
                "[01:15.31]向前进！向前进！\n" +
                "[01:19.54]革命气势不可阻挡\n" +
                "[01:23.66]向前进！向前进！\n" +
                "[01:27.86]朝着胜利的方向\n" +
                "[01:39.28]我们的道路洒满阳光\n" +
                "[01:43.38]我们的歌声传四方\n" +
                "[01:47.54]我们的朋友遍及全球\n" +
                "[01:52.81]五州架起友谊桥梁\n" +
                "[01:56.96]向前进！向前进！\n" +
                "[02:00.21]革命气势不可阻挡\n" +
                "[02:04.36]向前进向前进\n" +
                "[02:08.49]朝着胜利的方向\n" +
                "[02:12.73]我们的道路多么宽广\n" +
                "[02:16.96]我们的前程无比辉煌\n" +
                "[02:21.09]献身这壮丽的事业\n" +
                "[02:25.31]无限幸福无限荣光\n" +
                "[02:29.50]向前进向前进\n" +
                "[02:33.67]革命气势不可阻挡\n" +
                "[02:37.66]向前进向前进\n" +
                "[02:41.85]朝着胜利的方向\n" +
                "}");
        Log.e("判断歌词", mLrc.hasLrc() + "");
        if (startActivityStatus.equals(CREATE)) {
            mLrc.setOnPlayClickListener(new LrcView.OnPlayClickListener() {
                @Override
                public boolean onPlayClick(long time) {
                    Log.e("时间", "" + time);
                    //设置时间
                    int seekTime = (int) ((time - 16500));
                    mp.seekTo(seekTime);
                    //将信息存储到服务器中
                    //判断当前播放状态
                    if (status==STOP){
                        //播放
                        Log.e("播放状态","暂停") ;
                        mp.start();
                        status=RUN;
                        setProcessBar(seekTime);
                    }
                    set(seekTime);
                    return true;
                }
            });
        }

        //初始化歌曲信息
        initMusic();
    }

    private int musicLength;

    private void initMusic() {
        Uri uri = Uri.parse(getFilesDir().getAbsoluteFile().toString() + "/" + music.getObjectId() + ".mus");
        mp = MediaPlayer.create(this, uri);
        //获取文件的长度
        musicLength = mp.getDuration();
        mProgressBar.setMax(musicLength);
    }

    //默认为暂停状态
    private int status = STOP;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.stopRun:
                //判断当前的状态
                if (status == STOP) {
                    Log.e("播放", "开始");
                    //改变状态
                    status = RUN;
                    mp.start();

                    int currentPosition = mp.getCurrentPosition();
                    set(currentPosition);

                    //设置进度条
                    mProgressBar.setProgress(currentPosition);
                    Timer mTimer = new Timer();
                    TimerTask mTimerTask = new TimerTask() {
                        @Override
                        public void run() {
                            if (status == STOP) {
                                return;
                            }
                            mProgressBar.setProgress(mp.getCurrentPosition());
                            //设置歌词
                            mLrc.updateTime(mp.getCurrentPosition() + 16500);
                            //Log.e("播放器时间", mp.getCurrentPosition() + "");

                        }
                    };
                    mTimer.schedule(mTimerTask, 0, 10);

                } else {
                    status = STOP;
                    mp.pause();
                    set(mp.getCurrentPosition());
                    Log.e("播放", "暂停");
                }
                break;
        }
    }

    private void set(int time) {
        //判断当前是否是同步方
        if (startActivityStatus.equals(CREATE)) {
            Set.setSync(sync, status, time, new Set.Status() {
                @Override
                public void done(@Set.StatusType int type) {
                    if (type == Set.SUCCESS) {
                        //成功
                    } else {
                        //失败
                        Toast.makeText(MusicActivity.this, "出现了意外", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void info(BmobObject object) {

                }
            });
        } else {

        }

    }

    @Override
    public void finish() {
        //弹出对话框
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("是否要停止音乐并退出？");
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //释放资源
                if (mp != null) {
                    mp.stop();
                    mp.release();
                    mp = null;
                }
                MusicActivity.super.finish();
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        dialog.show();
    }
    private void setProcessBar(int position){
        //设置进度条
        mProgressBar.setProgress(position);
        Timer mTimer = new Timer();
        TimerTask mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (status == STOP) {
                    return;
                }
                mProgressBar.setProgress(mp.getCurrentPosition());
                //设置歌词
                mLrc.updateTime(mp.getCurrentPosition() + 16500);
                //Log.e("播放器时间", mp.getCurrentPosition() + "");

            }
        };
        mTimer.schedule(mTimerTask, 0, 10);
    }
}
