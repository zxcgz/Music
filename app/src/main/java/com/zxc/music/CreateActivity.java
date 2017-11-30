package com.zxc.music;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.zxc.music.bean.Music;
import com.zxc.music.bean.Sync;
import com.zxc.music.data.Query;
import com.zxc.music.data.Set;

import java.util.ArrayList;

import cn.bmob.v3.BmobObject;

import static com.zxc.music.data.Set.SUCCESS;

/**
 * Created by lenovo on 2017/11/2.
 */

public class CreateActivity extends Activity implements View.OnClickListener {

    private TextView mId;
    private Spinner mChoiceMusic;
    private Button mOk;
    private Button mCopy;
    private Button mStart;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    private void initView() {
        setContentView(R.layout.activity_create);
        mId = (TextView) findViewById(R.id.objectId);
        mChoiceMusic = (Spinner) findViewById(R.id.choiceMusic);
        mOk = (Button) findViewById(R.id.ok);
        mCopy = (Button) findViewById(R.id.copy);
        mStart = (Button) findViewById(R.id.start);

    }


    private Music music;
    private Sync sync ;

    private void initData() {
        //先查询Music表
        Query query = new Query();
        query.query(new Query.CallBack() {
            @Override
            public void done(Object o) {
                if (o!=null){
                    final ArrayList<Music> musics = (ArrayList<Music>) o;
                    ArrayList<String> musicName = new ArrayList<String>();
                    for (Music m :
                            musics) {
                        musicName.add(m.getMusicName());
                    }
                    music = musics.get(0);
                    mChoiceMusic.setAdapter(new ArrayAdapter<String>(CreateActivity.this, android.R.layout.simple_list_item_1, musicName));
                    mChoiceMusic.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            music = musics.get(position);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }else {
                    Toast.makeText(CreateActivity.this,"出现了错误",Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void done(BmobObject o) {

            }
        });


        mOk.setOnClickListener(this);
        mCopy.setOnClickListener(this);
        mStart.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ok:
                //创建
                //屏蔽按钮的点击事件
                mOk.setClickable(false);
                //屏蔽Spinner的点击事件
                mChoiceMusic.setClickable(false);
                Set.createMusic(music, new Set.Status() {
                    @Override
                    public void done(@Set.StatusType int type) {
                        if (type == SUCCESS) {
                            //创建成功
                            Log.e("创建", "成功");
                            //显示页面
                            mId.setVisibility(View.VISIBLE);
                            mCopy.setVisibility(View.VISIBLE);
                        } else {
                            Log.e("创建", "失败");
                            //开启按钮的点击事件
                            mOk.setClickable(true);
                        }
                    }
                    @Override
                    public void info(BmobObject object) {
                        //设置文字
                        mId.setText(object.getObjectId());
                        sync = (Sync) object;
                    }
                });
                break;
            case R.id.copy :
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("Label", mId.getText());
                cm.setPrimaryClip(mClipData);
                Toast.makeText(this,"内容已经赋值到剪贴板",Toast.LENGTH_SHORT).show();
                break;
            case R.id.start :
                //将信息存储到Sp中
                SharedPreferences sharedPreferences = getSharedPreferences("sync",MODE_PRIVATE) ;
                sharedPreferences.edit().putString("sync",sharedPreferences.getString("sync","")+"_"+sync.getObjectId()).commit() ;
                //开启页面
                Intent intent = new Intent() ;
                intent.putExtra("Music",music) ;
                intent.putExtra("Sync",sync) ;
                intent.putExtra("Status",MusicActivity.CREATE) ;
                intent.setClass(this,MusicActivity.class) ;
                startActivity(intent);
                break;
        }
    }
}
