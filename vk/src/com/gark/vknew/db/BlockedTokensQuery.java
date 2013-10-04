package com.gark.vknew.db;


public interface BlockedTokensQuery {
    int _TOKEN = 0x15;


    String[] PROJECTION = {
            VKDBSchema.Tables.BLOCKED_TOKENS + "." +
            BlockedTokensColumns._ID.getName(),
            BlockedTokensColumns.BLOCKED_TIME.getName(),
            BlockedTokensColumns.TOKEN_VALUE.getName()

    };


}
