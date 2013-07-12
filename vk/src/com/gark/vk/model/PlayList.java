package com.gark.vk.model;

import android.content.Context;

import com.gark.vk.utils.PlayerUtils;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Artem on 11.07.13.
 */
public class PlayList {

    private ArrayList<MusicObject> playList;
    private int currentPosition;
    private Context context;


    public PlayList(Context context) {
        this.context = context;
    }

    public ArrayList<MusicObject> getPlayList() {
        return playList;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setPlayList(ArrayList<MusicObject> playList) {
        this.playList = playList;
    }

    public MusicObject getCurrentItem() {
        return playList.get(currentPosition);
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    public void resetPosition() {
        currentPosition = 0;
    }

    public void moveToNextTrack() {
        boolean isRepeat = PlayerUtils.getRepeat(context);
        boolean isShuffle = PlayerUtils.getShuffle(context);

        // - -
        if (!isRepeat && !isShuffle) {
            if (currentPosition == (playList.size() - 1)) {
                currentPosition = 0;
            } else {
                currentPosition++;
            }
        }
        // + -
        else if (isRepeat && !isShuffle) {

        }
        // - +
        else if (!isRepeat && isShuffle) {
            Random random = new Random();
            currentPosition = random.nextInt(playList.size() - 1);
        }

        // + +
        else if (!isRepeat && isShuffle) {
            Random random = new Random();
            currentPosition = random.nextInt(playList.size() - 1);
        }


    }

    public void moveToPreviousTrack() {
        ///TODO
        if (currentPosition != 0) {
            currentPosition--;
        }
    }
}