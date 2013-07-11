package com.gark.vk.db;

import com.gark.vk.db.VKDBSchema.DBType;

public enum MusicColumns {
	_ID("_id", DBType.INT),
	AID("aid", DBType.INT),
	ARTIST("artist", DBType.TEXT),
	TITLE("title", DBType.TEXT),
	DURATION("duration", DBType.TEXT),
	URL("url", DBType.TEXT),
	LYRICS_ID("lyrics_id", DBType.TEXT),
	GENRE("genre", DBType.TEXT),
	ALBUM("album", DBType.TEXT),
    IS_ACTIVE("is_active", DBType.TEXT),
	;


	private String columnName;
	private DBType type;

	MusicColumns(String columnName, DBType type) {
		this.columnName = columnName;
		this.type = type;
	}

	public String getName() {
		return columnName;
	}

	public DBType getType() {
		return type;
	}

}
