package com.acmo0.youtubedownloader;
import static android.content.Context.NOTIFICATION_SERVICE;
import static android.os.Build.ID;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Config;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.NotificationManagerCompat.*;
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
        makeNotificationChannel("CHANNEL", "Download status", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(getApplicationContext());
        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(getApplicationContext(), "CHANNEL");

        if(!downloadDirectory.exists()){
            success = downloadDirectory.mkdirs();
            if(!success){
                notifyFail(mNotificationManager, notifBuilder);
                return Result.failure();
            }
        }



        Python py = Python.getInstance();
        PyObject pyf = py.getModule("downloader");
        PyObject sys = py.getModule("sys");
        PyObject downloader;
        //try {

        notifyDownloading(mNotificationManager, notifBuilder, "Downloading");
        downloader = pyf.callAttr("download", videoUrl, format, directory, maxQuality);
        /*}catch (Error e) {
            notify("Error while downloading");
            return Result.failure();
        }*/
        if(downloader.toBoolean() == false){
            notifyFail(mNotificationManager, notifBuilder);
            return Result.failure();
        }
        else{
            notifySuccess(mNotificationManager, notifBuilder);
            return Result.success();
        }
    }
    void notifyDownloading(NotificationManagerCompat mNotificationManager, NotificationCompat.Builder notifBuilder, String text){

        notifBuilder
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Youtube Downloader")
                .setContentText(text)
                .setProgress(0, 0, true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        mNotificationManager.notify(1, notifBuilder.build());
    }
    void notifyFail(NotificationManagerCompat mNotificationManager, NotificationCompat.Builder notifBuilder){
        notifBuilder.setContentText("Sorry, error while downloading...").setProgress(0,0,false);
        mNotificationManager.notify(1, notifBuilder.build());
    }
    void notifySuccess(NotificationManagerCompat mNotificationManager, NotificationCompat.Builder notifBuilder){
        notifBuilder.setContentText("Download finished !").setProgress(0,0,false);
        mNotificationManager.notify(1, notifBuilder.build());
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    void makeNotificationChannel(String id, String name, int importance)
    {
        NotificationChannel channel = new NotificationChannel(id, name, importance);

        NotificationManager notificationManager =
                (NotificationManager)getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

        assert notificationManager != null;
        notificationManager.createNotificationChannel(channel);
    }
}
