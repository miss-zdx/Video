package com.yf.video;

import android.util.Log;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;

import java.io.File;

public abstract class DownFileUtils {

    public void downFile(final String url, final String path) {
        Log.d("zdx","开始下载  下载链接  " + url + "   保存地址  " + path);
        File file = new File(path);
        if (file.exists()) {
            onFileExist(url, path);
            return;
        }
        FileDownloader.getImpl().create(url)
                .setPath(path,false)
                .setCallbackProgressTimes(300)
                .setMinIntervalUpdateSpeed(400)
                //.setCallbackProgressIgnored() //忽略所有回调，性能高点
                .setAutoRetryTimes(1)
                .setForceReDownload(true)
                .setListener(new FileDownloadSampleListener() {
                    //等待
                    @Override
                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        Log.d("zdx","开始下载");
                    }

                    //下载进度回调
                    @Override
                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        Log.d("zdx","soFarBytes =   " + soFarBytes);
                        Log.d("zdx","totalBytes  =   " + totalBytes);
                        Log.d("zdx","下载进度   " + (soFarBytes * 1.0) / (1.0 * totalBytes));
                        onDownProgressUpdate(url, path, (int) ((soFarBytes * 1.0) / (1.0 * totalBytes) * 100));
                    }

                    //完成下载
                    @Override
                    protected void completed(BaseDownloadTask task) {
                        Log.d("zdx","下载成功");
                        onDownComplete(url, path);
                    }

                    //暂停
                    @Override
                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                    }

                    //下载出错
                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        Log.d("zdx","下载出错  " + e.getMessage());
                        onDownFailed(url, path, e);
                    }

                    //已存在相同下载
                    @Override
                    protected void warn(BaseDownloadTask task) {
                        Log.d("zdx","已存在   任务");
                        onTaskExist(url, path);
                    }
                }).start();
    }

    public static void createFolder(String folder) {
        File file = new File(folder);
        if (!file.exists()) {
            file.mkdirs();
        }
    }


    /**
     * 文件已存在
     *
     * @param url
     * @param path
     */
    public abstract void onFileExist(String url, String path);

    /**
     * 下载失败
     *
     * @param url
     * @param path
     * @param throwable
     */
    public abstract void onDownFailed(String url, String path, Throwable throwable);

    /**
     * 任务已存在
     *
     * @param url
     * @param path
     */
    public abstract void onTaskExist(String url, String path);

    /**
     * 下载完成
     *
     * @param url
     * @param path
     */
    public abstract void onDownComplete(String url, String path);

    /**
     * 进度更新
     *
     * @param url
     * @param path
     * @param progress
     */
    public abstract void onDownProgressUpdate(String url, String path, int progress);


}
