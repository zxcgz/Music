package com.zxc.music;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zxc.music.bean.CorrectTime;
import com.zxc.music.bean.Music;
import com.zxc.music.bean.Sync;
import com.zxc.music.data.Query;

import java.util.ArrayList;

import cn.bmob.v3.BmobObject;

/**
 * 历史记录
 * Created by zxc on 2017/11/4.
 */

public class HistoryActivity extends Activity implements View.OnClickListener {

    private RecyclerView mList;
    private ArrayList<String> mHistoryList = new ArrayList<>();
    private Button mCreate;
    private Button mAdd;
    private MyAdapter mMyAdapter;
    private boolean createIsClicked = true ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    private void initData() {
        //获取历史信息
        getHistoryForCreate();
        mList.setLayoutManager(new LinearLayoutManager(this));
        mMyAdapter = new MyAdapter();
        mList.setAdapter(mMyAdapter);
        mCreate.setOnClickListener(this);
        mAdd.setOnClickListener(this);
    }

    private void initView() {
        setContentView(R.layout.activity_history);
        mList = (RecyclerView) findViewById(R.id.list);
        mCreate = (Button) findViewById(R.id.create);
        mAdd = (Button) findViewById(R.id.add);
    }

    public void getHistoryForCreate() {
        mHistoryList.clear();
        SharedPreferences sharedPreferences = getSharedPreferences("sync", MODE_PRIVATE);
        String sync = sharedPreferences.getString("sync", null);
        if (sync == null) {
            Toast.makeText(this, "出现了错误", Toast.LENGTH_SHORT).show();
        } else {
            //拆分数据
            String[] split = sync.split("_");
            for (int i = 0; i < split.length-1; i++) {
                mHistoryList.add(split[i+1]);
            }
        }
    }public void getHistoryForAdd() {
        mHistoryList.clear();
        SharedPreferences sharedPreferences = getSharedPreferences("sync", MODE_PRIVATE);
        String sync = sharedPreferences.getString("join", null);
        if (sync == null) {
            Toast.makeText(this, "出现了错误", Toast.LENGTH_SHORT).show();
        } else {
            //拆分数据
            String[] split = sync.split("_");
            for (int i = 0; i < split.length; i++) {
                mHistoryList.add(split[i]);
            }
        }
    }


    private class MyAdapter extends RecyclerView.Adapter {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = View.inflate(HistoryActivity.this, R.layout.history_list_item, null);
            MyViewHolder holder = new MyViewHolder(v);
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            MyViewHolder h = (MyViewHolder) holder;
            Log.e("测试数据",(h.history==null)+"") ;
            h.history.setText(mHistoryList.get(position));
            h.history.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //查询数据
                    Query q = new Query();
                    q.querySync(mHistoryList.get(position), new Query.CallBack() {
                        @Override
                        public void done(Object o) {
                            if (o != null) {
                                startAct(o);
                            }

                        }

                        @Override
                        public void done(BmobObject o) {
                            if (o != null) {
                                startAct(o);
                            }
                        }
                    });
                }
            });
        }

        @Override
        public int getItemCount() {
            return mHistoryList.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            RelativeLayout view;
            TextView history;

            public MyViewHolder(View itemView) {
                super(itemView);
                view = (RelativeLayout) itemView;
                history = (TextView) itemView.findViewById(R.id.history);
            }
        }
    }

    private void startAct(Object o) {
        final Sync sync = (Sync) o;
        if (!createIsClicked){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //设置时间矫正类
                    CorrectTime correctTime = CorrectTime.getCorrectTime() ;
                    correctTime.setTime(Long.decode(sync.getStartTime()), Integer.decode(sync.getStartTimeInMusic()));
                }
            }).start();
        }
        //开启页面
        Music music = sync.getMusic();
        Intent intent = new Intent();
        intent.putExtra("Music", music);
        intent.putExtra("Sync", sync);
        //判断当前的状态
        intent.putExtra("Status", createIsClicked?MusicActivity.CREATE:MusicActivity.ADD);
        intent.setClass(this, MusicActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create:
                createIsClicked = true ;
                mCreate.setTextColor(Color.RED);
                mAdd.setTextColor(Color.BLACK);
                getHistoryForCreate();
                mMyAdapter.notifyDataSetChanged();
                break;
            case R.id.add:
                createIsClicked = false ;
                mCreate.setTextColor(Color.BLACK);
                mAdd.setTextColor(Color.RED);
                getHistoryForAdd();
                mMyAdapter.notifyDataSetChanged();
                break;
        }
    }
}
