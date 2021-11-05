package com.yf.video;

import android.app.Application;

import com.liulishuo.filedownloader.FileDownloader;

public class MainApp extends Application {
    private static MainApp mainApp;

    @Override
    public void onCreate() {
        super.onCreate();
        mainApp = this;
        FileDownloader.setup(this);
    }

    public static MainApp getMainApp() {
        return mainApp;
    }
}
