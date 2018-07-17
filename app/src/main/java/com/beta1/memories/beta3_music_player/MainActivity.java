package com.beta1.memories.beta3_music_player;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String START_G_SONG = "start_g";
    public static final String START_SONG = "start";
    public static final String STOP_SONG = "stop";
    public static final String PREV_SONG = "prev";
    public static final String NEXT_SONG = "next";

    private final int REQUEST_EXTERNAL_STORAGE_PERMISSION = 200;
    private final String[] arPermissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.WAKE_LOCK
    };
    //public static String BROADCAST_ACTION;

    public static SongService mSongService;
    private Intent songIntent;
    private boolean songBound = false;
    private boolean isPermission = false;

    public static final ArrayList<Song> songList = new ArrayList();
    private ListView songView;

/*    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            controlSong(intent);
        }
    };*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //BROADCAST_ACTION = getPackageName();

        songView = findViewById(R.id.lvContainer);

        if (hasPermissions()) {
            isPermission = true;
            runApplication();
        } else {
            ActivityCompat.requestPermissions(this, arPermissions, REQUEST_EXTERNAL_STORAGE_PERMISSION);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isPermission) {
            startSongIntent();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mSongService != null) {
            mSongService.mManager.cancel(mSongService.NOTIFY_ID);
            stopService(songIntent);
            mSongService = null;
            Log.d("myLog", "mSongService = null");
        }

        // выключаем BroadcastReceiver
        //unregisterReceiver(mIntentReceiver);
    }

    private boolean hasPermissions(){
        boolean _isPermission = true;
        for (String perms : arPermissions) {
            int res = checkCallingOrSelfPermission(perms);
            if (res != PackageManager.PERMISSION_GRANTED) {
                _isPermission = false;
                break;
            }
        }
        return _isPermission;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean permissionToStorageAccepted = true;
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE_PERMISSION:
                for (int res : grantResults) {
                    if (res != PackageManager.PERMISSION_GRANTED) {
                        permissionToStorageAccepted = false;
                        break;
                    }
                }
                break;
            default:
                permissionToStorageAccepted = false;
                break;
        }
        if (permissionToStorageAccepted) {
            isPermission = true;
            runApplication();
            startSongIntent();
        } else {
            startActivity(new Intent(this, ErrorPermissionActivity.class));
            finish();
        }
    }

    private void runApplication() {
        getSongList();
        // Отсортировка песен
        Collections.sort(songList, new Comparator<Song>() {
            @Override
            public int compare(Song o1, Song o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });

        SongAdapter songAdapter = new SongAdapter(this, songList);
        songView.setAdapter(songAdapter);
        songView.setOnItemClickListener(new clickItemSong());

        // создаем фильтр для BroadcastReceiver
        /*final IntentFilter intentFilter = new IntentFilter(BROADCAST_ACTION);
        // регистрируем (включаем) BroadcastReceiver
        registerReceiver(mIntentReceiver, intentFilter);*/
    }

    private void controlSong(Intent intent) {
        final String command = intent.getStringExtra("command");
        switch (command) {
            case START_G_SONG:
                mSongService.playSong();
                break;
            case START_SONG:
                mSongService.startPlay();
                break;
            case STOP_SONG:
                mSongService.pausePlayer();
                break;
            case PREV_SONG:
                mSongService.prevSong();
                break;
            case NEXT_SONG:
                mSongService.nextSong();
                break;
        }
    }

    public Bitmap getAlbumart(Long album_id) {

        Bitmap bm = null;
        try {

            final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);

            ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "r");

            if (pfd != null) {
                FileDescriptor fd = pfd.getFileDescriptor();
                bm = BitmapFactory.decodeFileDescriptor(fd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bm;
    }

    private void getSongList() {
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = musicResolver.query(musicUri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            // get columns
            int idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            // add song to list
            do {
                long musicId = cursor.getLong(idColumn);
                String musicTitle = cursor.getString(titleColumn);
                String musicArtist = cursor.getString(artistColumn);
                long album_id = cursor.getLong(albumColumn);

                songList.add(new Song(musicId, musicTitle, musicArtist, getAlbumart(album_id)));
            } while (cursor.moveToNext());
        }
    }

    private void startSongIntent() {
        if (songIntent == null) {
            songIntent = new Intent(this, SongService.class);
            bindService(songIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(songIntent);
        }
    }

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SongService.MusicBinder binder = (SongService.MusicBinder) service;
            mSongService = binder.getService();
            mSongService.setList(songList);
            songBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            songBound = false;
        }
    };

    private class clickItemSong implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            //int tag = Integer.valueOf(String.valueOf(view.getTag()));

            Intent intent = new Intent(MainActivity.this, ContentMusic.class);
            if (mSongService.getPositionSong() != position) {
                mSongService.setSong(position);
                mSongService.playSong();
                intent.putExtra("startPlay", 1);
            }
            startActivity(intent);
        }
    }
}
