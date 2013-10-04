package com.gark.vknew.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.gark.vknew.db.MusicColumns;
import com.gark.vknew.db.VKDBSchema;

public class MusicObject implements VKDBSchema, Parcelable {

    public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(Tables.MUSIC).build();
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.apn";
    public static final String DEFAULT_SORT = MusicColumns._ID.getName() + " ASC";


    private String aid;
    private String artist;
    private String title;
    private String duration;
    private String lyrics_id;
    private String genre;
    private String album;


    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    private String url;


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


    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setLyrics_id(String lyrics_id) {
        this.lyrics_id = lyrics_id;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public MusicObject() {

    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getAid() {
        return aid;
    }

    protected MusicObject(Parcel in) {
        aid = in.readString();
        artist = in.readString();
        title = in.readString();
        duration = in.readString();
        lyrics_id = in.readString();
        genre = in.readString();
        album = in.readString();
        url = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(aid);
        dest.writeString(artist);
        dest.writeString(title);
        dest.writeString(duration);
        dest.writeString(lyrics_id);
        dest.writeString(genre);
        dest.writeString(album);
        dest.writeString(url);
    }

    public static final Parcelable.Creator<MusicObject> CREATOR = new Parcelable.Creator<MusicObject>() {
        public MusicObject createFromParcel(Parcel in) {
            return new MusicObject(in);
        }

        public MusicObject[] newArray(int size) {
            return new MusicObject[size];
        }
    };
}
