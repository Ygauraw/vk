package com.gark.vk.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gark.vk.R;
import com.gark.vk.navigation.NavigationControllerFragment;
import com.gark.vk.services.PlaybackService;
import com.gark.vk.utils.Log;
import com.gark.vk.utils.PlayerUtils;

/**
 * Created by Artem on 10.07.13.
 */
public class ControlsFragment extends NavigationControllerFragment {
    private TextView tempTxt;
    private CheckBox chkPlayStop;
    private ImageButton btnNextTrack;
    private ImageButton btnPrevTrack;
    private SeekBar mSeekBar;
    private ServiceConnection sConn;
    private CheckBox chkShuffle;
    private CheckBox chkRepeat;

    private BroadcastReceiver updateReceiver;
    private BroadcastReceiver onStartReceiver;
    private BroadcastReceiver onPrepareReceiver;
    private BroadcastReceiver onStopReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.controls, null);

        mSeekBar = (SeekBar) view.findViewById(R.id.seekBar);
        mSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);

        tempTxt = (TextView) view.findViewById(R.id.text_temp);


        btnNextTrack = (ImageButton) view.findViewById(R.id.next_track);
        btnPrevTrack = (ImageButton) view.findViewById(R.id.prev_track);


        btnPrevTrack.setOnClickListener(onClickListener);
        btnNextTrack.setOnClickListener(onClickListener);


        chkPlayStop = (CheckBox) view.findViewById(R.id.play_stop);
        chkPlayStop.setOnCheckedChangeListener(onCheckedChangeListener);

        chkShuffle = (CheckBox) view.findViewById(R.id.shuffle);
        chkShuffle.setChecked(PlayerUtils.getShuffle(getActivity()));
        chkShuffle.setOnCheckedChangeListener(onCheckedChangeListener);

        chkRepeat = (CheckBox) view.findViewById(R.id.repeat);
        chkRepeat.setChecked(PlayerUtils.getRepeat(getActivity()));
        chkRepeat.setOnCheckedChangeListener(onCheckedChangeListener);

        return view;
    }

    final CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.shuffle:
                    PlayerUtils.setShuffle(getActivity(), isChecked);
                    break;
                case R.id.repeat:
                    PlayerUtils.setRepeat(getActivity(), isChecked);
                    break;
                case R.id.play_stop:
                    if (buttonView.isPressed()) {
                        intent = new Intent(getActivity(), PlaybackService.class);
                        intent.setAction(PlaybackService.SERVICE_TOGGLE_PLAY);
                        getActivity().startService(intent);
                    }
                    break;
            }

        }
    };

    final View.OnClickListener onClickListener = new View.OnClickListener() {
        Intent intent = null;

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
//                case R.id.play_stop:
//                    intent = new Intent(getActivity(), PlaybackService.class);
//                    intent.setAction(PlaybackService.SERVICE_TOGGLE_PLAY);
//                    getActivity().startService(intent);
//                    updateButton();
//                    break;
                case R.id.next_track:
                    intent = new Intent(getActivity(), PlaybackService.class);
                    intent.setAction(PlaybackService.SERVICE_PLAY_NEXT);
                    getActivity().startService(intent);
                    break;
                case R.id.prev_track:
                    intent = new Intent(getActivity(), PlaybackService.class);
                    intent.setAction(PlaybackService.SERVICE_PLAY_PREVIOUS);
                    getActivity().startService(intent);
                    break;
            }
        }
    };

    final SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            seekBar.setProgress(progress);
            if (fromUser) {
                if (mService != null && mService.isPlaying()) {
                    Intent intent = new Intent(getActivity(), PlaybackService.class);
                    intent.setAction(PlaybackService.SERVICE_SEEK_TO);
                    intent.putExtra(PlaybackService.EXTRA_SEEK_TO, progress);
                    getActivity().startService(intent);
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };


    Intent intent;
    private PlaybackService mService;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        sConn = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                PlaybackService.LocalBinder binder = (PlaybackService.LocalBinder) service;
                mService = binder.getService();
                if (mService != null) {
                    if (mService.isPlaying()) {
                        showPause();
                    } else {
                        showPlay();
                    }
                }
            }

            public void onServiceDisconnected(ComponentName name) {
                mService = null;
            }
        };
        intent = new Intent(getActivity(), PlaybackService.class);
        getActivity().bindService(intent, sConn, Context.BIND_AUTO_CREATE);


        intent = null;
        updateReceiver = new PlaybackUpdateReceiver();
        intent = getActivity().registerReceiver(updateReceiver, new IntentFilter(PlaybackService.SERVICE_UPDATE_NAME));
        if (intent != null) {
            updateReceiver.onReceive(getActivity(), intent);
        }

        onStartReceiver = new StartPlayReceiver();
        getActivity().registerReceiver(onStartReceiver, new IntentFilter(PlaybackService.SERVICE_PRESS_PLAY));

        onPrepareReceiver = new OnPrepareReceiver();
        getActivity().registerReceiver(onPrepareReceiver, new IntentFilter(PlaybackService.SERVICE_ON_PREPARE));

        onStopReceiver = new OnStopReceiver();
        getActivity().registerReceiver(onStopReceiver, new IntentFilter(PlaybackService.SERVICE_ON_STOP));
    }


    @Override
    public void onDestroyView() {

        getActivity().unbindService(sConn);

        super.onDestroyView();

        if (updateReceiver != null) {
            getActivity().unregisterReceiver(updateReceiver);
            updateReceiver = null;
        }

        if (onStartReceiver != null) {
            getActivity().unregisterReceiver(onStartReceiver);
            onStartReceiver = null;
        }

        if (onPrepareReceiver != null) {
            getActivity().unregisterReceiver(onPrepareReceiver);
            onPrepareReceiver = null;
        }

        if (onStopReceiver != null) {
            getActivity().unregisterReceiver(onStopReceiver);
            onStopReceiver = null;
        }
    }


    private class PlaybackUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int duration = intent.getIntExtra(PlaybackService.EXTRA_DURATION, 1);
            // Drop out if no duration is given (flicker?)
            if (duration == 1) {
//                Log.v(LOG_TAG, "Playback update; no duration dropout");
                return;
            }

//            mSeekBar


            final String artist = intent.getStringExtra(PlaybackService.EXTRA_ARTIST);
            final String title = intent.getStringExtra(PlaybackService.EXTRA_TITLE);
            int secondary = intent.getIntExtra(PlaybackService.SECONDARY_PROGRESS, 0);
            int position = intent.getIntExtra(PlaybackService.EXTRA_POSITION, 0);
//            int downloaded = intent.getIntExtra(PlaybackService.EXTRA_DOWNLOADED, 1);

            mSeekBar.setSecondaryProgress(secondary);
            mSeekBar.setProgress((position * 100) / duration);

            tempTxt.setText("Playback update; position = " + position + //" millsecs; " + "downloaded = " + duration + " millsecs" + "\n" +
                    "Title = " + title + ", Artist = " + artist);

//            Toast.makeText(getActivity(), "Playback update; position = " + position + " millsecs; " + "downloaded = " + duration + " millsecs", Toast.LENGTH_SHORT).show();
//            Log.v("Playback update; position = " + position + " millsecs; " + "downloaded = " + duration + " millsecs");
//            boolean isPlaying = intent.getBooleanExtra(PlaybackService.EXTRA_IS_PLAYING, false);
//            if (!changingProgress) {
//                progressBar.setMax(duration);
//                progressBar.setProgress(position);
//            }
//            progressBar.setSecondaryProgress(downloaded);

            // StringBuilder much faster than String.Format
//            StringBuilder length = new StringBuilder(13);
//            length.append(position / 60000);
//            length.append(':');
//            int secs = position / 1000 % 60;
//            if (secs < 10) {
//                length.append('0');
//            }
//            length.append(secs);
//            length.append(" / ");
//            length.append(duration / 60000);
//            length.append(':');
//            secs = duration / 1000 % 60;
//            if (secs < 10) {
//                length.append('0');
//            }
//            length.append(secs);
//            lengthText.setText(length.toString());

//            if (position > 0) {
//                // Streams have no 'downloaded' amount
//                if (downloaded == 0 || downloaded >= position) {
////                    stopPlaylistSpinners();
//                } else if (isPlaying) {
////                    startPlaylistSpinners();
//                }
//            }

//            if (isPlaying == playPauseShowsPlay) {
//                if (isPlaying) {
//                    playPauseButton.setImageResource(R.drawable.pause_button_normal);
//                    contractedPlayButton.setImageResource(R.drawable.pause_button_normal);
//                    playPauseShowsPlay = false;
//                } else {
//                    playPauseButton.setImageResource(R.drawable.play_button_normal);
//                    contractedPlayButton.setImageResource(R.drawable.play_button_normal);
//                    playPauseShowsPlay = true;
//                }
//            }
        }
    }

    private class StartPlayReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            chkPlayStop.setChecked(false);
            getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);

        }
    }

    ;


    private class OnPrepareReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            showPause();
            getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
        }
    };


    private class OnStopReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            showPlay();
        }
    }

    ;

    private void showPause() {
        chkPlayStop.setChecked(false);
    }

    private void showPlay() {
        chkPlayStop.setChecked(true);
    }
}
