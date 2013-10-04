package com.gark.vknew.db;

import com.gark.vknew.db.VKDBSchema.DBType;

public enum VideoColumns {
	_ID("_id", DBType.INT),
	ID("id", DBType.TEXT),
	DESCRIPTION("description", DBType.TEXT),
	TITLE("title", DBType.TEXT),
	DURATION("duration", DBType.TEXT),
    OWNER_ID("owner_id", DBType.TEXT),
    DATE("date", DBType.TEXT),
    THUMB("thumb", DBType.TEXT),
    IMAGE_MEDIUM("image_medium", DBType.TEXT),
    PLAYER("player", DBType.TEXT),
    MP4_240("MP4_240", DBType.TEXT),
    MP4_360("MP4_360", DBType.TEXT),
    MP4_480("MP4_480", DBType.TEXT),
    MP4_720("MP4_720", DBType.TEXT),
    EXTERNAL("EXTERNAL", DBType.TEXT);


//    private static final String MP4_720 = "mp4_720";
//    private static final String EXTERNAL = "external";

	private String columnName;
	private DBType type;

	VideoColumns(String columnName, DBType type) {
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
