package com.acmo0.youtubedownloader;

import static com.acmo0.youtubedownloader.DownloaderWorker.DIRECTORY;
import static com.acmo0.youtubedownloader.DownloaderWorker.FORMAT;
import static com.acmo0.youtubedownloader.DownloaderWorker.MAX_QUALITY;
import static com.acmo0.youtubedownloader.DownloaderWorker.VIDEO_URL;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.R)
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    Button buttonDownload;
    EditText editTextLink;
    AutoCompleteTextView editTextDirectory;
    RadioGroup radioLayout;
    RadioButton radioButton720p;
    RadioButton radioButton480p;
    RadioButton radioButton360p;
    RadioButton radioButtonAudio;
    ProgressBar progressBar;
    Button StopDownloadButton;
    TextView textViewWait;
    Button infoButton;
    Button infoButton2;
    String[] availableDirectories = {Environment.getExternalStorageDirectory().getPath()+"/Download/"};
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.configureToolBar();
        this.configureDrawerLayout();
        this.configureNavigationView();

        Bundle extras = getIntent().getExtras();
        String sharedUrl = "";
        if(extras!=null) {
            sharedUrl = extras.getString(Intent.EXTRA_TEXT);
        }

        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
        LifecycleOwner me = this;
        buttonDownload = findViewById(R.id.buttonDownload);
        infoButton = findViewById(R.id.infoButton);
        infoButton2 = findViewById(R.id.infoButton2);
        editTextLink = findViewById(R.id.editTextLink);
        editTextDirectory = (AutoCompleteTextView) findViewById(R.id.editTextDirectory);
        radioButton720p = findViewById(R.id.radioButton720);
        radioButton480p = findViewById(R.id.radioButton480);
        radioButton360p = findViewById(R.id.radioButton360);
        radioButtonAudio = findViewById(R.id.radioButtonAudio);
        radioLayout = findViewById(R.id.radioLayout);
        textViewWait = findViewById(R.id.textViewWait);
        StopDownloadButton = findViewById(R.id.stopbutton);
        editTextLink.setText(sharedUrl);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String defaultValue = getResources().getString(R.string.directory_key_default);
        String last_directory = sharedPref.getString(getString(R.string.directory_key), defaultValue);
        editTextDirectory.setText(last_directory);
        if (!editTextLink.getText().toString().equals("")) {
            radioLayout.setAlpha(1);

            radioButton360p.setClickable(true);
            radioButton480p.setClickable(true);
            radioButton720p.setClickable(true);
            radioButtonAudio.setClickable(true);

            buttonDownload.setClickable(true);

        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, availableDirectories);
        editTextDirectory.setThreshold(0);
        editTextDirectory.setAdapter(arrayAdapter);

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayPopupWindow(editTextLink, getString(R.string.text_supported_websites),getString(R.string.text_tip_link));
            }
        });
        infoButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayPopupWindow(editTextDirectory, getString(R.string.text_directory_explanation),getString(R.string.text_tip_directory_explanation));
            }
        });

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
                    directories[0] = new File(Environment.getExternalStorageDirectory().getPath()+"/Download");
                }
                List<String> writableDirectories = new ArrayList<>();
                for(int i = 0; i<directories.length; i++){
                    if(directories[i].canWrite() || directories[i] == new File(Environment.getExternalStorageDirectory().getPath()) || directories[i] == new File("/storage/")){
                        writableDirectories.add(directories[i].toString());
                    }
                }
                String[] writableDirectoriesAvailable;
                writableDirectoriesAvailable = new String[writableDirectories.size()];
                writableDirectoriesAvailable = writableDirectories.toArray(writableDirectoriesAvailable);
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.select_dialog_item, writableDirectoriesAvailable);
                editTextDirectory.setThreshold(0);
                editTextDirectory.setAdapter(arrayAdapter);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        WorkManager downloaderWorkManager = WorkManager.getInstance(getApplicationContext());
        buttonDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    progressBar.setVisibility(View.VISIBLE);
                    textViewWait.setVisibility(View.VISIBLE);
                    textViewWait.setText(R.string.text_wait);
                    buttonDownload.setVisibility(View.INVISIBLE);

                    String videoUrl = editTextLink.getText().toString();

                    String directory = editTextDirectory.getText().toString();

                    SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(getString(R.string.directory_key),directory);
                    editor.apply();
                    if (directory.equals("")) {
                        directory = Environment.getExternalStorageDirectory().getPath()+"/Download/";
                        System.out.println("Null directory, selected :" + directory);
                    }
                    int optionId = radioLayout.getCheckedRadioButtonId();
                    String format = "unknow";
                    String maxQuality = "";
                    switch (optionId) {

                        case R.id.radioButton360:
                            maxQuality = "360p";
                            format = "mp4";
                            break;

                        case R.id.radioButton480:
                            maxQuality = "480p";
                            format = "mp4";
                            break;

                        case R.id.radioButton720:
                            maxQuality = "720p";
                            format = "mp4";
                            break;

                        case R.id.radioButtonAudio:
                            maxQuality = "";
                            format = "m4a";
                            break;
                    }
                    Data arguments = new Data.Builder()
                            .putString(FORMAT, format)
                            .putString(DIRECTORY, directory)
                            .putString(MAX_QUALITY, maxQuality)
                            .putString(VIDEO_URL, videoUrl).build();
                    StopDownloadButton.setVisibility(View.VISIBLE);

                    OneTimeWorkRequest downloaderWorkRequest = new OneTimeWorkRequest.Builder(DownloaderWorker.class).setInputData(arguments).build();

                    WorkManager.getInstance(getApplicationContext()).getWorkInfoByIdLiveData(downloaderWorkRequest.getId())
                            .observe(me, new Observer<WorkInfo>() {
                                @Override
                                public void onChanged(@Nullable WorkInfo workInfo) {
                                    if (workInfo.getState().equals(WorkInfo.State.ENQUEUED)) {
                                        textViewWait.setText(R.string.text_prepare_download);
                                    }
                                    if (workInfo.getState().equals(WorkInfo.State.RUNNING)) {
                                        textViewWait.setText(R.string.text_downloading);
                                        cancelButton(downloaderWorkManager, downloaderWorkRequest.getId());
                                    }
                                    if (workInfo.getState().equals(WorkInfo.State.SUCCEEDED)) {
                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.text_download_suceedeed), Toast.LENGTH_SHORT).show();
                                        textViewWait.setVisibility(View.INVISIBLE);
                                        progressBar.setVisibility(View.INVISIBLE);
                                        buttonDownload.setVisibility(View.VISIBLE);
                                        StopDownloadButton.setVisibility(View.GONE);
                                    }
                                    if (workInfo.getState().equals(WorkInfo.State.FAILED)) {
                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.text_download_fail), Toast.LENGTH_SHORT).show();
                                        textViewWait.setVisibility(View.INVISIBLE);
                                        progressBar.setVisibility(View.INVISIBLE);
                                        buttonDownload.setVisibility(View.VISIBLE);
                                        StopDownloadButton.setVisibility(View.GONE);
                                    }
                                }
                            });
                    downloaderWorkManager.enqueue(downloaderWorkRequest);
                }
                else if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.title_permission_needed)
                            .setMessage(R.string.text_why_permission)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    requestStoragePermission();
                                }
                            })
                            .setNegativeButton(R.string.text_cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .create().show();
                }
                else{
                    requestStoragePermission();
                }
            }

        });

    }
    @Override
    protected void onResume() {
        Bundle extras = getIntent().getExtras();
        String sharedUrl = "";
        if(extras!=null) {
            sharedUrl = extras.getString(Intent.EXTRA_TEXT);
        }
        editTextLink.setText(sharedUrl);
        super.onResume();

    }
    @Override
    public void onBackPressed(){
        if(this.drawerLayout.isDrawerOpen(GravityCompat.START)){
            this.drawerLayout.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }
    }
    private void cancelButton(WorkManager worker, UUID workId){
        this.StopDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                worker.cancelWorkById(workId);
                textViewWait.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                buttonDownload.setVisibility(View.VISIBLE);
                StopDownloadButton.setVisibility(View.GONE);
            }
        });
    }
    private void configureToolBar(){
        this.toolBar = (Toolbar) findViewById(R.id.myToolBar);

        setSupportActionBar(toolBar);
    }
    private void configureDrawerLayout() {
        this.drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private  void configureNavigationView(){
        this.navigationView = (NavigationView) findViewById(R.id.main_layout_nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if(requestCode == 1){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                buttonDownload.performClick();
            }else{
                Toast.makeText(this, "Storage permission is required.\nSee app parameters to enable Storage permission.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item){
        int id = item.getItemId();
        switch (id){
            case R.id.about_item:
                Intent infoActivityIntent = new Intent(getApplicationContext(), InfoActivity.class);
                startActivity(infoActivityIntent);
                break;
            case R.id.github_item:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/acmo0/Android-Youtube-Downloader"));
                startActivity(browserIntent);
                break;
            default:
                break;
        }
        this.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    private void displayPopupWindow(View anchorView, String text, String tip) {
        PopupWindow popup = new PopupWindow(MainActivity.this);
        View layout = getLayoutInflater().inflate(R.layout.popup_layout, null);
        popup.setContentView(layout);
        // Set content width and height
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        TextView messageTextView = layout.findViewById(R.id.tvCaption);
        TextView tipTextView = layout.findViewById(R.id.tip);
        ImageView tipIcon = layout.findViewById(R.id.imageView2);
        messageTextView.setText(text);
        tipTextView.setText(tip);
        if(tip.equals("")){
            tipTextView.setVisibility(View.INVISIBLE);
            tipIcon.setVisibility(View.INVISIBLE);
        }
        // Closes the popup window when touch outside of it - when looses focus
        popup.setOutsideTouchable(true);
        popup.setFocusable(true);
        // Show anchored to button
        popup.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_background));
        popup.showAsDropDown(anchorView);
    }
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }

}
