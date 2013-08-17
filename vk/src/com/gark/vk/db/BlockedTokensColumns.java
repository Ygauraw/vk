package com.gark.vk.db;

import com.gark.vk.db.VKDBSchema.DBType;

public enum BlockedTokensColumns {
	_ID("_id", DBType.INT),
	TOKEN_VALUE("token_value", DBType.INT),
	BLOCKED_TIME("blocked_time", DBType.TEXT);


	private String columnName;
	private DBType type;

	BlockedTokensColumns(String columnName, DBType type) {
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
