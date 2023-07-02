package com.example.music;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    MusicService musicService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //侧边栏显示内容
        NavigationView navigationView = findViewById(R.id.nav_view);
        View inflateHeaderView = navigationView.inflateHeaderView(R.layout.header_navigation_drawer);
        final TextView usernameInfo = inflateHeaderView.findViewById(R.id.usernameInfo);
        final TextView emailInfo = inflateHeaderView.findViewById(R.id.emailInfo);
        Button play=findViewById(R.id.play);
        Button prev=findViewById(R.id.prev);
        Button next=findViewById(R.id.next);
        usernameInfo.setText(getSharedPreferences("LoginActivity", 0).getString("username", "username"));
        emailInfo.setText(getSharedPreferences("LoginActivity", 0).getString("email", "email"));

        //点击按钮弹出菜单栏
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(Gravity.LEFT));

        //标签页
        getSupportFragmentManager().beginTransaction().replace(R.id.detail, HomeFragment.newInstance()).commit();
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.detail, HomeFragment.newInstance()).commit();
                }
                if (tab.getPosition() == 1) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.detail, MyFragment.newInstance()).commit();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        //进入播放页
        ImageView imageView = findViewById(R.id.imageView);
        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
            intent.putExtra("status",play.getText());
            startActivity(intent);
        });
        ConstraintLayout constraintLayout = findViewById(R.id.constraintLayout);
        constraintLayout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
            intent.putExtra("status",play.getText());
            startActivity(intent);
        });

        //权限
        if (this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            //如果应用之前请求过此权限但用户拒绝了请求，此方法将返回 true。if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
            //这里可以写个对话框之类的
        }else {
            //申请权限，字符串数组内是一个或多个要申请的权限，1是申请权限结果的返回参数，在onRequestPermissionsResult可以得知申请结果
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }

        //默认播放模式
        final int playMode = getSharedPreferences("PlayerActivity", 0).getInt("playMode", 3);
        if(playMode==3){
            SharedPreferences.Editor playerActivity = getSharedPreferences("PlayerActivity", 0).edit();
            playerActivity.putInt("playMode", 0);
            playerActivity.apply();
        }



        //播放控制
        Intent intent = new Intent(MainActivity.this, MusicService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        play.setOnClickListener(v -> {
            if(musicService.getStatus()){
                musicService.pause();
                play.setText("播放");
            }else{
                musicService.play();
                play.setText("暂停");
            }
        });
        prev.setOnClickListener(v -> {
            ContentResolver resolver = getContentResolver();
            Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media._ID);
            String lastPlayedPath = getSharedPreferences("MainActivity", 0).getString("lastPath", "");//最后播放路径
            String newPath = "";//下次播放路径
            cursor.moveToFirst();
            String firstPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));//所有列表里的第一首
            if (firstPath.equals(lastPlayedPath)) {
                cursor.moveToLast();
                newPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
            } else {
                String tempPath = firstPath;
                while (cursor.moveToNext()) {
                    String nextPath=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    if (lastPlayedPath.equals(nextPath)) {
                        newPath = tempPath;
                        break;
                    } else {
                        tempPath=nextPath;
                    }
                }
            }
            cursor.close();
            musicService.init(newPath);
            SharedPreferences.Editor editor = getSharedPreferences("MainActivity", 0).edit();
            editor.putString("lastPath", newPath);
            editor.apply();
            play.setText("暂停");
            musicService.play();
        });
        next.setOnClickListener(v -> {
            ContentResolver resolver = getContentResolver();
            Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media._ID);
            String lastPlayedPath = getSharedPreferences("MainActivity", 0).getString("lastPath", "");//最后播放路径
            String newPath = "";//下次播放路径
            cursor.moveToLast();
            String LastPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));//所有列表里的最后一首
            if (LastPath.equals(lastPlayedPath)) {
                cursor.moveToFirst();
                newPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
            } else {
                String tempPath = LastPath;
                while (cursor.moveToPrevious()) {
                    String prevPath=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    if (lastPlayedPath.equals(prevPath)) {
                        newPath = tempPath;
                        break;
                    } else {
                        tempPath=prevPath;
                    }
                }
            }
            cursor.close();
            musicService.init(newPath);
            SharedPreferences.Editor editor = getSharedPreferences("MainActivity", 0).edit();
            editor.putString("lastPath", newPath);
            editor.apply();
            play.setText("暂停");
            musicService.play();
        });




    }
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
        }
    };

    @Override
    protected void onDestroy() {
        unbindService(serviceConnection);
        super.onDestroy();
    }
}