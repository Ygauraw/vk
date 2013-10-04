package com.gark.vknew.db;


public interface SuggestionQuery {
    int _TOKEN = 0x2;
    
    
    
    String[] PROJECTION = {
    		VKDBSchema.Tables.SUGGESTION + "." +
    		SuggestionColumns._ID.getName(),
            SuggestionColumns.TEXT.getName()
    };
    

}
