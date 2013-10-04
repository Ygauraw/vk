package com.gark.vknew.model;

import android.net.Uri;

import com.gark.vknew.db.MusicColumns;
import com.gark.vknew.db.VKDBSchema;

public class SuggestionObject implements VKDBSchema {

    public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(Tables.SUGGESTION).build();
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.suggestion";
    public static final String DEFAULT_SORT = MusicColumns._ID.getName() + " ASC";



}
