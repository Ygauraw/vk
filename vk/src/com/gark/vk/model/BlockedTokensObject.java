package com.gark.vk.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.gark.vk.db.BlockedTokensColumns;
import com.gark.vk.db.MusicColumns;
import com.gark.vk.db.VKDBSchema;

public class BlockedTokensObject implements VKDBSchema {

    public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(Tables.BLOCKED_TOKENS).build();
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.blockedTokens";
    public static final String DEFAULT_SORT = BlockedTokensColumns._ID.getName() + " ASC";


    private String tokenValue;
    private long time;
}
