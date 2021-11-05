package com.yf.video;


import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends Activity {

    String TAG = "ZDX";
    String PATH ;
    String playingPath ;
    private VideoView videoView;
    private TextView tv;
    int stopPosition= 0;
    private String playName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        videoView = findViewById(R.id.videoView);
        tv = findViewById(R.id.tv);
        PATH = getExternalFilesDir("").getAbsolutePath() + File.separator + "downloadVideo";
        File file = new File(PATH);
        Log.d(TAG, "onCreate: " + file.exists());
        if(file.exists()){
            File[] fileArray = file.listFiles();
            if(fileArray.length > 0){
                File fileName = fileArray[0];
                playName = fileName.getName();
                Log.d(TAG, "playName: " + playName);
                Log.d(TAG, "playName: " + playName.substring(playName.length() - 3,playName.length()));
                if(!playName.substring(playName.length() - 3,playName.length()).equals("mp4")){
                   Log.d(TAG,"文件名错误删除 = " + new File(fileName.getPath()).delete());
                }
                if(fileName.exists() && playName.substring(playName.length() - 3,playName.length()).equals("mp4")){
                    startVideo(fileName.getPath());
                    playingPath = fileName.getPath();
                }
                Log.d(TAG, "onCreate: " + fileName.getPath());
            }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                login();
            }
        }).start();
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
    private void login(){
        String url = "http://139.159.149.205:8081/aidevices/api/v1/admin/user/login";
        OkHttpClient okHttpClient = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");//数据类型为json格式，
        JSONObject josnStr = new JSONObject();
        try {
            josnStr.put("username","admin");
            josnStr.put("password","admin");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(JSON, josnStr.toString());
        final Request request = new Request.Builder()
                .url(url)
                .post(body)//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String data = response.body().string();
                Log.d(TAG, "onResponse: " + data);
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    JSONObject str = jsonObject.getJSONObject("data");
                    String token = str.getString("token");
                    getVideo(token);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    private void getVideo(final String token){
        String url = "http://139.159.149.205:8081/aidevices/api/v1/common/file/videoLast";
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .addHeader("Authorization",token)
                .url(url)
                .get()//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String s = response.body().string();
                Log.d(TAG, "onResponse: " + s);
                JSONObject josnStr = null;
                try {
                    josnStr = new JSONObject(s);
                    String data = josnStr.getString("data");
                    if(!TextUtils.isEmpty(data)){
                        josnStr = new JSONObject(data);
                        String name = josnStr.getString("name");
                        if(TextUtils.isEmpty(playName)){
                            downloadVideo(token,name);
                        }else {
                            if(!TextUtils.equals(playName,name)){
                                downloadVideo(token,name);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    private void downloadVideo(String token , final String name){
        Log.d("zdx","downloadVideo");
        String url = "http://139.159.149.205:8081/aidevices/api/v1/common/file/downloadVideo/" + name;
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .addHeader("Authorization",token)
                .url(url)
                .get()//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String s = response.body().string();
                Log.d(TAG, "onResponse: " + s);
                JSONObject josnStr = null;
                DownFileUtils.createFolder(PATH);
                String filePath = PATH + "/" + name;
                try {
                    josnStr = new JSONObject(s);
                    String data = josnStr.getString("data");
                    if(!TextUtils.isEmpty(data)){
                        josnStr = new JSONObject(data);
                        String fileUrl = josnStr.getString("fileUrl");
                        DownFileUtils downFileUtils = new DownFileUtils() {
                            @Override
                            public void onFileExist(String url, String path) {
                                Log.d("zdx","url " + url);
                                Log.d("zdx","path " + path);
                            }

                            @Override
                            public void onDownFailed(String url, String path, Throwable throwable) {
                                Log.d("zdx","下载文件失败，  " + throwable.getMessage());
                                tv.setText("文件下载出错");

                            }

                            @Override
                            public void onTaskExist(String url, String path) {

                            }

                            @Override
                            public void onDownComplete(String url, String path) {
                                Log.d("zdx","onDownComplete path = " + path);
                                Log.d("zdx","onDownComplete url= " + url);
                                File file = new File(PATH);
                                if(file.exists()){
                                    File[] fileArray = file.listFiles();
                                    if(fileArray.length == 1){
                                        if(!videoView.isPlaying()){
                                            startVideo(path);
                                            playingPath = path;
                                        }
                                    }else if(fileArray.length > 1 && videoView.isPlaying()){
                                        startVideo(path);
                                        boolean isDelete = new File(playingPath).delete();
                                        Log.d(TAG, "onDownComplete: " + isDelete);
                                        if(isDelete){
                                            playingPath = path;
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onDownProgressUpdate(String url, String path, int progress) {
                                Log.d("zdx","progress " + progress);
                            }
                        };
                        downFileUtils.downFile(fileUrl, filePath);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
    }

}
