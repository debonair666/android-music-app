package com.example.music;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {


    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView ;
        TextView name ;
        TextView singer ;
        TextView time ;
        String path;
        public ViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.image);
            name = view.findViewById(R.id.name);
            singer = view.findViewById(R.id.singer);
            time = view.findViewById(R.id.time);
        }
    }
    private final List<Song> mDatas;
    private final Context context;
    public RecyclerAdapter(Context context, List<Song> mDatas,OnItemClickListener onItemClickListener) {
        this.context = context;
        this.mDatas = mDatas;
        this.onItemClickListener=onItemClickListener;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.song_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song data =mDatas.get(position);
        holder.name.setText(data.getName());
        holder.singer.setText(data.getArtist());
        holder.path=data.getPath();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        holder.time.setText(formatter.format(data.getDuration()- TimeZone.getDefault().getRawOffset()));
        holder.itemView.setOnClickListener(v -> {
            onItemClickListener.OnItemClick(v,data);
        });
    }


    @Override
    public int getItemCount() {
        return mDatas.size();
    }


    public interface OnItemClickListener {

        void OnItemClick(View view, Song data);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }


}
