package com.gark.vk.services;

/**
 * Created by Artem on 10.07.13.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.gark.vk.R;
import com.gark.vk.db.MusicColumns;
import com.gark.vk.model.MusicObject;
import com.gark.vk.model.PlayList;
import com.gark.vk.ui.MainActivity;

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
    public static final String SERVICE_ON_STOP = SERVICE_PREFIX + "ON_STOP";

    public static final String SERVICE_PLAY_PLAYLIST = SERVICE_PREFIX + "PLAYLIST";
    public static final String SERVICE_PLAY_SINGLE = SERVICE_PREFIX + "PLAY_SINGLE";
    public static final String SERVICE_PLAY_ENTRY = SERVICE_PREFIX + "PLAY_ENTRY";
    public static final String SERVICE_TOGGLE_PLAY = SERVICE_PREFIX + "TOGGLE_PLAY";
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

    public static enum PLAYBACK_SERVICE_ERROR {Connection, Playback}

    private NotificationManager m_notificationMgr;
    private NotificationCompat.Builder nb;


    private MediaPlayer mediaPlayer;
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
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);

        mPlayList = new PlayList(this);
        mPlayList.resetPosition();

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
            } else if (action.equals(SERVICE_SEEK_TO)) {
                seekTo(intent.getIntExtra(EXTRA_SEEK_TO, 0));
            } else if (action.equals(SERVICE_PLAY_NEXT)) {
                mPlayList.moveToNextTrack();
                playCurrent(0, 1);
            } else if (action.equals(SERVICE_PLAY_PREVIOUS)) {
                mPlayList.moveToPreviousTrack();
                playCurrent(0, 1);
            } else if (action.equals(SERVICE_STOP_PLAYBACK)) {
                stopSelfResult(startId);
            } else if (action.equals(SERVICE_PLAY_PLAYLIST)) {
                ArrayList<MusicObject> playList = intent.getParcelableArrayListExtra(SERVICE_PLAY_PLAYLIST);
                if (playList != null && playList.size() > 0)
                    mPlayList.setPlayList(playList);
            }
        }
    }

    private void showActiveTrack() {
        String aid = mPlayList.getCurrentItem().getAid();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MusicColumns.IS_ACTIVE.getName(), 1);
        asyncQueryHandler.startUpdate(0, null, MusicObject.CONTENT_URI, contentValues, MusicColumns.AID.getName() + "=?", new String[]{aid});
    }

    private void hideActiveTrack() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MusicColumns.IS_ACTIVE.getName(), 0);
        asyncQueryHandler.startUpdate(0, null, MusicObject.CONTENT_URI, contentValues, null, null);
    }


    private boolean playCurrent(int startingErrorCount, int startingWaitTime) {
        errorCount = startingErrorCount;
        connectionErrorWaitTime = startingWaitTime;
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
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    synchronized public boolean isPlaying() {
        return isPrepared && mediaPlayer.isPlaying();
    }

    synchronized private void seekTo(int pos) {
        if (isPrepared) {
            seekToPosition = 0;
            mediaPlayer.seekTo((pos * mediaPlayer.getDuration()) / 100);
        }
    }

    private void prepareThenPlay() throws IllegalArgumentException, IllegalStateException, IOException {
        stop();

        Intent intent = new Intent(SERVICE_PRESS_PLAY);
        getApplicationContext().sendBroadcast(intent);

        try {
            String playUrl = mPlayList.getCurrentItem().getUrl();
            mediaPlayer.reset();
            mediaPlayer.setDataSource(playUrl);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    synchronized private void play() {
        if (!isPrepared) {
            Log.e(LOG_TAG, "play - not prepared");
            return;
        }

        showActiveTrack();
        showNotification(this);

        mediaPlayer.start();
        mediaPlayerHasStarted = true;
    }

    synchronized private void pause() {
        hideActiveTrack();
        hideNotification(this);

        Log.d(LOG_TAG, "pause");
        if (isPrepared) {
            mediaPlayer.pause();
        }
    }

    synchronized private void stop() {
        hideActiveTrack();
        hideNotification(this);


        Log.d(LOG_TAG, "stop");
        if (isPrepared) {
            isPrepared = false;
            mediaPlayer.stop();
            mediaPlayer.seekTo(0);
            seekToPosition = 0;
        }

//        Intent intent = new Intent(SERVICE_ON_STOP);
//        getApplicationContext().sendBroadcast(intent);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        showActiveTrack();

        showNotification(this);

        Intent intent = new Intent(SERVICE_ON_PREPARE);
        intent.putExtra(SERVICE_ON_PREPARE, mPlayList.getCurrentPosition());
        getApplicationContext().sendBroadcast(intent);


        Log.d(LOG_TAG, "Prepared");
        if (mediaPlayer != null) {
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

        Toast.makeText(this, "destroy", Toast.LENGTH_SHORT).show();
        Log.e("destroy", "destroy");

        super.onDestroy();

        mPlayList.resetPosition();

        Log.w(LOG_TAG, "Service exiting");

        stop();

        if (updateProgressThread != null) {
            updateProgressThread.interrupt();
            try {
                updateProgressThread.join(1000);
            } catch (InterruptedException e) {
                Log.e(LOG_TAG, "", e);
            }
        }

//        synchronized (this) {
        if (mediaPlayer != null) {
            if (mediaPlayerHasStarted) {
                mediaPlayer.release();
            } else {
                mediaPlayer.setOnBufferingUpdateListener(null);
                mediaPlayer.setOnCompletionListener(null);
                mediaPlayer.setOnErrorListener(null);
                mediaPlayer.setOnInfoListener(null);
                mediaPlayer.setOnPreparedListener(null);
                mediaPlayer.setOnSeekCompleteListener(null);
            }
            mediaPlayer = null;
        }
//        }

        serviceLooper.quit();

//        notificationManager.cancel(NOTIFICATION_ID);
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
        if (mediaPlayer == null)
            return;

        if (isPrepared) {

            if (lastUpdateBroadcast != null) {
                getApplicationContext().removeStickyBroadcast(lastUpdateBroadcast);
                lastUpdateBroadcast = null;
            }

            int duration = mediaPlayer.getDuration();
            seekToPosition = mediaPlayer.getCurrentPosition();
            if (!markedRead) {
                if (seekToPosition > duration / 10) {
                    markedRead = true;
//                    playlist.markAsRead(current.getId());
                }
            }

            Intent tempUpdateBroadcast = new Intent(SERVICE_UPDATE_NAME);
            tempUpdateBroadcast.putExtra(EXTRA_DURATION, duration);
            tempUpdateBroadcast.putExtra(EXTRA_DOWNLOADED, (int) ((lastBufferPercent / 100.0) * duration));
            tempUpdateBroadcast.putExtra(SECONDARY_PROGRESS, lastBufferPercent);
            tempUpdateBroadcast.putExtra(EXTRA_POSITION, seekToPosition);
            tempUpdateBroadcast.putExtra(EXTRA_IS_PLAYING, mediaPlayer.isPlaying());
            if (mPlayList.getCurrentItem() != null) {
                tempUpdateBroadcast.putExtra(EXTRA_ARTIST, mPlayList.getCurrentItem().getArtist());
                tempUpdateBroadcast.putExtra(EXTRA_TITLE, mPlayList.getCurrentItem().getTitle());
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
        Toast.makeText(this, "onComplete", Toast.LENGTH_SHORT).show();


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
            Log.e(LOG_TAG, "Connection failed.  Resetting mediaPlayer" +
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
            mediaPlayer.reset();
        } else {
            Log.w(LOG_TAG, "Connection error. Waiting for " +
                    connectionErrorWaitTime + " milliseconds.");
        }
        SystemClock.sleep(connectionErrorWaitTime);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {

        Toast.makeText(this, "onError", Toast.LENGTH_SHORT).show();

        Log.w(LOG_TAG, "onError(" + what + ", " + extra + ")");
        if (!isPrepared) {
            Log.w(LOG_TAG, "MediaPlayer refused to play current item. Bailing on prepare.");
        }

        isPrepared = false;
        mediaPlayer.reset();

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

        Intent notificationIntent = new Intent(context, MainActivity.class);
//        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_controls);
        remoteViews.setOnClickPendingIntent(R.id.close_notification, pendingIntent);
//        remoteViews.setImageViewResource(R.drawable.ic_launcher, R.drawable.ic_launcher);
//        remoteViews.setTextViewText(R.string.app_name, "sdfsdfsdfsd");

//        Bundle bundle = new Bundle();
//        String alarmSet = context.getString(R.string.app_name);


//        notificationIntent.putExtras(bundle);
        nb = new NotificationCompat.Builder(context)
                .setContent(remoteViews)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("My notification")
                .setContentText("Hello World!")
//                .setContentIntent(PendingIntent.getActivity(context, NOTIFICATION_ID_ALARM, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT))
                .setOngoing(true);
        m_notificationMgr.notify(NOTIFICATION_ID_ALARM, nb.build());

    }

    final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    private void hideNotification(Context context) {
        m_notificationMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        m_notificationMgr.cancel(NOTIFICATION_ID_ALARM);



    }

    private static final int NOTIFICATION_ID_ALARM = 24;


}
