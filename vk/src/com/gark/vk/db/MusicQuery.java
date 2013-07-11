package com.gark.vk.db;


public interface MusicQuery {
    int _TOKEN = 0x1;
    
    
    
    String[] PROJECTION = {
    		VKDBSchema.Tables.MUSIC + "." +
    		MusicColumns._ID.getName(),
    		MusicColumns.AID.getName(),
    		MusicColumns.ARTIST.getName(),
    		MusicColumns.TITLE.getName(),
    		MusicColumns.DURATION.getName(),
    		MusicColumns.URL.getName(),
    		MusicColumns.LYRICS_ID.getName(),
    		MusicColumns.GENRE.getName(),
            MusicColumns.IS_ACTIVE.getName(),
    		MusicColumns.ALBUM.getName()
    		
    };
    

}
