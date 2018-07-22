package com.beta1.memories.beta3_music_player;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    public static final String BROADCAST_ACTION = "com.beta3_music.control_music";
    public static final String PLAY_ACTION = "play";
    public static final String START_ACTION = "start";
    public static final String PAUSE_ACTION = "pause";

    private Intent intentBroadcast;
    private final IBinder binder = new MusicBinder();
    private MediaPlayer mp;
    private ArrayList<Song> songsList;
    private int position = -1;

    private NotificationManager mManager;
    private String songTitle;
    private final int NOTIFY_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        intentBroadcast = new Intent(BROADCAST_ACTION);
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

    private void putIntentCommand(String command) {
        intentBroadcast.putExtra("command", command);
        sendBroadcast(intentBroadcast);
    }

    public void notifManagerCancel() {
        mManager.cancel(NOTIFY_ID);
    }

    public void setList(ArrayList<Song> arr) {
        this.songsList = arr;
    }

    public void setPosition(int position) {
        this.position = position;
        Log.d("myLog", String.valueOf("set position = " + position));
    }

    public void play() {
        mp.reset();
        final Song arSong = songsList.get(position);
        Log.d("myLog", String.valueOf("play position = " + position));

        songTitle = arSong.getTitle();
        final long id = arSong.getId();

        final Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
        try {
            mp.setDataSource(getApplicationContext(), trackUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mp.prepareAsync();
    }

    public Song getList() {
        return songsList.get(position);
    }

    public long getCurrentPosition() {
        return mp.getCurrentPosition();
    }

    public int position() {
        Log.d("myLog", String.valueOf("get position = " + position));
        return position;
    }

    public long duration() {
        return mp.getDuration();
    }

    public boolean isPlaying() {
        return mp.isPlaying();
    }

    public void pause() {
        mp.pause();
        putIntentCommand(PAUSE_ACTION);
    }

    public void start() {
        mp.start();
        putIntentCommand(START_ACTION);
    }

    public long seek(final long whereto) {
        mp.seekTo((int) whereto);
        return whereto;
    }

    public void prev() {
        if (position > 0) {
            position --;
        } else {
            position = songsList.size() - 1;
        }
        play();
    }

    public void next() {
        Log.d("myLog", "пришел next");
        int sizeSong = songsList.size() - 1;
        if (sizeSong == 0) {
            return;
        }
        if (position < sizeSong) {
            position ++;
        } else {
            position = 0;
        }
        play();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mp.getCurrentPosition() > 0) {
            mp.reset();
            next();
            Log.d("myLog", String.valueOf("next onCompletion"));
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
        putIntentCommand(PLAY_ACTION); // broadcast
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
        MusicService getService() {
            return MusicService.this;
        }
    }
}
