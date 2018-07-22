package com.beta1.memories.beta3_music_player.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.beta1.memories.beta3_music_player.R;
import com.beta1.memories.beta3_music_player.Song;

import java.util.ArrayList;

public class BaseSongAdapter extends BaseAdapter {

    Context context;
    ArrayList<Song> arSong;

    public BaseSongAdapter(Context context, ArrayList<Song> arSong) {
        this.context = context;
        this.arSong = arSong;
    }

    @Override
    public int getCount() {
        return arSong.size();
    }

    @Override
    public Object getItem(int position) {
        return arSong.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_song, parent, false);
        }

        Song currSong = arSong.get(position);

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvArtist = view.findViewById(R.id.tvArtist);
        ImageView ivSong = view.findViewById(R.id.ivSong);

        view.setTag(position);

        tvTitle.setText(currSong.getTitle());
        tvArtist.setText(currSong.getArtist());
        ivSong.setImageBitmap(currSong.getAlbumB());

        return view;
    }
}
