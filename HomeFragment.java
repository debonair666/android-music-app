package com.example.music;

import static android.content.Context.BIND_AUTO_CREATE;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    MusicService musicService;
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
    public void onStart() {

        super.onStart();
        Intent intent = new Intent(getContext(), MusicService.class);
        getActivity().bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        // 音乐列表
        List<Song> list = new ArrayList<>();

        ContentResolver resolver = getActivity().getContentResolver();
        Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media._ID);
        while (cursor.moveToNext()) {
                String displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                Integer duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                String isFavorite = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_FAVORITE));
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
//                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
                Song song = new Song();
                song.setName(displayName);
                song.setDuration(duration);
                song.setArtist(artist);
                song.setIsFavourite(isFavorite);
                song.setPath(path);
                list.add(song);
        }
        cursor.close();
        RecyclerView recyclerView = getActivity().findViewById(R.id.songList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        RecyclerAdapter.OnItemClickListener clickListener=new RecyclerAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, Song data) {
            //这里要把歌曲信息广播出去，同时修改两处歌曲信息

                Intent intent = new Intent(getContext(), MusicService.class);
                boolean bound = getActivity().bindService(intent, serviceConnection, BIND_AUTO_CREATE);
                if(bound){
                    musicService.init(data.getPath());
                    SharedPreferences.Editor editor = getActivity().getPreferences(0).edit();
                    editor.putString("lastPath", data.getPath());
                    editor.apply();
                    Button play= getActivity().findViewById(R.id.play);
                    play.setText("暂停");
                    musicService.play();

                }else{
                    Toast.makeText(getContext(), "bind service failed", Toast.LENGTH_SHORT).show();
                }

            }
        };
        RecyclerAdapter recyclerAdapter = new RecyclerAdapter(getContext(), list,clickListener);
        recyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
}