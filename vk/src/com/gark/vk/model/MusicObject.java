package com.gark.vk.model;

import android.net.Uri;

import com.gark.vk.db.MusicColumns;
import com.gark.vk.db.VKDBSchema;

public class MusicObject implements VKDBSchema {

    public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(Tables.MUSIC).build();
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.apn";
    public static final String DEFAULT_SORT = MusicColumns.AID.getName() + " ASC";




    private int aid;
    private String artist;
    private String title;
    private String duration;
    private String lyrics_id;
    private String genre;
    private String album;

    public int getAid() {
        return aid;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public String getDuration() {
        return duration;
    }

    public String getLyrics_id() {
        return lyrics_id;
    }

    public String getGenre() {
        return genre;
    }

    public String getAlbum() {
        return album;
    }
}
