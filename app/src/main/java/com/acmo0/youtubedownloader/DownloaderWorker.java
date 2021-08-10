package com.acmo0.youtubedownloader;
import android.content.Context;

import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;

import java.io.File;

import io.reactivex.annotations.NonNull;

public class DownloaderWorker extends Worker {
    public static final String RETURN_VALUE = "RETURN VALUE";
    public static final String FORMAT = "FORMAT";
    public static final String DIRECTORY = "DIRECTORY";
    public static final String MAX_QUALITY = "MAX_QUALITY";
    public static final String VIDEO_URL = "VIDEO_URL";
    public DownloaderWorker(@NonNull Context context, @NonNull WorkerParameters params){
        super(context, params);
    }
    @Override
    public Result doWork(){
        String format = getInputData().getString(FORMAT);
        String directory = getInputData().getString(DIRECTORY);
        String maxQuality = getInputData().getString(MAX_QUALITY);
        String videoUrl = getInputData().getString(VIDEO_URL);
        File downloadDirectory = new File(directory);
        boolean success = true;

        if(!downloadDirectory.exists()){
            success = downloadDirectory.mkdirs();
            if(!success){
                notify("Download failed");
                return Result.failure();
            }
        }

        Python py = Python.getInstance();
        PyObject pyf = py.getModule("downloader");
        PyObject sys = py.getModule("sys");
        PyObject downloader;
        //try {
        downloader = pyf.callAttr("download", videoUrl, format, directory, maxQuality);
        /*}catch (Error e) {
            notify("Error while downloading");
            return Result.failure();
        }*/
        notify(downloader.toString());
        if(downloader.toBoolean() == false){
            return Result.failure();
        }
        else{
            notify("Download successes");
            return Result.success();
        }
    }
    void notify(String text){
        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(getApplicationContext(), "downloader-yt")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Youtube Downloader")
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
    }
}
