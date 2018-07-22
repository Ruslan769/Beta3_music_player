package com.beta1.memories.beta3_music_player;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.WeakHashMap;

public class MusicPlayer {

    private static final WeakHashMap<Context, ServiceBinder> mConnectionMap = new WeakHashMap<Context, ServiceBinder>();
    public static MusicService mService;

    public static final ServiceToken bindToService(final Context context, final EventServiceConnect callback) {

        Activity realActivity = ((Activity) context).getParent();

        if (realActivity == null) {
            realActivity = (Activity) context;
        }
        final ContextWrapper contextWrapper = new ContextWrapper(realActivity);
        contextWrapper.startService(new Intent(contextWrapper, MusicService.class));
        final ServiceBinder binder = new ServiceBinder(callback);
        if (contextWrapper.bindService(
                new Intent().setClass(contextWrapper, MusicService.class), binder, 0)) {
            mConnectionMap.put(contextWrapper, binder);
            return new ServiceToken(contextWrapper);
        }
        return null;
    }

    public static void unbindFromService(final ServiceToken token) {
        if (token == null) {
            return;
        }
        final ContextWrapper mContextWrapper = token.mWrappedContext;
        final ServiceBinder mBinder = mConnectionMap.remove(mContextWrapper);
        if (mBinder == null) {
            return;
        }
        mContextWrapper.unbindService(mBinder);
        if (mConnectionMap.isEmpty()) {
            mService = null;
            Log.d("myLog", "unbindFromService: mService = null");
        }
    }

    public static final class ServiceBinder implements ServiceConnection {
        private final EventServiceConnect mCallback;

        public ServiceBinder(final EventServiceConnect callback) {
            mCallback = callback;
        }

        @Override
        public void onServiceConnected(final ComponentName className, final IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            mService = binder.getService();
            if (mCallback != null) {
                mCallback.onConnectedService();
            }
        }

        @Override
        public void onServiceDisconnected(final ComponentName className) {
            if (mCallback != null) {
                mCallback.onDisconnectedService();
            }
            mService = null;
            Log.d("myLog", "onServiceDisconnected: mService = null");
        }
    }

    public static final class ServiceToken {
        public ContextWrapper mWrappedContext;
        public ServiceToken(final ContextWrapper context) {
            mWrappedContext = context;
        }
    }

    public static void setList(ArrayList<Song> arr) {
        if (mService != null) {
            mService.setList(arr);
        }
    }

    public static void setPosition(int position) {
        if (mService != null) {
            mService.setPosition(position);
        }
    }

    public static boolean isPlaying() {
        if (mService != null) {
            return mService.isPlaying();
        }
        return false;
    }

    public static void notifyCancel() {
        if (mService != null) {
            mService.notifManagerCancel();
        }
    }

    @Nullable
    public static Song getList() {
        if (mService != null) {
            return mService.getList();
        }
        return null;
    }

    public static int getPosition() {
        if (mService != null) {
            return mService.position();
        }
        return -1;
    }

    public static long getCurrentPosition() {
        if (mService != null) {
            return mService.getCurrentPosition();
        }
        return 0;
    }

    public static long getDuration() {
        if (mService != null) {
            return mService.duration();
        }
        return 0;
    }

    public static void prev() {
        if (mService != null) {
            mService.prev();
        }
    }

    public static void next() {
        if (mService != null) {
            mService.next();
        }
    }

    public static void play() {
        if (mService != null) {
            mService.play();
        }
    }

    public static void playOrPause() {
        if (mService != null) {
            if (mService.isPlaying()) {
                mService.pause();
            } else {
                mService.start();
            }
        }
    }

    public static void seek(final long position) {
        if (mService != null) {
            mService.seek(position);
        }
    }
}
