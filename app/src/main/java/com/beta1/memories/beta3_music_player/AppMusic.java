package com.beta1.memories.beta3_music_player;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.beta1.memories.beta3_music_player.adapter.BaseSongAdapter;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AppMusic extends AppCompatActivity implements EventServiceConnect {

    private boolean permission = false;
    private final int REQUEST_EXTERNAL_STORAGE_PERMISSION = 200;
    private final String[] arPermissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.WAKE_LOCK
    };

    private MusicPlayer.ServiceToken mToken;
    private final ArrayList<Song> arListSongs = new ArrayList();
    private ListView songView;

    private boolean startOnCreate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startOnCreate = true;

        songView = findViewById(R.id.lvContainer);

        if (hasPermissions()) {
            permission = true;
            runApplication();
        } else {
            ActivityCompat.requestPermissions(this, arPermissions, REQUEST_EXTERNAL_STORAGE_PERMISSION);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
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
        //For Android 8.0+: service may get destroyed if in background too long
        if (MusicPlayer.mService == null && permission && !startOnCreate){
            Log.d("myLog", "onResume: mService == null and startOnCreate == false");
            mToken = MusicPlayer.bindToService(this, this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mToken != null) {
            MusicPlayer.notifyCancel();
            MusicPlayer.unbindFromService(mToken);
            mToken = null;
        }
    }

    private boolean hasPermissions(){
        boolean isPermission = true;
        for (String perms : arPermissions) {
            int res = checkCallingOrSelfPermission(perms);
            if (res != PackageManager.PERMISSION_GRANTED) {
                isPermission = false;
                break;
            }
        }
        return isPermission;
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
            permission = true;
            runApplication();
        } else {
            startActivity(new Intent(this, ErrorPermissionActivity.class));
            finish();
        }
    }

    private void runApplication() {
        mToken = MusicPlayer.bindToService(this, this);
        getSongList();
        // Отсортировка песен
        Collections.sort(arListSongs, new Comparator<Song>() {
            @Override
            public int compare(Song o1, Song o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });

        final BaseSongAdapter songAdapter = new BaseSongAdapter(this, arListSongs);
        songView.setAdapter(songAdapter);
        songView.setOnItemClickListener(new clickItemSong());
    }

    @Override
    public void onConnectedService() {
        MusicPlayer.setList(arListSongs);
        Log.d("myLog", "onConnect: songList");
    }

    @Override
    public void onDisconnectedService() {
        Log.d("myLog", "onDisconnect");
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
        final ContentResolver musicResolver = getContentResolver();
        final Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        final Cursor cursor = musicResolver.query(musicUri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            // get columns
            final int idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
            final int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            final int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            final int albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            // add song to list
            do {
                final long musicId = cursor.getLong(idColumn);
                final String musicTitle = cursor.getString(titleColumn);
                final String musicArtist = cursor.getString(artistColumn);
                final long album_id = cursor.getLong(albumColumn);

                arListSongs.add(new Song(musicId, musicTitle, musicArtist, getAlbumart(album_id)));
            } while (cursor.moveToNext());
        }
    }

    private class clickItemSong implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            Intent intent = new Intent(AppMusic.this, ContentMusic.class);
            intent.putExtra("play", position);
            startActivity(intent);
        }
    }
}
