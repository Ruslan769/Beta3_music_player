package com.beta1.memories.beta3_music_player;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContentMusic extends AppCompatActivity {

    ImageButton btnPlay, btnPrev, btnNext;
    SeekBar seekBarSong;

    SongService mSongService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_music);

        btnPlay = findViewById(R.id.btnPlay);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        seekBarSong = findViewById(R.id.seekBarSong);

        dataExtra();
        mSongService.playSong();
    }

    private void dataExtra() {
        Bundle bundle = getIntent().getExtras();
        mSongService = bundle.getParcelable("service");

        Song arSong = mSongService.getList();

        Bitmap albumB = arSong.getAlbumB();
        String artist = arSong.getArtist();
        String title = arSong.getTitle();

        ImageView imgContentAlbum = findViewById(R.id.imgContentAlbum);
        imgContentAlbum.setImageBitmap(albumB);
        imgContentAlbum.setColorFilter(R.color.filterContentImage);

        CircleImageView imgContentAlbumMin = findViewById(R.id.imgContentAlbumMin);
        imgContentAlbumMin.setImageBitmap(albumB);

        TextView tvArtistNameContent = findViewById(R.id.tvArtistNameContent);
        tvArtistNameContent.setText(artist);

        TextView tvSongNameContent = findViewById(R.id.tvSongNameContent);
        tvSongNameContent.setText(title);
    }
}
