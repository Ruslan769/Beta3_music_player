package com.beta1.memories.beta3_music_player;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;

import java.io.IOException;
import java.util.ArrayList;

public class SongService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private final IBinder binder = new MusicBinder();
    private MediaPlayer mp;
    private ArrayList<Song> songsList;
    private int positionSong = -1;

    public NotificationManager mManager;
    private String songTitle;
    public final int NOTIFY_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        mp = new MediaPlayer();
        mp.setLooping(true);
        mp.setOnCompletionListener(this);
        mp.setOnErrorListener(this);
        mp.setOnPreparedListener(this);
        mp.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
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

        this.songTitle = arSong.getTitle();
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

    public int getPositionSong() {
        return positionSong;
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

    public void prevSong() {
        if (positionSong > 0) {
            positionSong --;
        } else {
            positionSong = songsList.size() - 1;
        }
        playSong();
    }

    public void nextSong() {
        int sizeSong = songsList.size() - 1;
        if (sizeSong == 0) {
            return;
        }
        if (positionSong < sizeSong) {
            positionSong ++;
        } else {
            positionSong = 0;
        }
        playSong();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mp.getCurrentPosition() > 0) {
            mp.reset();
            nextSong();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        Intent intent = new Intent(this, ContentMusic.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent PI = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap iconSong = getList().getAlbumB();

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, "music_notif")
                        .setContentIntent(PI)
                        .setSmallIcon(R.drawable.ic_music_icon)
                        .setLargeIcon(iconSong)
                        .setTicker(songTitle)
                        .setOngoing(true)
                        .setContentTitle("")
                        .setContentText(songTitle);

        mManager.notify(NOTIFY_ID, builder.build());
    }

    public class MusicBinder extends Binder {
        SongService getService() {
            return SongService.this;
        }
    }
}
