package com.example.music;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;

import java.io.IOException;
import java.util.Random;

public class MusicService extends Service {
    public static MediaPlayer mp = new MediaPlayer();
    private Binder musicBinder = new MusicBinder();

    public MusicService() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        return musicBinder;
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    public void init(String path) {
        try {
            mp.reset();
            mp.setDataSource(path);
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void play() {

        int playMode = getSharedPreferences("PlayerActivity", 0).getInt("playMode", 0);
        mp.setOnCompletionListener(mp1 -> {
            if (playMode == 0) {
                mp.start();
            }
            if (playMode == 1) {
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
                this.init(newPath);
                SharedPreferences.Editor editor = getSharedPreferences("MainActivity", 0).edit();
                editor.putString("lastPath", newPath);
                editor.apply();
                this.play();
            }
            if (playMode == 2) {
                ContentResolver resolver = getContentResolver();
                Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media._ID);
                String newPath = "";//下次播放路径
                int count = cursor.getCount();
                int position = new Random().nextInt(count-1);
                cursor.moveToPosition(position);
                newPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                cursor.close();
                this.init(newPath);
                SharedPreferences.Editor editor = getSharedPreferences("MainActivity", 0).edit();
                editor.putString("lastPath", newPath);
                editor.apply();
                this.play();
            }
        });
        mp.start();
    }

    public void pause() {
        mp.pause();
    }


    public boolean getStatus() {
        return mp.isPlaying();
    }


}