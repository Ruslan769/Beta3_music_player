package com.beta1.memories.beta3_music_player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContentMusic extends AppCompatActivity {

    private ImageButton btnPlay, btnPrev, btnNext;
    private TextView tvTimePassed, tvTimeLeft;
    private SeekBar seekBar;
    private int overflowcounter = 0;
    private boolean contentMusicPaused = true;

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            handleCommandIntent(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_music);

        final IntentFilter intentFilter = new IntentFilter(MusicService.BROADCAST_ACTION);
        // регистрируем (включаем) BroadcastReceiver
        registerReceiver(mIntentReceiver, intentFilter);

        btnPlay = findViewById(R.id.btnPlay);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        tvTimePassed = findViewById(R.id.tvTimePassed);
        tvTimeLeft = findViewById(R.id.tvTimeLeft);
        seekBar = findViewById(R.id.seekBarSong);

        btnPlay.setOnClickListener(new eventButton());
        btnPrev.setOnClickListener(new eventButton());
        btnNext.setOnClickListener(new eventButton());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    MusicPlayer.seek((long) i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        //boolean isPlay = false;
        if (getIntent().hasExtra("play")) {
            final int position = getIntent().getIntExtra("play", 0);
            /*Log.d("myLog", "position = " + position);
            Log.d("myLog", "old position = " + MusicPlayer.getPosition());*/
            if (MusicPlayer.getPosition() != position) {
                MusicPlayer.setPosition(position);
                MusicPlayer.play();
                return;
            }
        }

        setContentSong();
    }

    @Override
    public void onPause() {
        super.onPause();
        contentMusicPaused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (MusicPlayer.isPlaying()) {
            contentMusicPaused = false;
            seekBarStart();
        } else {
            setSeekBarText();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mIntentReceiver);
    }

    private void buttonChange() {
        if (MusicPlayer.isPlaying()) {
            buttonLogoPause();
        } else {
            buttonLogoPlay();
        }
    }

    private void buttonLogoPause() {
        btnPlay.setImageResource(R.drawable.ic_action_pause);
    }

    private void buttonLogoPlay() {
        btnPlay.setImageResource(R.drawable.ic_action_play);
    }

    private void seekBarStart() {
        if (seekBar != null) {
            seekBar.postDelayed(mUpdateProgress, 10);
        }
    }

    private class eventButton implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnPlay:
                    MusicPlayer.playOrPause();
                    break;
                case R.id.btnPrev:
                    MusicPlayer.prev();
                    break;
                case R.id.btnNext:
                    MusicPlayer.next();
                    break;
            }
        }
    }

    private void handleCommandIntent(Intent intent) {
        final String command = intent.getStringExtra("command");
        switch (command) {
            case MusicService.PLAY_ACTION:
                Log.d("myLog", "PLAY_ACTION");
                setContentSong();
                if (contentMusicPaused) {
                    contentMusicPaused = false;
                    seekBarStart();
                }
                break;
            case MusicService.START_ACTION:
                contentMusicPaused = false;
                buttonLogoPause();
                seekBarStart();
                break;
            case MusicService.PAUSE_ACTION:
                contentMusicPaused = true;
                buttonLogoPlay();
                break;
        }
    }

    private void setSeekBarText() {
        if (seekBar == null) return;
        final long position = MusicPlayer.getCurrentPosition();
        final long positionSec = position / 1000;
        final long timeLeft = MusicPlayer.getDuration() / 1000 - positionSec;
        seekBar.setProgress((int) position);
        if (tvTimePassed != null && tvTimeLeft != null) {
            tvTimePassed.setText(makeShortTimeString(getBaseContext(), positionSec));
            tvTimeLeft.setText("-" + makeShortTimeString(getBaseContext(), timeLeft));
        }
    }

    private String makeShortTimeString(final Context context, long secs) {
        long hours, mins;

        hours = secs / 3600;
        secs %= 3600;
        mins = secs / 60;
        secs %= 60;

        final String durationFormat = context.getResources().getString(
                hours == 0 ? R.string.durationformatshort : R.string.durationformatlong);
        return String.format(durationFormat, hours, mins, secs);
    }

    //seekbar
    private final Runnable mUpdateProgress = new Runnable() {

        @Override
        public void run() {
            setSeekBarText();
            overflowcounter--;
            final int delay = 250; //not sure why this delay was so high before
            if (overflowcounter < 0 && !contentMusicPaused) {
                overflowcounter++;
                seekBar.postDelayed(mUpdateProgress, delay); //delay
            }
            Log.d("myLog", "mUpdateProgress: overflowcounter = " + overflowcounter);
        }
    };

    private void setContentSong() {
        Log.d("myLog", "setContentSong");
        final Song arSong = MusicPlayer.getList();

        if (arSong == null) {
            return;
        }

        buttonChange();
        seekBar.setMax((int) MusicPlayer.getDuration());

        final Bitmap albumB = arSong.getAlbumB();
        final String artist = arSong.getArtist();
        final String title = arSong.getTitle();

        final ImageView imgContentAlbum = findViewById(R.id.imgContentAlbum);
        imgContentAlbum.setImageBitmap(albumB);
        imgContentAlbum.setColorFilter(R.color.filterContentImage);

        final CircleImageView imgContentAlbumMin = findViewById(R.id.imgContentAlbumMin);
        imgContentAlbumMin.setImageBitmap(albumB);

        final TextView tvArtistNameContent = findViewById(R.id.tvArtistNameContent);
        tvArtistNameContent.setText(artist);

        final TextView tvSongNameContent = findViewById(R.id.tvSongNameContent);
        tvSongNameContent.setText(title);
    }
}
