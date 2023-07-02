package com.example.music;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MyFragment extends Fragment {

    public static MyFragment newInstance() {
        MyFragment fragment = new MyFragment();
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        // 音乐列表
        List<Song> list = new ArrayList<>();

        ContentResolver resolver = getActivity().getContentResolver();
        Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        while (cursor.moveToNext()) {

            String displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
            Integer duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
            String isFavorite = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_FAVORITE));
            String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
//            String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH));
            Song song = new Song();
            song.setName(displayName);
            song.setDuration(duration);
            song.setArtist(artist);
            song.setIsFavourite(isFavorite);
            list.add(song);
        }
        cursor.close();
        RecyclerView recyclerView = getActivity().findViewById(R.id.myList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        RecyclerAdapter.OnItemClickListener clickListener=new RecyclerAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, Song data) {
                Toast.makeText(getContext(), "1", Toast.LENGTH_SHORT).show();
            }
        };
        RecyclerAdapter recyclerAdapter = new RecyclerAdapter(getContext(), list,clickListener);
        recyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_my, container, false);
    }

}