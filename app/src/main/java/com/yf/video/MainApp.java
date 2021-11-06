package com.yf.video;

import android.app.Application;

import com.liulishuo.filedownloader.FileDownloader;

import java.io.File;

public class MainApp extends Application {

    private static MainApp mainApp;
    public static String PATH ; // 视频存放的路径；
    public static String TOKEN = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mainApp = this;
        PATH = getExternalFilesDir("").getAbsolutePath() + File.separator + "downloadVideo";
        FileDownloader.setup(this);
    }

    public static MainApp getMainApp() {
        return mainApp;
    }

    public static void setToken(String token){
        TOKEN = token;
    }

    public static String getToken() {
        return TOKEN;
    }
}
