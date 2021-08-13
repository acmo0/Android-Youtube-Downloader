package com.acmo0.youtubedownloader;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class InfoActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolBar;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_layout);
        this.configureToolBar();
        this.configureDrawerLayout();
        this.configureNavigationView();
        this.configureTextView();

    }
    @Override
    public boolean onNavigationItemSelected(MenuItem item){
        int id = item.getItemId();
        switch (id){
            case R.id.activity_main_drawer_download:
                finish();
                break;
            case R.id.about_item:
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
    private void configureToolBar(){
        this.toolBar = (Toolbar) findViewById(R.id.myToolBar);
        setSupportActionBar(toolBar);
    }
    private void configureDrawerLayout() {
        this.drawerLayout = (DrawerLayout) findViewById(R.id.drawer_info_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private  void configureNavigationView(){
        this.navigationView = (NavigationView) findViewById(R.id.main_layout_nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }
    private void configureTextView(){
        TextView infoTV = findViewById(R.id.infoTV);
        infoTV.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
