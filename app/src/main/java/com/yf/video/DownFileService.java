package com.yf.video;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
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

public class DownFileService extends Service {

    private static final String TAG = "zdx";
    private static final String URL = "http://139.159.149.205:8081/";
    private String playName;
    private String path;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(playName == null){
            path = MainApp.PATH;
            playName = intent.getStringExtra("playName");
            Log.d(TAG,"onStartCommand " + playName);
            if(MainApp.getToken() == null){
                login();
            }else {
                getVideo(MainApp.getToken());
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    //登录获取token
    private void login(){
        String url = URL + "aidevices/api/v1/admin/user/login";
        OkHttpClient okHttpClient = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");//数据类型为json格式，
        JSONObject josnStr = new JSONObject();
        try {
            // TODO: 2021/11/6  username ，password 后期需要更换，调式环境目前固定为admin
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
                    MainApp.setToken(token);
                    getVideo(token);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    private void getVideo(final String token){
        String url = URL + "aidevices/api/v1/common/file/videoLast";
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
        String url = URL + "aidevices/api/v1/common/file/downloadVideo/" + name;
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
                DownFileUtils.createFolder(path);
                String filePath = path + "/" + name;
                try {
                    josnStr = new JSONObject(s);
                    String data = josnStr.getString("data");
                    if(!TextUtils.isEmpty(data)){
                        josnStr = new JSONObject(data);
                        String fileUrl = josnStr.getString("fileUrl");
                        DownFileUtils downFileUtils = new DownFileUtils() {
                            @Override
                            public void onFileExist(String url, String path) {
                                Log.d(TAG,"onFileExist url " + url);
                                Log.d(TAG,"onFileExist path " + path);
                            }

                            @Override
                            public void onDownFailed(String url, String path, Throwable throwable) {
                                Log.d(TAG,"下载文件失败，  " + throwable.getMessage());
                                EventBus.getDefault().post(new DownFileBean(404,throwable.getMessage()));
                            }

                            @Override
                            public void onTaskExist(String url, String path) {

                            }

                            @Override
                            public void onDownComplete(String url, String path) {
                                EventBus.getDefault().post(new DownFileBean(200,path));
                            }

                            @Override
                            public void onDownProgressUpdate(String url, String path, int progress) {
                                EventBus.getDefault().post(new DownFileBean(100,String.valueOf(progress)));
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

    //上传设备信息
    private void updata(String token, String data){
        String url = URL + "aidevices/api/v1/devices/uploadDeviceData";
        OkHttpClient okHttpClient = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");//数据类型为json格式，
        JSONObject josnStr = new JSONObject();
        try {
            josnStr.put("data",data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(JSON, josnStr.toString());
        final Request request = new Request.Builder()
                .addHeader("Authorization",token)
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
            }

        });
    }

    @Override
    public void onDestroy() {

    }
}
