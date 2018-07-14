package com.beta1.memories.beta3_music_player;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;

import java.io.IOException;
import java.util.ArrayList;

public class SongService extends Service {

    private final IBinder binder = new MusicBinder();
    private MediaPlayer mp;
    private ArrayList<Song> songsList;
    private int positionSong;
    private String SongTitle;

    @Override
    public void onCreate() {
        super.onCreate();
        this.positionSong = 0;
        mp = new MediaPlayer();
        mp.setLooping(true);
        mp.setOnPreparedListener(new EventPrepare());
        mp.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (mp.isPlaying()) {
            mp.stop();
        }
        mp.release();
        mp = null;
        return false;
    }

    public void onDestroy() {
        if (mp != null) {
            mp.release();
        }
    }

    public void setList(ArrayList<Song> arr) {
        this.songsList = arr;
    }

    public void setSong(int songIndex) {
        this.positionSong = songIndex;
    }

    public void playSong() {
        mp.reset();
        Song arSong = songsList.get(positionSong);

        this.SongTitle = arSong.getTitle();
        long id = arSong.getId();

        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
        try {
            mp.setDataSource(getApplicationContext(), trackUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mp.prepareAsync();
    }

    public Song getList() {
        return songsList.get(positionSong);
    }

    public int getPosition() {
        return mp.getCurrentPosition();
    }

    public int getDuration() {
        return mp.getDuration();
    }

    public boolean isPlaying() {
        return mp.isPlaying();
    }

    public void pausePlayer() {
        mp.pause();
    }

    public void startPlay() {
        mp.start();
    }

    public void seekPlayer(int position) {
        mp.seekTo(position);
    }

    private class EventPrepare implements MediaPlayer.OnPreparedListener {

        @Override
        public void onPrepared(MediaPlayer mp) {
            mp.start();
        }
    }

    public class MusicBinder extends Binder {
        SongService getService() {
            return SongService.this;
        }
    }
}
