package com.gark.vk.db;

import com.gark.vk.db.VKDBSchema.DBType;

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
    PLAYER("player", DBType.TEXT);

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
