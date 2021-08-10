package com.acmo0.youtubedownloader;

import static com.acmo0.youtubedownloader.DownloaderWorker.DIRECTORY;
import static com.acmo0.youtubedownloader.DownloaderWorker.FORMAT;
import static com.acmo0.youtubedownloader.DownloaderWorker.MAX_QUALITY;
import static com.acmo0.youtubedownloader.DownloaderWorker.VIDEO_URL;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.R)
public class MainActivity extends AppCompatActivity {
    public static Button buttonDownload;
    EditText editTextLink;
    AutoCompleteTextView editTextDirectory;
    RadioGroup radioLayout;
    RadioButton radioButton720p;
    RadioButton radioButton480p;
    RadioButton radioButton360p;
    RadioButton radioButtonAudio;
    public static ProgressBar progressBar;
    public static TextView textViewWait;
    String[] availableDirectories = {"/sdcard/Download/"};
    Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle extras = getIntent().getExtras();
        String sharedUrl = "";
        if(extras!=null) {
            sharedUrl = extras.getString(Intent.EXTRA_TEXT);
        }

        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
        String sdf = new String(Environment.getExternalStorageDirectory().getName());
        String sddir = new String(Environment.getExternalStorageDirectory().getPath().replace(sdf,""));
        System.out.println("STORAGE :" + sddir);
        LifecycleOwner me = this;
        buttonDownload = findViewById(R.id.buttonDownload);
        editTextLink = findViewById(R.id.editTextLink);
        editTextDirectory = (AutoCompleteTextView) findViewById(R.id.editTextDirectory);
        radioButton720p = findViewById(R.id.radioButton720);
        radioButton480p = findViewById(R.id.radioButton480);
        radioButton360p = findViewById(R.id.radioButton360);
        radioButtonAudio = findViewById(R.id.radioButtonAudio);
        radioLayout = findViewById(R.id.radioLayout);
        textViewWait = findViewById(R.id.textViewWait);

        editTextLink.setText(sharedUrl);
        if (!editTextLink.getText().toString().equals("")) {
            radioLayout.setAlpha(1);

            radioButton360p.setClickable(true);
            radioButton480p.setClickable(true);
            radioButton720p.setClickable(true);
            radioButtonAudio.setClickable(true);

            buttonDownload.setClickable(true);

        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, availableDirectories);
        editTextDirectory.setThreshold(0);
        editTextDirectory.setAdapter(arrayAdapter);


        this.progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        editTextLink.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (!editTextLink.getText().toString().equals("")) {
                    radioLayout.setAlpha(1);

                    radioButton360p.setClickable(true);
                    radioButton480p.setClickable(true);
                    radioButton720p.setClickable(true);
                    radioButtonAudio.setClickable(true);

                    buttonDownload.setClickable(true);

                } else {
                    radioLayout.setAlpha((float) 0.1);
                    radioButton360p.setClickable(false);
                    radioButton480p.setClickable(false);
                    radioButton720p.setClickable(false);
                    radioButtonAudio.setClickable(false);

                    buttonDownload.setClickable(false);
                }
            }
        });

        editTextDirectory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i0, int i1, int i2) {
                String path = editTextDirectory.getText().toString();
                System.out.println("PATH :"+path);
                File[] directories;
                path = path.substring(0,path.lastIndexOf("/")+1);
                if(path.equals("")){
                    path = "/";
                }
                System.out.println("PATH 2 : "+path);
                directories = new File(path).listFiles(File::isDirectory);
                if(directories == null){
                    directories = new File[1];
                    directories[0] = new File("/sdcard/Download");
                }
                List<String> writableDirectories = new ArrayList<String>();
                for(int i = 0; i<directories.length; i++){
                    if(directories[i].canWrite() || directories[i] == new File("/sdcard/") || directories[i] == new File("/storage/")){
                        writableDirectories.add(directories[i].toString());
                    }
                }
                String[] writableDirectoriesAvailable;
                if(writableDirectories!= null) {
                    writableDirectoriesAvailable = new String[writableDirectories.size()];
                    writableDirectoriesAvailable = writableDirectories.toArray(writableDirectoriesAvailable);
                }else{
                    writableDirectoriesAvailable = new String[0];
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.select_dialog_item, writableDirectoriesAvailable);
                editTextDirectory.setThreshold(0);
                editTextDirectory.setAdapter(arrayAdapter);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        WorkManager downloaderWorkManager = WorkManager.getInstance();
        OneTimeWorkRequest downloaderWorkRequest = new OneTimeWorkRequest.Builder(DownloaderWorker.class).build();
        System.out.println(this);
        buttonDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                textViewWait.setVisibility(View.VISIBLE);
                textViewWait.setText("Please wait, downloading...");
                buttonDownload.setVisibility(View.INVISIBLE);

                String videoUrl = editTextLink.getText().toString();
                String directory = editTextDirectory.getText().toString();
                if (directory.equals("")){
                    directory = "/sdcard/Download/";
                }
                int optionId = radioLayout.getCheckedRadioButtonId();
                String format = "unknow";
                String maxQuality = "";
                switch (optionId){

                    case R.id.radioButton360:
                        maxQuality = "360";
                        format = "mp4";
                        break;

                    case R.id.radioButton480:
                        maxQuality = "480";
                        format = "mp4";
                        break;

                    case R.id.radioButton720:
                        maxQuality = "720";
                        format = "mp4";
                        break;

                    case R.id.radioButtonAudio:
                        maxQuality = "";
                        format = "m4a";
                        break;
                }
                Data arguments = new Data.Builder().putString(FORMAT, format).putString(DIRECTORY, directory).putString(MAX_QUALITY, maxQuality).putString(VIDEO_URL, videoUrl).build();
                OneTimeWorkRequest downloaderWorkRequest = new OneTimeWorkRequest.Builder(DownloaderWorker.class).setInputData(arguments).build();
                WorkManager.getInstance().getWorkInfoByIdLiveData(downloaderWorkRequest.getId())
                        .observe( me, new Observer<WorkInfo>() {
                            @Override
                            public void onChanged(@Nullable WorkInfo workInfo) {
                                if(workInfo.getState().equals(WorkInfo.State.ENQUEUED)){
                                    textViewWait.setText("Please wait, preparing downloading...");
                                }
                                if(workInfo.getState().equals(WorkInfo.State.RUNNING)){
                                    textViewWait.setText("Please wait, downloading...");
                                }
                                if(workInfo.getState().equals(WorkInfo.State.SUCCEEDED)){
                                    Toast.makeText(getApplicationContext(), "Download succeeded", Toast.LENGTH_SHORT).show();
                                    textViewWait.setVisibility(View.INVISIBLE);
                                    progressBar.setVisibility(View.INVISIBLE);
                                    buttonDownload.setVisibility(View.VISIBLE);
                                }
                                if(workInfo.getState().equals(WorkInfo.State.FAILED)){
                                    Toast.makeText(getApplicationContext(), "Download failed", Toast.LENGTH_SHORT).show();
                                    textViewWait.setVisibility(View.INVISIBLE);
                                    progressBar.setVisibility(View.INVISIBLE);
                                    buttonDownload.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                downloaderWorkManager.enqueue(downloaderWorkRequest);

            }

        });

    }
}
