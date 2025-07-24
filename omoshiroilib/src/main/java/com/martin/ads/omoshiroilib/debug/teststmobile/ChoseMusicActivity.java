package com.martin.ads.omoshiroilib.debug.teststmobile;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.content.res.AssetFileDescriptor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.martin.ads.omoshiroilib.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChoseMusicActivity extends AppCompatActivity implements View.OnClickListener {
    RecyclerView musicRv;
    //    数据源
    List<LocalMusicBean> mDatas;
    private LocalMusicAdapter adapter;
    MediaPlayer mediaPlayer;
    private String cur_music;
    private boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chose_music);
        initView();
        mediaPlayer = new MediaPlayer();
        mDatas = new ArrayList<>();
//     创建适配器对象
        adapter = new LocalMusicAdapter(this, mDatas);
        musicRv.setAdapter(adapter);
//        设置布局管理器
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        musicRv.setLayoutManager(layoutManager);
//        加载本地数据源
        loadLocalMusicData();
//        设置每一项的点击事件
        setEventListener();
    }

    private void setEventListener() {
        /* 设置每一项的点击事件*/
        adapter.setOnItemClickListener(new LocalMusicAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                if (adapter.selectPosition == position) {
                    // 取消选中
                    adapter.setSelectPosition(-1);
                    flag=false;
                } else {
                    adapter.setSelectPosition(position); // 更新选中的项
                    flag=true;
                }
                adapter.notifyDataSetChanged();
                LocalMusicBean musicBean = mDatas.get(position);
                playMusicInMusicBean(musicBean);
            }
        });
    }

    public void playMusicInMusicBean(LocalMusicBean musicBean) {
        /*根据传入对象播放音乐*/
        stopMusic();
//                重置多媒体播放器
        mediaPlayer.reset();
//                设置新的播放路径
        try {
            cur_music = musicBean.getPath();
            mediaPlayer.setDataSource(musicBean.getPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String albumArt = musicBean.getAlbumArt();
        Log.i("lsh123", "playMusicInMusicBean: albumpath==" + albumArt);
        Bitmap bm = BitmapFactory.decodeFile(albumArt);
        Log.i("lsh123", "playMusicInMusicBean: bm==" + bm);
        playMusic();
    }

    /*
     * 点击播放按钮播放音乐，或者暂停从新播放
     * 播放音乐有两种情况：
     * 1.从暂停到播放
     * 2.从停止到播放
     * */
    private void playMusic() {
        /* 播放音乐的函数*/
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            try {
                mediaPlayer.prepare();
                mediaPlayer.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*  private void pauseMusic() {
     *//* 暂停音乐的函数*//*
        if (mediaPlayer!=null&&mediaPlayer.isPlaying()) {
            currentPausePositionInSong = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
        }
    }*/
    private void stopMusic() {
        /* 停止音乐的函数*/
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
            mediaPlayer.stop();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMusic();
    }

    private void loadLocalMusicData() {
        /* 加载本地存储当中的音乐mp3文件到集合当中*/
//        1.获取ContentResolver对象
        ContentResolver resolver = getContentResolver();
//        2.获取本地音乐存储的Uri地址
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//        3 开始查询地址
        Cursor cursor = resolver.query(uri, null, null, null, null);
//        4.遍历Cursor
        int id = 0;
        while (cursor.moveToNext()) {
            String song = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String singer = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            id++;
            String sid = String.valueOf(id);
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
            String time = sdf.format(new Date(duration));
//          获取专辑图片主要是通过album_id进行查询
            String album_id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            String albumArt = getAlbumArt(album_id);
//            将一行当中的数据封装到对象当中
            LocalMusicBean bean = new LocalMusicBean(sid, song, singer, album, time, path, albumArt);
            mDatas.add(bean);
        }
//        数据源变化，提示适配器更新
        adapter.notifyDataSetChanged();
    }


    private String getAlbumArt(String album_id) {
        String mUriAlbums = "content://media/external/audio/albums";
        String[] projection = new String[]{"album_art"};
        Cursor cur = this.getContentResolver().query(
                Uri.parse(mUriAlbums + "/" + album_id),
                projection, null, null, null);
        String album_art = null;
        if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
            cur.moveToNext();
            album_art = cur.getString(0);
        }
        cur.close();
        cur = null;
        return album_art;
    }

    private void initView() {
        /* 初始化控件的函数*/
        musicRv = findViewById(R.id.local_music_rv);
        findViewById(R.id.bt_confirm).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_confirm) {
            Intent intent = new Intent();
            if(flag)
            {
                intent.putExtra("path", cur_music);
            }else {
                intent.putExtra("path", "0");
            }
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }
}