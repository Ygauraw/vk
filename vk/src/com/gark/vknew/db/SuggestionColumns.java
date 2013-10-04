package com.gark.vknew.db;

import android.app.SearchManager;

import com.gark.vknew.db.VKDBSchema.DBType;

public enum SuggestionColumns {
	_ID("_id", DBType.INT),
	TEXT(SearchManager.SUGGEST_COLUMN_TEXT_1, DBType.TEXT);


	private String columnName;
	private DBType type;

	SuggestionColumns(String columnName, DBType type) {
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
