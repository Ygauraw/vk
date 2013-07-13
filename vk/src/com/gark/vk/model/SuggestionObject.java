package com.gark.vk.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.gark.vk.db.MusicColumns;
import com.gark.vk.db.VKDBSchema;

public class SuggestionObject implements VKDBSchema {

    public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(Tables.SUGGESTION).build();
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.suggestion";
    public static final String DEFAULT_SORT = MusicColumns._ID.getName() + " ASC";



}
