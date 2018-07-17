package com.beta1.memories.beta3_music_player;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContentMusic extends AppCompatActivity {

    private ImageButton btnPlay, btnPrev, btnNext;
    private SeekBar seekBarSong;

    //private Intent intentMainControl;
    //private boolean playing = false;
    //private int positionSong = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_music);

        btnPlay = findViewById(R.id.btnPlay);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        seekBarSong = findViewById(R.id.seekBarSong);

        //intentMainControl = new Intent(MainActivity.BROADCAST_ACTION);

        btnPlay.setOnClickListener(new eventButton());
        btnPrev.setOnClickListener(new eventButton());
        btnNext.setOnClickListener(new eventButton());

        if (!MainActivity.mSongService.isPlaying() && !getIntent().hasExtra("startPlay")) {
            btnPlay.setImageResource(R.drawable.ic_action_play);
        }

        setContentSong();
        //startSong(MainActivity.START_G_SONG);
    }

    private class eventButton implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnPlay:
                    if (MainActivity.mSongService.isPlaying()) {
                        MainActivity.mSongService.pausePlayer();
                        btnPlay.setImageResource(R.drawable.ic_action_play);
                    } else {
                        MainActivity.mSongService.startPlay();
                        btnPlay.setImageResource(R.drawable.ic_action_pause);
                    }
                    break;
                case R.id.btnPrev:
                    if (!MainActivity.mSongService.isPlaying()) {
                        btnPlay.setImageResource(R.drawable.ic_action_pause);
                    }
                    MainActivity.mSongService.prevSong();
                    setContentSong();
                    break;
                case R.id.btnNext:
                    if (!MainActivity.mSongService.isPlaying()) {
                        btnPlay.setImageResource(R.drawable.ic_action_pause);
                    }
                    MainActivity.mSongService.nextSong();
                    setContentSong();
                    break;
            }
        }
    }

/*    void putIntentCommand(String command) {
        intentMainControl.putExtra("command", command);
        sendBroadcast(intentMainControl);
    }

    void startSong(String command) {
        playing = true;
        putIntentCommand(command);
        btnPlay.setImageResource(R.drawable.ic_action_pause);
    }

    void stopSong() {
        playing = false;
        putIntentCommand(MainActivity.STOP_SONG);
        btnPlay.setImageResource(R.drawable.ic_action_play);
    }

    void playControl() {
        if (playing) {
            stopSong();
        } else {
            startSong(MainActivity.START_SONG);
        }
    }

    void prevSong() {
        if (positionSong > 0) {
            positionSong --;
        } else {
            positionSong = MainActivity.songList.size() - 1;
        }
        setContentSong();
        putIntentCommand(MainActivity.PREV_SONG);
    }

    void nextSong() {
        int sizeSong = MainActivity.songList.size() - 1;
        if (sizeSong == 0) {
            return;
        }
        if (positionSong < sizeSong) {
            positionSong ++;
        } else {
            positionSong = 0;
        }
        setContentSong();
        putIntentCommand(MainActivity.NEXT_SONG);
    }*/

    private void setContentSong() {
        Song arSong = MainActivity.mSongService.getList();

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

        seekBarSong.setMax(MainActivity.mSongService.getDuration() / 1000);

    }
}
