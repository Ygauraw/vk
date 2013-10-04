package com.gark.vknew.ui;

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
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gark.vknew.R;
import com.gark.vknew.adapters.MusicAdapter;
import com.gark.vknew.navigation.NavigationControllerFragment;
import com.gark.vknew.services.PlaybackService;
import com.gark.vknew.utils.StorageUtils;

import java.util.Locale;

/**
 * Created by Artem on 10.07.13.
 */
public class ControlsFragment extends NavigationControllerFragment {
    //    private TextView tempTxt;
    private ImageView imgPlayStop;
    private ImageButton btnNextTrack;
    private ImageButton btnPrevTrack;
    private SeekBar mSeekBar;
    private ServiceConnection sConn;
    private ImageView imgShuffle;
    private ImageView imgRepeat;

    private TextView currentDuration;
    private TextView totalDuration;

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

        btnNextTrack = (ImageButton) view.findViewById(R.id.next_track);
        btnPrevTrack = (ImageButton) view.findViewById(R.id.prev_track);

        currentDuration = (TextView) view.findViewById(R.id.current_duration);
        totalDuration = (TextView) view.findViewById(R.id.total_duration);

        btnPrevTrack.setOnClickListener(onClickListener);
        btnNextTrack.setOnClickListener(onClickListener);


        imgPlayStop = (ImageView) view.findViewById(R.id.play_stop);
        imgPlayStop.setOnClickListener(onClickListener);

        imgShuffle = (ImageView) view.findViewById(R.id.shuffle);
        updateShuffleButton();
        imgShuffle.setOnClickListener(onClickListener);

        imgRepeat = (ImageView) view.findViewById(R.id.repeat);
        updateRepeatButton();
        imgRepeat.setOnClickListener(onClickListener);

        return view;
    }

    final CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
//                case R.id.shuffle:
//                    StorageUtils.setShuffle(getActivity(), isChecked);
//                    break;
//                case R.id.repeat:
//                    StorageUtils.setRepeat(getActivity(), isChecked);
//                    break;
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
                case R.id.play_stop:
                    intent = new Intent(getActivity(), PlaybackService.class);
                    intent.setAction(PlaybackService.SERVICE_TOGGLE_PLAY);
                    getActivity().startService(intent);
                    break;
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
                case R.id.repeat:
                    boolean isActive = StorageUtils.getRepeat(getActivity());
                    StorageUtils.setRepeat(getActivity(), !isActive);
                    updateRepeatButton();
                    break;
                case R.id.shuffle:
                    boolean isShuffle = StorageUtils.getShuffle(getActivity());
                    StorageUtils.setShuffle(getActivity(), !isShuffle);
                    updateShuffleButton();
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
                mSeekBar.setEnabled((mService != null && mService.isPlaying()) ? true : false);
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

        onStopReceiver = new OnPauseReceiver();
        getActivity().registerReceiver(onStopReceiver, new IntentFilter(PlaybackService.SERVICE_ON_PAUSE));


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

//


    }


    private class PlaybackUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int duration = intent.getIntExtra(PlaybackService.EXTRA_DURATION, 1);
            if (duration == 1) {
                return;
            }

//            final String artist = intent.getStringExtra(PlaybackService.EXTRA_ARTIST);
//            final String title = intent.getStringExtra(PlaybackService.EXTRA_TITLE);
            int secondary = intent.getIntExtra(PlaybackService.SECONDARY_PROGRESS, 0);
            int position = intent.getIntExtra(PlaybackService.EXTRA_POSITION, 0);
//            int downloaded = intent.getIntExtra(PlaybackService.EXTRA_DOWNLOADED, 1);

            mSeekBar.setSecondaryProgress(secondary);
//            mSeekBar.setProgress(((int) (position / duration)) * 100);
            if (duration != 0) {
                mSeekBar.setProgress((position * 100 / duration));
            }

            position /= 1000;
            duration /= 1000;

            String currentDurationValue = String.format(Locale.getDefault(), MusicAdapter.TIME_FORMATTER, position / 60, position % 60);
            currentDuration.setText(currentDurationValue);

            String totalDurationValue = String.format(Locale.getDefault(), MusicAdapter.TIME_FORMATTER, duration / 60, duration % 60);
            totalDuration.setText(totalDurationValue);

//            if (artist != null && title != null && ((MainActivity1) getActivity()).getViewPager().getCurrentItem() == 0) {
//                String header = String.format(Locale.getDefault(), "%s \"%s\"", artist, title);
//                getSherlockActivity().getSupportActionBar().setTitle(Html.fromHtml(header));
//            }

        }
    }


    private class StartPlayReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            showPause();
            if (intent.getExtras() != null && intent.getExtras().containsKey(PlaybackService.SERVICE_PRESS_PLAY)) {
            } else {
                if (getActivity() != null) {
                    getActivity().setProgressBarIndeterminateVisibility(true);
                }
            }
            mSeekBar.setEnabled(true);


        }
    }

    ;


    private class OnPrepareReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            showPause();
            if (getActivity() != null) {
                getActivity().setProgressBarIndeterminateVisibility(false);
            }
        }
    }

    ;


    private class OnPauseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            showPlay();
            mSeekBar.setEnabled(false);
        }
    }

    ;

    private void showPause() {
        imgPlayStop.setImageResource(R.drawable.ic_pause_dark_tablet);
    }

    private void showPlay() {
        imgPlayStop.setImageResource(R.drawable.ic_play_dark_tablet);
    }

    private void updateRepeatButton() {
        boolean isActive = StorageUtils.getRepeat(getActivity());
        imgRepeat.setImageResource(isActive ? R.drawable.ic_repeat_one_song_dark_tablet : R.drawable.ic_repeat_dark_tablet);
    }

    private void updateShuffleButton() {
        boolean isActive = StorageUtils.getShuffle(getActivity());
        imgShuffle.setImageResource(isActive ? R.drawable.ic_shuffle_dark_selected_tablet : R.drawable.ic_shuffle_dark_tablet);
    }
}
