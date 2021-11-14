package com.acmo0.youtubedownloader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import static com.acmo0.youtubedownloader.DownloaderWorker.downloader;

public class StopDownloadBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        downloader.put("stop",true);
        System.out.println("Clicked");
    }
}
