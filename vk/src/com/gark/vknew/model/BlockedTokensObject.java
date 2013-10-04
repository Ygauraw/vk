package com.gark.vknew.model;

import android.net.Uri;

import com.gark.vknew.db.BlockedTokensColumns;
import com.gark.vknew.db.VKDBSchema;

public class BlockedTokensObject implements VKDBSchema {

    public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(Tables.BLOCKED_TOKENS).build();
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.blockedTokens";
    public static final String DEFAULT_SORT = BlockedTokensColumns._ID.getName() + " ASC";


    private String tokenValue;
    private long time;
}
