package com.example.music;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.Slider;

import java.util.Timer;
import java.util.TimerTask;

public class PlayerActivity extends AppCompatActivity {
    MusicService musicService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        //绑定service
        Intent intent = new Intent(PlayerActivity.this, MusicService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);

//        TextView songName=findViewById(R.id.songName);
        Button back = findViewById(R.id.back);
        Button favorite = findViewById(R.id.like);
        Button mode = findViewById(R.id.mode);
        Button prev = findViewById(R.id.prev);
        Button next = findViewById(R.id.next);
        Button play = findViewById(R.id.player_play);

        int playMode = getSharedPreferences("PlayerActivity", 0).getInt("playMode", 0);
        if (playMode == 0) {
            mode.setText("单曲");
        }
        if (playMode == 1) {
            mode.setText("列表");
        }
        if (playMode == 2) {
            mode.setText("随机");
        }

        Slider slider = findViewById(R.id.slider);
        slider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {

            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                float sliderValue = slider.getValue() / 100;
                int musicValue = Math.round(sliderValue * musicService.mp.getDuration());
                musicService.mp.seekTo(musicValue);
            }
        });

        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    Float value = (float) msg.obj;
                    if (value > 1) {
                        slider.setValue(100);
                    } else {
                        slider.setValue((float) msg.obj * 100);
                    }

                }
                super.handleMessage(msg);
            }
        };

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (musicService.mp.isPlaying()) {
                    int currentPosition = musicService.mp.getCurrentPosition();
                    int duration = musicService.mp.getDuration();
                    float v = (float) (currentPosition / (duration * 1.0));
                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = v;
                    handler.sendMessage(msg);
                }
            }
        };
        timer.schedule(timerTask, 0, 10);


        back.setOnClickListener(v -> {
            finish();
        });
        play.setText(getIntent().getStringExtra("status"));


        favorite.setOnClickListener(v -> {
            Toast.makeText(this, "favorite", Toast.LENGTH_SHORT).show();
        });
        mode.setOnClickListener(v -> {
            SharedPreferences.Editor editor = getSharedPreferences("PlayerActivity", 0).edit();
            int nextPlayMode = 0;

            String modeType = mode.getText().toString();
            if (modeType.equals("单曲")) {
                mode.setText("列表");
                nextPlayMode = 1;
            } else if (modeType.equals("列表")) {
                mode.setText("随机");
                nextPlayMode = 2;
            } else {
                mode.setText("单曲");
            }
            editor.putInt("playMode", nextPlayMode);
            editor.apply();
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
                    String nextPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    if (lastPlayedPath.equals(nextPath)) {
                        newPath = tempPath;
                        break;
                    } else {
                        tempPath = nextPath;
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
                    String prevPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    if (lastPlayedPath.equals(prevPath)) {
                        newPath = tempPath;
                        break;
                    } else {
                        tempPath = prevPath;
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
        play.setOnClickListener(v -> {
            if (musicService.getStatus()) {
                musicService.pause();
                play.setText("播放");
            } else {
                musicService.play();
                play.setText("暂停");
            }
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
        super.onDestroy();
        unbindService(serviceConnection);
    }


//        Button start = findViewById(R.id.start);

//        ProgressBar progressBar = findViewById(R.id.playProgress);
//        textView = findViewById(R.id.name);

//        start.setOnClickListener(v -> {
//            musicService.play();
//
//            progressBar.setMax(MusicService.mp.getDuration());
//            Timer timer = new Timer();
//            TimerTask timerTask = new TimerTask() {
//                @Override
//                public void run() {
//                    progressBar.setProgress(MusicService.mp.getCurrentPosition());
//                }
//            };
//            timer.schedule(timerTask, 0, 10);
//
//        });

}