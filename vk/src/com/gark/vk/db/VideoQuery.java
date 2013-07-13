package com.gark.vk.db;


public interface VideoQuery {
    int _TOKEN = 0x3;

    String[] PROJECTION = {
    		VKDBSchema.Tables.VIDEO + "." +
    		VideoColumns._ID.getName(),
            VideoColumns.ID.getName(),
            VideoColumns.DESCRIPTION.getName(),
            VideoColumns.TITLE.getName(),
            VideoColumns.DURATION.getName(),
            VideoColumns.OWNER_ID.getName(),
            VideoColumns.DATE.getName(),
            VideoColumns.THUMB.getName(),
            VideoColumns.IMAGE_MEDIUM.getName(),
            VideoColumns.PLAYER.getName()
    };
    

}
