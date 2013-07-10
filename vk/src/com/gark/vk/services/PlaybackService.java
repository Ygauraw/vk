package com.gark.vk.services;

/**
 * Created by Artem on 10.07.13.
 */

import android.app.NotificationManager;
import android.app.Service;
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
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.List;

public class PlaybackService extends Service implements OnPreparedListener, OnSeekCompleteListener, OnBufferingUpdateListener, OnCompletionListener, OnErrorListener, OnInfoListener {

    private static final String LOG_TAG = PlaybackService.class.getName();

    private static final String SERVICE_PREFIX = "com.gark.vk.services.";

    public static final String SERVICE_CHANGE_NAME = SERVICE_PREFIX + "CHANGE";
    public static final String SERVICE_CLOSE_NAME = SERVICE_PREFIX + "CLOSE";
    public static final String SERVICE_UPDATE_NAME = SERVICE_PREFIX + "UPDATE";
    public static final String SERVICE_ERROR_NAME = SERVICE_PREFIX + "ERROR";

    public static final String SERVICE_PLAY_SINGLE = SERVICE_PREFIX + "PLAY_SINGLE";
    public static final String SERVICE_PLAY_ENTRY = SERVICE_PREFIX + "PLAY_ENTRY";
    public static final String SERVICE_TOGGLE_PLAY = SERVICE_PREFIX + "TOGGLE_PLAY";
    public static final String SERVICE_BACK_30 = SERVICE_PREFIX + "BACK_30";
    public static final String SERVICE_FORWARD_30 = SERVICE_PREFIX + "FORWARD_30";
    public static final String SERVICE_SEEK_TO = SERVICE_PREFIX + "SEEK_TO";
    public static final String SERVICE_PLAY_NEXT = SERVICE_PREFIX + "PLAYNEXT";
    public static final String SERVICE_PLAY_PREVIOUS = SERVICE_PREFIX + "PLAYPREVIOUS";
    public static final String SERVICE_STOP_PLAYBACK = SERVICE_PREFIX + "STOP_PLAYBACK";
    public static final String SERVICE_STATUS = SERVICE_PREFIX + "STATUS";
    public static final String SERVICE_CLEAR_PLAYER = SERVICE_PREFIX + "CLEAR_PLAYER";

    public static final String EXTRA_ID = SERVICE_PREFIX + "ID";
    public static final String EXTRA_TITLE = SERVICE_PREFIX + "TITLE";
    public static final String EXTRA_DOWNLOADED = SERVICE_PREFIX + "DOWNLOADED";
    public static final String SECONDARY_PROGRESS = SERVICE_PREFIX + "SECONDARY";
    public static final String EXTRA_DURATION = SERVICE_PREFIX + "DURATION";
    public static final String EXTRA_POSITION = SERVICE_PREFIX + "POSITION";
    public static final String EXTRA_SEEK_TO = SERVICE_PREFIX + "SEEK_TO";
    public static final String EXTRA_IS_PLAYING = SERVICE_PREFIX + "IS_PLAYING";
    public static final String EXTRA_IS_PREPARED = SERVICE_PREFIX + "IS_PREPARED";

    public static final String EXTRA_ERROR = SERVICE_PREFIX + "ERROR";

    public static enum PLAYBACK_SERVICE_ERROR {Connection, Playback}

    private MediaPlayer mediaPlayer;
    private boolean isPrepared = false;
    private boolean markedRead;
    // Track whether we ever called start() on the media player so we don't try
    // to reset or release it. This causes a hang (ANR) on Droid X
    // http://code.google.com/p/android/issues/detail?id=959
    private boolean mediaPlayerHasStarted = false;

    //    private StreamProxy proxy;
    private NotificationManager notificationManager;
    private static final int NOTIFICATION_ID = 1;
    private int startId;
    private String currentAction;

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
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message message = serviceHandler.obtainMessage();
        message.arg1 = startId;
        message.obj = intent;
        serviceHandler.sendMessage(message);
        return super.onStartCommand(intent, flags, startId);

    }

    //    @Override
//    public void onStart(Intent intent, int startId) {
//        Log.d(LOG_TAG, "OnStart");
//        super.onStart(intent, startId);
//        Message message = serviceHandler.obtainMessage();
//        message.arg1 = startId;
//        message.obj = intent;
//        serviceHandler.sendMessage(message);
//    }

    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        if (action.equals(SERVICE_PLAY_SINGLE) || action.equals(SERVICE_PLAY_ENTRY)) {
            currentAction = action;
            //TODO
//            current = intent.getParcelableExtra(Playable.PLAYABLE_TYPE);
//            seekToPosition = intent.getIntExtra(EXTRA_SEEK_TO, 0);
            playCurrent(0, 1);
        } else if (action.equals(SERVICE_TOGGLE_PLAY)) {
            if (isPlaying()) {
                pause();
                // Get rid of the toggle intent, since we don't want it redelivered
                // on restart
                Intent emptyIntent = new Intent(intent);
                emptyIntent.setAction("");
                startService(emptyIntent);
                //TODO
//            } else if (current != null) {
//                if (isPrepared) {
//                    play();
//                } else {
//                    playCurrent(0, 1);
//                }
            } else {
                currentAction = SERVICE_PLAY_ENTRY;
                errorCount = 0;
                playFirstUnreadEntry();
            }
        }
// else if (action.equals(SERVICE_BACK_30)) {
//            seekRelative(-30000);
//        } else if (action.equals(SERVICE_FORWARD_30)) {
//            seekRelative(30000);
//        }
        else if (action.equals(SERVICE_SEEK_TO)) {
            seekTo(intent.getIntExtra(EXTRA_SEEK_TO, 0));
        } else if (action.equals(SERVICE_PLAY_NEXT)) {
            //TODO
//            playNextEntry();
        } else if (action.equals(SERVICE_PLAY_PREVIOUS)) {
//            playPreviousEntry();
        } else if (action.equals(SERVICE_STOP_PLAYBACK)) {
            stopSelfResult(startId);
        } else if (action.equals(SERVICE_STATUS)) {
            updateProgress();
        } else if (action.equals(SERVICE_CLEAR_PLAYER)) {
            if (!isPlaying()) {
                stopSelfResult(startId);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    private boolean playCurrent(int startingErrorCount, int startingWaitTime) {
        errorCount = startingErrorCount;
        connectionErrorWaitTime = startingWaitTime;
        while (errorCount < ERROR_RETRY_COUNT) {
            try {
                //TODO
                prepareThenPlay("http://cs9-2v4.vk.me/p1/42027eeae83b20.mp3", true);
                return true;
            } catch (UnknownHostException e) {
                Log.w(LOG_TAG, "Unknown host in playCurrent");
                handleConnectionError();
            } catch (ConnectException e) {
                Log.w(LOG_TAG, "Connect exception in playCurrent");
                handleConnectionError();
            } catch (IOException e) {
                //TODO
//                Log.e(LOG_TAG, "IOException on playlist entry " + current.getId(), e);
                incrementErrorCount();
            }
        }

        return false;
    }

    //TODO
//    private void playNextEntry() {
//        do {
//            long id = current.getId();
//            if (id != -1) {
//                current = playlist.getNextEntry(current.getId());
//            } else {
//                current = playlist.getFirstUnreadEntry();
//            }
//        } while (current != null && !playCurrent(0, 1));
//    }
//
//    private void playPreviousEntry() {
//        do {
//            current = playlist.getPreviousEntry(current.getId());
//        } while (current != null && !playCurrent(0, 1));
//    }
//
    private void playFirstUnreadEntry() {
        playCurrent(0, 1);
//        do {
//            current = playlist.getFirstUnreadEntry();
//        } while (current != null && !playCurrent(0, 1));
//
//        if (current == null) {
//            stopSelfResult(startId);
//        }
    }
//
//    private void finishEntryAndPlayNext() {
//        if (current.getId() >= 0 && !markedRead) {
//            playlist.markAsRead(current.getId());
//        }
//
//        do {
//            current = playlist.getNextEntry(current.getId());
//        } while (current != null && !playCurrent(0, 1));
//
//        if (current == null) {
//            stopSelfResult(startId);
//        }
//    }

    synchronized private int getPosition() {
        if (isPrepared) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    synchronized public boolean isPlaying() {
        return isPrepared && mediaPlayer.isPlaying();
    }

    synchronized private void seekRelative(int pos) {
        if (isPrepared) {
            seekToPosition = 0;
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + pos);
        }
    }

    synchronized private void seekTo(int pos) {
        if (isPrepared) {
            seekToPosition = 0;
            mediaPlayer.seekTo((pos * mediaPlayer.getDuration()) / 100);
        }
    }

    private void prepareThenPlay(String url, boolean stream) throws IllegalArgumentException, IllegalStateException, IOException {
//        Log.d(LOG_TAG, "playNew");
        // First, clean up any existing audio.
        stop();


        Log.d(LOG_TAG, "listening to " + url + " stream=" + stream);
        String playUrl = url;
        // From 2.2 on (SDK ver 8), the local mediaplayer can handle Shoutcast
        // streams natively. Let's detect that, and not proxy.
        int sdkVersion = 0;
        try {
            sdkVersion = Integer.parseInt(Build.VERSION.SDK);
        } catch (NumberFormatException ignored) {
        }

        //TODO
//        if (stream && sdkVersion < 8) {
//            if (proxy == null) {
//                proxy = new StreamProxy();
//                proxy.init();
//                proxy.start();
//            }
//            playUrl = String.format("http://127.0.0.1:%d/%s",
//                    proxy.getPort(), url);
//        }

        // We only have to mark an item read on playlist items,
        // so set markedRead to false only when a playlist entry
        markedRead = !currentAction.equals(SERVICE_PLAY_ENTRY);
        synchronized (this) {
            Log.d(LOG_TAG, "reset: " + playUrl);
            mediaPlayer.reset();
            mediaPlayer.setDataSource(playUrl);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            Log.d(LOG_TAG, "Preparing: " + playUrl);
            mediaPlayer.prepareAsync();
            Log.d(LOG_TAG, "Waiting for prepare");
        }
    }

    synchronized private void play() {
        //TODO
//        if (!isPrepared || current == null) {
//            Log.e(LOG_TAG, "play - not prepared");
//            return;
//        }
//        Log.d(LOG_TAG, "play " + current.getId());

        mediaPlayer.start();
        mediaPlayerHasStarted = true;

        //TODO
//        CharSequence contentText = current.getTitle();
//        Notification notification =
//                new Notification(R.drawable.stat_notify_musicplayer,
//                        contentText,
//                        System.currentTimeMillis());
//        notification.flags = Notification.FLAG_NO_CLEAR
//                | Notification.FLAG_ONGOING_EVENT;
//        Context context = getApplicationContext();
//        CharSequence title = getString(R.string.app_name);
//        Intent notificationIntent;
//        if (current.getActivityData() != null) {
//            notificationIntent = new Intent(this, current.getActivity());
//            notificationIntent.putExtra(Constants.EXTRA_ACTIVITY_DATA,
//                    current.getActivityData());
//            notificationIntent.putExtra(Constants.EXTRA_DESCRIPTION,
//                    R.string.msg_main_subactivity_nowplaying);
//        } else {
//            notificationIntent = new Intent(this, NewsListActivity.class);
//        }
//        notificationIntent.setAction(Intent.ACTION_VIEW);
//        notificationIntent.addCategory(Intent.CATEGORY_DEFAULT);
//        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
//                notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
//        notification.setLatestEventInfo(context, title, contentText, contentIntent);
//        notificationManager.notify(NOTIFICATION_ID, notification);

        // Change broadcasts are sticky, so when a new receiver connects, it will
        // have the data without polling.
        if (lastChangeBroadcast != null) {
            getApplicationContext().removeStickyBroadcast(lastChangeBroadcast);
        }
        lastChangeBroadcast = new Intent(SERVICE_CHANGE_NAME);
//        lastChangeBroadcast.putExtra(EXTRA_TITLE, current.getTitle());
//        lastChangeBroadcast.putExtra(EXTRA_ID, current.getId());
        getApplicationContext().sendStickyBroadcast(lastChangeBroadcast);

//        if (current != null && current.getUrl() != null) {
//            Tracker.PlayEvent e = new Tracker.PlayEvent(current.getUrl());
//            Tracker.instance(getApplication()).trackLink(e);
//        }
    }

    synchronized private void pause() {
        Log.d(LOG_TAG, "pause");
        if (isPrepared) {
            mediaPlayer.pause();
        }
        notificationManager.cancel(NOTIFICATION_ID);

//        if (current != null) {
//            Tracker.PauseEvent e = new Tracker.PauseEvent(current.getUrl());
//            Tracker.instance(getApplication()).trackLink(e);
//        }
    }

    synchronized private void stop() {
        Log.d(LOG_TAG, "stop");
        if (isPrepared) {
            isPrepared = false;
//            if (proxy != null) {
//                proxy.stop();
//                proxy = null;
//            }
            mediaPlayer.stop();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(LOG_TAG, "Prepared");
        synchronized (this) {
            if (mediaPlayer != null) {
                isPrepared = true;
            }
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
        super.onDestroy();
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

        synchronized (this) {
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
        }

        serviceLooper.quit();

        notificationManager.cancel(NOTIFICATION_ID);
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
        Log.w(LOG_TAG, "onComplete()");

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
        Log.w(LOG_TAG, "onError(" + what + ", " + extra + ")");
        synchronized (this) {
            if (!isPrepared) {
                // This file was not good and MediaPlayer quit
                Log.w(LOG_TAG,
                        "MediaPlayer refused to play current item. Bailing on prepare.");
            }
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
        Log.w(LOG_TAG, "onInfo(" + arg1 + ", " + arg2 + ")");
        return false;
    }
}
