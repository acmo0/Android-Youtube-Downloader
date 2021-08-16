package com.acmo0.youtubedownloader;
import static android.content.Context.NOTIFICATION_SERVICE;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;

import java.io.File;

import io.reactivex.annotations.NonNull;

public class DownloaderWorker extends Worker {

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
        boolean success;
        makeNotificationChannel("CHANNEL", "Download status", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(getApplicationContext());
        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(getApplicationContext(), "CHANNEL");


        Intent activityIntent = new Intent(getApplicationContext(), MainActivity.class);
        activityIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        activityIntent.setClass(getApplicationContext(), MainActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addNextIntentWithParentStack(activityIntent);
        PendingIntent activityPendingIntent = PendingIntent.getActivity(getApplicationContext(),0,activityIntent, 0);

        if(!downloadDirectory.exists()){
            success = downloadDirectory.mkdirs();
            if(!success){
                notifyFail(mNotificationManager, notifBuilder, activityPendingIntent);
                return Result.failure();
            }
        }

        Python py = Python.getInstance();
        PyObject pyf = py.getModule("downloader");
        PyObject downloader;


        notifyDownloading(mNotificationManager, notifBuilder, getApplicationContext().getResources().getString(R.string.text_notif_downloading), activityPendingIntent);
        downloader = pyf.callAttr("download", videoUrl, format, directory, maxQuality, Environment.getExternalStorageDirectory().getPath()+"/Android/data/com.acmo0.youtubedownloader");

        if(downloader.toBoolean() == false){
            notifyFail(mNotificationManager, notifBuilder, activityPendingIntent);
            return Result.failure();
        }
        else{
            notifySuccess(mNotificationManager, notifBuilder, activityPendingIntent);
            return Result.success();
        }
    }
    void notifyDownloading(NotificationManagerCompat mNotificationManager, NotificationCompat.Builder notifBuilder, String text, PendingIntent mPendingIntent){
        notifBuilder
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Video Downloader")
                .setContentText(text)
                .setProgress(0, 0, true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        mNotificationManager.notify(1, notifBuilder.build());
    }
    void notifyFail(NotificationManagerCompat mNotificationManager, NotificationCompat.Builder notifBuilder, PendingIntent mPendingIntent){
        notifBuilder.setContentIntent(mPendingIntent).setSmallIcon(R.mipmap.ic_launcher).setContentText(getApplicationContext().getResources().getString(R.string.text_notif_download_error)).setProgress(0,0,false);
        mNotificationManager.notify(1, notifBuilder.build());
    }
    void notifySuccess(NotificationManagerCompat mNotificationManager, NotificationCompat.Builder notifBuilder, PendingIntent mPendingIntent){
        notifBuilder.setContentIntent(mPendingIntent).setSmallIcon(R.mipmap.ic_launcher).setContentText(getApplicationContext().getResources().getString(R.string.text_notif_download_finished)).setProgress(0,0,false);
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
