package com.gark.vk.services;

/**
 * Created by Artem on 10.07.13.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.widget.RemoteViews;

import com.gark.vk.R;
import com.gark.vk.db.MusicColumns;
import com.gark.vk.model.MusicObject;
import com.gark.vk.model.PlayList;
import com.gark.vk.ui.MainActivity1;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class PlaybackService extends Service implements OnPreparedListener, OnSeekCompleteListener, OnBufferingUpdateListener, OnCompletionListener, OnErrorListener, OnInfoListener {

    private static final String LOG_TAG = PlaybackService.class.getName();

    private static final String SERVICE_PREFIX = "com.gark.vk.services.";

    public static final String SERVICE_CHANGE_NAME = SERVICE_PREFIX + "CHANGE";
    public static final String SERVICE_CLOSE_NAME = SERVICE_PREFIX + "CLOSE";
    public static final String SERVICE_UPDATE_NAME = SERVICE_PREFIX + "UPDATE";
    public static final String SERVICE_ERROR_NAME = SERVICE_PREFIX + "ERROR";
    public static final String SERVICE_PRESS_PLAY = SERVICE_PREFIX + "PRESS_PLAY";
    public static final String SERVICE_ON_PREPARE = SERVICE_PREFIX + "ON_PREPARE";
    public static final String SERVICE_ON_PAUSE = SERVICE_PREFIX + "ON_STOP";
//    public static final String SERVICE_ON_START_PRESSED = SERVICE_PREFIX + "ON_START_PRESSED";

    public static final String SERVICE_PLAY_PLAYLIST = SERVICE_PREFIX + "PLAYLIST";
    public static final String SERVICE_PLAY_SINGLE = SERVICE_PREFIX + "PLAY_SINGLE";
    public static final String SERVICE_PLAY_ENTRY = SERVICE_PREFIX + "PLAY_ENTRY";
    public static final String SERVICE_TOGGLE_PLAY = SERVICE_PREFIX + "TOGGLE_PLAY";
    public static final String SERVICE_STOP = SERVICE_PREFIX + "STOP";
    public static final String SERVICE_BACK_30 = SERVICE_PREFIX + "BACK_30";
    public static final String SERVICE_FORWARD_30 = SERVICE_PREFIX + "FORWARD_30";
    public static final String SERVICE_SEEK_TO = SERVICE_PREFIX + "SEEK_TO";
    public static final String SERVICE_PLAY_MOVE = SERVICE_PREFIX + "MOVE";
    public static final String SERVICE_PLAY_NEXT = SERVICE_PREFIX + "PLAYNEXT";
    public static final String SERVICE_PLAY_PREVIOUS = SERVICE_PREFIX + "PLAYPREVIOUS";
    public static final String SERVICE_STOP_PLAYBACK = SERVICE_PREFIX + "STOP_PLAYBACK";
    public static final String SERVICE_STATUS = SERVICE_PREFIX + "STATUS";
    public static final String SERVICE_CLEAR_PLAYER = SERVICE_PREFIX + "CLEAR_PLAYER";

    public static final String EXTRA_ID = SERVICE_PREFIX + "ID";
    public static final String EXTRA_TITLE = SERVICE_PREFIX + "TITLE";
    public static final String EXTRA_ARTIST = SERVICE_PREFIX + "ARTIST";
    public static final String EXTRA_URL = SERVICE_PREFIX + "URL";
    public static final String EXTRA_DOWNLOADED = SERVICE_PREFIX + "DOWNLOADED";
    public static final String SECONDARY_PROGRESS = SERVICE_PREFIX + "SECONDARY";
    public static final String EXTRA_DURATION = SERVICE_PREFIX + "DURATION";
    public static final String EXTRA_POSITION = SERVICE_PREFIX + "POSITION";
    public static final String EXTRA_SEEK_TO = SERVICE_PREFIX + "SEEK_TO";
    public static final String EXTRA_IS_PLAYING = SERVICE_PREFIX + "IS_PLAYING";
    public static final String EXTRA_IS_PREPARED = SERVICE_PREFIX + "IS_PREPARED";

    public static final String EXTRA_ERROR = SERVICE_PREFIX + "ERROR";

    public static final String NOTIFICATION_CLOSE_APPLICATION = SERVICE_PREFIX + "CLOSE_APPLICATION";
    public static final String NOTIFICATION_UPDATE = SERVICE_PREFIX + "NOTIFICATION_UPDATE";
    public static final int NOTIFICATION_CLOSE_ACTION = 11;
    public static final int NOTIFICATION_NEXT_ACTION = 12;
    public static final int NOTIFICATION_TOOGLE_ACTION = 13;

    public static enum PLAYBACK_SERVICE_ERROR {Connection, Playback}

    private NotificationManager m_notificationMgr;
    private NotificationCompat.Builder nb;


    private MediaPlayer mMediaPlayer;
    private boolean isPrepared = false;
    private boolean markedRead;
    // Track whether we ever called start() on the media player so we don't try
    // to reset or release it. This causes a hang (ANR) on Droid X
    // http://code.google.com/p/android/issues/detail?id=959
    private boolean mediaPlayerHasStarted = false;

    //    private StreamProxy proxy;
//    private NotificationManager notificationManager;
//    private static final int NOTIFICATION_ID = 1;
    private int startId;
//    private String currentAction;

    // Error handling
    private int errorCount;
    private int connectionErrorWaitTime;
    private int seekToPosition;

    private TelephonyManager telephonyManager;
    private PhoneStateListener listener;
    private boolean isPausedInCall = false;
    private Intent lastChangeBroadcast;
    private Intent lastUpdateBroadcast;
    private int lastBufferPercent = 0;
    private Thread updateProgressThread;

    // Amount of time to rewind playback when resuming after call
    private final static int RESUME_REWIND_TIME = 3000;
    private final static int ERROR_RETRY_COUNT = 3;
    private final static int RETRY_SLEEP_TIME = 30000;

    private Looper serviceLooper;
    private ServiceHandler serviceHandler;
    private WifiManager.WifiLock wifiLock;


    private PlayList mPlayList;
    private AsyncQueryHandler asyncQueryHandler;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            startId = msg.arg1;
            onHandleIntent((Intent) msg.obj);
        }
    }

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public PlaybackService getService() {
            // Return this instance of LocalService so clients can call public methods
            return PlaybackService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnSeekCompleteListener(this);

        mPlayList = new PlayList(this);
        mPlayList.resetPosition();


        wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
        wifiLock.acquire();

//        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        playlist = new PlaylistRepository(getApplicationContext(), getContentResolver());


        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        // Create a PhoneStateListener to watch for off-hook and idle events
        listener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        // Phone going off-hook or ringing, pause the player.
                        if (isPlaying()) {
                            pause();
                            isPausedInCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Rewind a couple of seconds and start playing.
                        if (isPausedInCall) {
                            isPausedInCall = false;
                            seekTo(Math.max(0, getPosition() - RESUME_REWIND_TIME));
                            play();
                        }
                        break;
                }
            }
        };

        // Register the listener with the telephony manager.
        telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);

        HandlerThread thread = new HandlerThread("PlaybackService:WorkerThread");
        thread.start();

        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);

        asyncQueryHandler = new AsyncQueryHandler(getContentResolver()) {
        };

        switchButtonListener = new SwitchButtonListener();
        this.registerReceiver(switchButtonListener, new IntentFilter(PlaybackService.NOTIFICATION_UPDATE));

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//        Toast.makeText(this, "onStartCommand", Toast.LENGTH_SHORT).show();
//        Log.e("onStartCommand","onStartCommand");

        Message message = serviceHandler.obtainMessage();
        message.arg1 = startId;
        message.obj = intent;
        serviceHandler.sendMessage(message);
        return super.onStartCommand(intent, flags, startId);

    }


    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (action.equals(SERVICE_PLAY_SINGLE) || action.equals(SERVICE_PLAY_ENTRY)) {
                mPlayList.setCurrentPosition(intent.getIntExtra(EXTRA_POSITION, 0));
                playCurrent(0, 1);
            } else if (action.equals(SERVICE_TOGGLE_PLAY)) {
                if (isPlaying()) {
                    pause();
                    Intent emptyIntent = new Intent(intent);
                    emptyIntent.setAction("");
                    startService(emptyIntent);
                } else {
                    if (isPrepared) {
                        play();
                    } else {
                        playCurrent(0, 1);
                    }
                }
                updateNotification();
            } else if (action.equals(SERVICE_STOP)) {
                if (isPlaying()) {
                    pause();
                }
            } else if (action.equals(SERVICE_SEEK_TO)) {
                seekTo(intent.getIntExtra(EXTRA_SEEK_TO, 0));
            } else if (action.equals(SERVICE_PLAY_NEXT)) {
                mPlayList.moveToNextTrack();
                playCurrent(0, 1);
            } else if (action.equals(SERVICE_PLAY_PREVIOUS)) {
                if (mMediaPlayer != null && mMediaPlayer.getCurrentPosition() < 10 * 1000) {
                    mPlayList.moveToPreviousTrack();
                }
                playCurrent(0, 1);
            } else if (action.equals(SERVICE_STOP_PLAYBACK)) {
                stopSelfResult(startId);
            } else if (action.equals(SERVICE_PLAY_PLAYLIST)) {
                ArrayList<MusicObject> playList = intent.getParcelableArrayListExtra(SERVICE_PLAY_PLAYLIST);
                if (playList != null && playList.size() > 0)
                    mPlayList.setPlayList(playList);
            } else if (action.equals(NOTIFICATION_CLOSE_APPLICATION)) {
                if (!isPlaying()) {
                    hideNotification(this);
                }
            }
        }
    }

    private void showActiveTrack() {
        if (mPlayList.getCurrentItem() != null) {
            String aid = mPlayList.getCurrentItem().getAid();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MusicColumns.IS_ACTIVE.getName(), 1);
            asyncQueryHandler.startUpdate(0, null, MusicObject.CONTENT_URI, contentValues, MusicColumns.AID.getName() + "=?", new String[]{aid});
        }
    }

    private void hideActiveTrack() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MusicColumns.IS_ACTIVE.getName(), 0);
        asyncQueryHandler.startUpdate(0, null, MusicObject.CONTENT_URI, contentValues, null, null);
    }


    private boolean playCurrent(int startingErrorCount, int startingWaitTime) {
        errorCount = startingErrorCount;
        connectionErrorWaitTime = startingWaitTime;

        Intent intent = new Intent(SERVICE_PRESS_PLAY);
        getApplicationContext().sendBroadcast(intent);

        while (errorCount < ERROR_RETRY_COUNT) {
            try {
                prepareThenPlay();

                hideActiveTrack();
//                showActiveTrack();

                return true;
            } catch (UnknownHostException e) {
                Log.w(LOG_TAG, "Unknown host in playCurrent");
                handleConnectionError();
            } catch (ConnectException e) {
                Log.w(LOG_TAG, "Connect exception in playCurrent");
                handleConnectionError();
            } catch (IOException e) {
                incrementErrorCount();
            }
        }
        return false;
    }


    synchronized private int getPosition() {
        if (isPrepared) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    synchronized public boolean isPlaying() {
        return isPrepared && mMediaPlayer.isPlaying();
    }

    synchronized private void seekTo(int pos) {
        if (isPrepared) {
            seekToPosition = 0;
            mMediaPlayer.seekTo((pos * mMediaPlayer.getDuration()) / 100);
        }
    }

    private void prepareThenPlay() throws IllegalArgumentException, IllegalStateException, IOException {
        stopPlayer();

        try {
            if (mPlayList.getCurrentItem() == null) {
                return;
            }

            String playUrl = mPlayList.getCurrentItem().getUrl();
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(playUrl);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    synchronized private void play() {
        if (!isPrepared) {
            return;
        }

        showActiveTrack();
        showNotification(this);

        mMediaPlayer.start();
        mediaPlayerHasStarted = true;

        Intent intent = new Intent(SERVICE_PRESS_PLAY);
        Bundle bundle = new Bundle();
        bundle.putString(SERVICE_PRESS_PLAY, SERVICE_PRESS_PLAY);
        intent.putExtras(bundle);
        getApplicationContext().sendBroadcast(intent);

    }

    synchronized private void pause() {
        hideActiveTrack();

        Intent intent = new Intent(SERVICE_ON_PAUSE);
        getApplicationContext().sendBroadcast(intent);

        Log.d(LOG_TAG, "pause");
        if (isPrepared) {
            mMediaPlayer.pause();
        }
    }

    synchronized private void stopPlayer() {
        hideActiveTrack();
        hideNotification(this);


        Log.d(LOG_TAG, "stopPlayer");
        if (isPrepared) {
            isPrepared = false;
            mMediaPlayer.stop();
            mMediaPlayer.seekTo(0);
            seekToPosition = 0;
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        showActiveTrack();

//        showNotification(this);

        Intent intent = new Intent(SERVICE_ON_PREPARE);
        intent.putExtra(SERVICE_ON_PREPARE, mPlayList.getCurrentPosition());
        getApplicationContext().sendBroadcast(intent);


        if (mMediaPlayer != null) {
            isPrepared = true;
        }

        if (seekToPosition > 0) {
            Log.d(LOG_TAG, "Seeking to starting position: " + seekToPosition);
            mp.seekTo(seekToPosition);
        } else {
            startPlaying();
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        Log.d(LOG_TAG, "Seek complete");
        if (seekToPosition > 0) {
            seekToPosition = 0;
            startPlaying();
        }
    }

    private void startPlaying() {
        play();
        updateProgressThread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    updateProgress();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        });
        updateProgressThread.start();
    }

    @Override
    public void onDestroy() {
        this.unregisterReceiver(switchButtonListener);
        super.onDestroy();

        mPlayList.resetPosition();
        stopPlayer();

        wifiLock.release();

        if (updateProgressThread != null) {
            updateProgressThread.interrupt();
            try {
                updateProgressThread.join(1000);
            } catch (InterruptedException e) {
                Log.e(LOG_TAG, "", e);
            }
        }

        if (mMediaPlayer != null) {
            if (mediaPlayerHasStarted) {
                mMediaPlayer.release();
            } else {
                mMediaPlayer.setOnBufferingUpdateListener(null);
                mMediaPlayer.setOnCompletionListener(null);
                mMediaPlayer.setOnErrorListener(null);
                mMediaPlayer.setOnInfoListener(null);
                mMediaPlayer.setOnPreparedListener(null);
                mMediaPlayer.setOnSeekCompleteListener(null);
            }
            mMediaPlayer = null;
        }

        serviceLooper.quit();

        if (lastChangeBroadcast != null) {
            getApplicationContext().removeStickyBroadcast(lastChangeBroadcast);
        }
        getApplicationContext().sendBroadcast(new Intent(SERVICE_CLOSE_NAME));
        telephonyManager.listen(listener, PhoneStateListener.LISTEN_NONE);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int progress) {
        if (isPrepared) {
            lastBufferPercent = progress;
            updateProgress();
        }
    }

    /**
     * Sends an UPDATE broadcast with the latest info.
     */
    private void updateProgress() {

        // Stop updating after mediaplayer is released
        if (mMediaPlayer == null)
            return;

        if (isPrepared) {

            if (lastUpdateBroadcast != null) {
                try {
                    this.removeStickyBroadcast(lastUpdateBroadcast);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                lastUpdateBroadcast = null;
            }

            int duration = mMediaPlayer.getDuration();
            seekToPosition = mMediaPlayer.getCurrentPosition();
            if (!markedRead) {
                if (seekToPosition > duration / 10) {
                    markedRead = true;
                }
            }

            Intent tempUpdateBroadcast = new Intent(SERVICE_UPDATE_NAME);
            tempUpdateBroadcast.putExtra(EXTRA_DURATION, duration);
            tempUpdateBroadcast.putExtra(EXTRA_DOWNLOADED, (int) ((lastBufferPercent / 100.0) * duration));
            tempUpdateBroadcast.putExtra(SECONDARY_PROGRESS, lastBufferPercent);
            tempUpdateBroadcast.putExtra(EXTRA_POSITION, seekToPosition);
            tempUpdateBroadcast.putExtra(EXTRA_IS_PLAYING, mMediaPlayer.isPlaying());
            try {
                tempUpdateBroadcast.putExtra(EXTRA_ARTIST, mPlayList.getCurrentItem().getArtist());
                tempUpdateBroadcast.putExtra(EXTRA_TITLE, mPlayList.getCurrentItem().getTitle());
            } catch (Exception e) {
                e.printStackTrace();
            }

            tempUpdateBroadcast.putExtra(EXTRA_IS_PREPARED, isPrepared);

            // Update broadcasts while playing are not sticky, due to concurrency
            // issues.  These fire very often, so this shouldn't be a problem.
            getApplicationContext().sendBroadcast(tempUpdateBroadcast);
        } else {
            if (lastUpdateBroadcast == null) {
                lastUpdateBroadcast = new Intent(SERVICE_UPDATE_NAME);
                lastUpdateBroadcast.putExtra(EXTRA_IS_PLAYING, false);
                getApplicationContext().sendStickyBroadcast(lastUpdateBroadcast);
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
//        Log.w(LOG_TAG, "onComplete()");
//        Toast.makeText(this, "onComplete", Toast.LENGTH_SHORT).show();


        mPlayList.moveToNextTrack();

        try {
            playCurrent(0, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void incrementErrorCount() {
        errorCount++;
        Log.e(LOG_TAG, "Media player increment error count:" + errorCount);
        if (errorCount >= ERROR_RETRY_COUNT) {
            Intent intent = new Intent(SERVICE_ERROR_NAME);
            intent.putExtra(EXTRA_ERROR, PLAYBACK_SERVICE_ERROR.Playback.ordinal());
            getApplicationContext().sendBroadcast(intent);
        }
    }

    private void handleConnectionError() {
        connectionErrorWaitTime *= 5;
        if (connectionErrorWaitTime > RETRY_SLEEP_TIME) {
            Log.e(LOG_TAG, "Connection failed.  Resetting mMediaPlayer" +
                    " and trying again in 30 seconds.");

            Intent intent = new Intent(SERVICE_ERROR_NAME);
            intent.putExtra(EXTRA_ERROR, PLAYBACK_SERVICE_ERROR.Connection.ordinal());
            getApplicationContext().sendBroadcast(intent);

            // If a stream, increment since it could be bad
//            if (current.isStream()) {
//                errorCount++;
//            }

            connectionErrorWaitTime = RETRY_SLEEP_TIME;
            // Send error notification and keep waiting
            isPrepared = false;
            mMediaPlayer.reset();
        } else {
            Log.w(LOG_TAG, "Connection error. Waiting for " + connectionErrorWaitTime + " milliseconds.");
        }
        SystemClock.sleep(connectionErrorWaitTime);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {

//        Toast.makeText(this, "onError", Toast.LENGTH_SHORT).show();

        Log.w(LOG_TAG, "onError(" + what + ", " + extra + ")");
        if (!isPrepared) {
            Log.w(LOG_TAG, "MediaPlayer refused to play current item. Bailing on prepare.");
        }

        isPrepared = false;
        mMediaPlayer.reset();

        incrementErrorCount();
        if (errorCount < ERROR_RETRY_COUNT) {
            playCurrent(errorCount, 1);
            // Returning true means we handled the error, false causes the
            // onCompletion handler to be called
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onInfo(MediaPlayer arg0, int arg1, int arg2) {
        return false;
    }


    private void showNotification(Context context) {

        m_notificationMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(context, MainActivity1.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);


        Intent intent = new Intent(PlaybackService.NOTIFICATION_UPDATE);
        intent.putExtra(PlaybackService.NOTIFICATION_UPDATE, NOTIFICATION_CLOSE_ACTION);
        PendingIntent pendingIntentClose = PendingIntent.getBroadcast(this, NOTIFICATION_CLOSE_ACTION, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        intent = new Intent(PlaybackService.NOTIFICATION_UPDATE);
        intent.putExtra(PlaybackService.NOTIFICATION_UPDATE, NOTIFICATION_NEXT_ACTION);
        PendingIntent pendingIntentNext = PendingIntent.getBroadcast(this, NOTIFICATION_NEXT_ACTION, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        intent = new Intent(PlaybackService.NOTIFICATION_UPDATE);
        intent.putExtra(PlaybackService.NOTIFICATION_UPDATE, NOTIFICATION_TOOGLE_ACTION);
        PendingIntent pendingIntentPause = PendingIntent.getBroadcast(this, NOTIFICATION_TOOGLE_ACTION, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_controls);

        remoteViews.setOnClickPendingIntent(R.id.close_notification, pendingIntentClose);
        remoteViews.setOnClickPendingIntent(R.id.next_track_notification, pendingIntentNext);
        remoteViews.setOnClickPendingIntent(R.id.play_stop_notification, pendingIntentPause);


        if (mPlayList.getCurrentItem() != null) {
            Spanned notificationText = Html.fromHtml(mPlayList.getCurrentItem().getArtist() + "\n" + mPlayList.getCurrentItem().getTitle());
            remoteViews.setTextViewText(R.id.text_artist, notificationText);
        }


        nb = new NotificationCompat.Builder(context)
//                .setContent(remoteViews)
                .setSmallIcon(R.drawable.yellow_headphones_1)
                .setContentIntent(PendingIntent.getActivity(context, NOTIFICATION_ID_ALARM, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setOngoing(true);

        notification = nb.build();
        notification.contentView = remoteViews;
        m_notificationMgr.notify(NOTIFICATION_ID_ALARM, notification);


    }


    private void hideNotification(Context context) {
        m_notificationMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        m_notificationMgr.cancel(NOTIFICATION_ID_ALARM);
    }

    private Notification notification;
    private static final int NOTIFICATION_ID_ALARM = 24;
    private SwitchButtonListener switchButtonListener;


    public class SwitchButtonListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int action = intent.getIntExtra(PlaybackService.NOTIFICATION_UPDATE, 0);
            switch (action) {
                case NOTIFICATION_CLOSE_ACTION:
                    pause();
                    stopPlayer();
                    stopSelf();
                    onDestroy();
                    System.exit(1);
                    break;
                case NOTIFICATION_NEXT_ACTION:
                    onCompletion(null);
                    break;
                case NOTIFICATION_TOOGLE_ACTION:
                    intent = new Intent(PlaybackService.this, PlaybackService.class);
                    intent.setAction(PlaybackService.SERVICE_TOGGLE_PLAY);
                    startService(intent);
                    break;
            }
        }
    }

    private void updateNotification() {
        if (notification != null && notification.contentView != null) {
            notification.contentView.setImageViewResource(R.id.play_stop_notification, isPlaying() ? R.drawable.btn_playback_pause_normal_jb_dark : R.drawable.btn_playback_play_normal_jb_dark);
            m_notificationMgr.notify(NOTIFICATION_ID_ALARM, notification);
        }

    }


}
