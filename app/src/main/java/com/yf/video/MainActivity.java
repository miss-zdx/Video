package com.yf.video;


import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

public class MainActivity extends Activity {

    String TAG = "zdx";
    String playingPath ;
    private VideoView videoView;
    private TextView tv;
    int stopPosition= 0;
    private String playName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        videoView = findViewById(R.id.videoView);
        tv = findViewById(R.id.tv);
        getDownloadVideo();
        Intent intent = new Intent(this,DownFileService.class);
        intent.putExtra("playName",playName);
        startService(intent);
    }

    private void getDownloadVideo(){
        File file = new File(MainApp.PATH);
        Log.d(TAG, "onCreate: " + file.exists());
        if(file.exists()){
            File[] fileArray = file.listFiles();
            if(fileArray.length > 0){
                File fileName = fileArray[0];
                playName = fileName.getName();
                Log.d(TAG, "playName: " + playName);
                Log.d(TAG, "playName substring: " + playName.substring(playName.length() - 3,playName.length()));
                if(!playName.substring(playName.length() - 3,playName.length()).equals("mp4")){
                    Log.d(TAG,"文件名错误删除 = " + new File(fileName.getPath()).delete());
                }
                if(fileName.exists() && playName.substring(playName.length() - 3,playName.length()).equals("mp4")){
                    startVideo(fileName.getPath());
                    playingPath = fileName.getPath();
                }
                Log.d(TAG, "getDownloadVideo: " + fileName.getPath());
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownFileBean(DownFileBean downFileBean) {
        Log.d("zdx", "onDownFileBean: " + downFileBean.toString());
        if(downFileBean.getCode() == 404){
            tv.setText(" 下载失败 ："+ downFileBean.getMsg());
        }else if(downFileBean.getCode() == 100){
            tv.setText("视频加载"+ downFileBean.getMsg() + "%");
        }else if(downFileBean.getCode() == 200 && !TextUtils.isEmpty(downFileBean.getMsg())){
            File file = new File(MainApp.PATH);
            if(file.exists()){
                File[] fileArray = file.listFiles();
                if(fileArray.length == 1){
                    if(!videoView.isPlaying()){
                        startVideo(downFileBean.getMsg());
                        playingPath = downFileBean.getMsg();
                    }
                }else if(fileArray.length > 1 && videoView.isPlaying()){
                    startVideo(downFileBean.getMsg());
                    boolean isDelete = new File(playingPath).delete();
                    Log.d(TAG, "onDownFileBean: " + isDelete);
                    if(isDelete){
                        playingPath = downFileBean.getMsg();
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(videoView != null && videoView.canPause()){
            videoView.seekTo(stopPosition);
            videoView.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(videoView.isPlaying()){
            stopPosition = videoView.getCurrentPosition();
            videoView.pause();
        }
    }

    private void startVideo(String path){
        videoView.setVisibility(View.VISIBLE);
        tv.setVisibility(View.GONE);
        videoView.resume();
        videoView.setVideoPath(path);
        //创建MediaController对象
        MediaController mediaController = new MediaController(this);

        //VideoView与MediaController建立关联
        mediaController.setVisibility(View.GONE);
        videoView.setMediaController(mediaController);

        //让VideoView获取焦点
        videoView.requestFocus();
        videoView.start();
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mPlayer) {
                // TODO Auto-generated method stub
                mPlayer.start();
                mPlayer.setLooping(true);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if(videoView != null){
            videoView.resume();
        }
    }
}
