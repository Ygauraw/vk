package com.gark.vk.services;

/**
 * Created by Artem on 10.07.13.
 */

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;

import com.gark.vk.utils.Log;

public class PlaybackService1 extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener , MediaPlayer.OnPreparedListener{

    private static final String SERVICE_PREFIX = "com.gark.vk.services.";
    public static final String SERVICE_TOGGLE_PLAY = SERVICE_PREFIX + "TOGGLE_PLAY";

    private int startId;
    private Looper serviceLooper;
    private ServiceHandler serviceHandler;
    private MediaPlayer mediaPlayer;


//    private final Handler handler = new Handler();

//    private final Runnable r = new Runnable() {
//        @Override
//        public void run() {
////            updateSeekProgress();
//        }
//    };

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

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnCompletionListener(this);

        HandlerThread thread = new HandlerThread("PlaybackService:WorkerThread");
        thread.start();

        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message message = serviceHandler.obtainMessage();
        message.arg1 = startId;
        message.obj = intent;
        serviceHandler.sendMessage(message);
        return super.onStartCommand(intent, flags, startId);
    }

    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        if (action.equals(SERVICE_TOGGLE_PLAY)) {

            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource("http://cs9-2v4.vk.me/p1/42027eeae83b20.mp3");
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.prepareAsync();
            } catch (Exception e) {
                e.printStackTrace();
            }


//            currentAction = action;
            //TODO
//            current = intent.getParcelableExtra(Playable.PLAYABLE_TYPE);
//            seekToPosition = intent.getIntExtra(EXTRA_SEEK_TO, 0);
//            playCurrent(0, 1);

            if (isPlaying()) {
                pauseAudio();
                // Get rid of the toggle intent, since we don't want it redelivered
                // on restart
                Intent emptyIntent = new Intent(intent);
                emptyIntent.setAction("");
                startService(emptyIntent);
            } else {
//                playAudio();
            }

        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
        Log.e("" + i);
    }


    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }


    synchronized private void stop() {
        if (isPrepared) {
            isPrepared = false;
//            if (proxy != null) {
//                proxy.stop();
//                proxy = null;
//            }
            mediaPlayer.stop();
        }
    }

    private void stopAudio() {
        mediaPlayer.stop();
//        btn_play.setEnabled(true);
//        btn_pause.setEnabled(false);
//        btn_stop.setEnabled(false);
//        seekBar.setProgress(0);
    }

    private void pauseAudio() {
        mediaPlayer.pause();
//        btn_play.setEnabled(true);
//        btn_pause.setEnabled(false);
    }

    private void playAudio() {
        mediaPlayer.start();
//        btn_play.setEnabled(false);
//        btn_pause.setEnabled(true);
//        btn_stop.setEnabled(true);
    }

    boolean isPrepared;

    synchronized public boolean isPlaying() {
        return isPrepared && mediaPlayer.isPlaying();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        isPrepared = true;
    }

}
