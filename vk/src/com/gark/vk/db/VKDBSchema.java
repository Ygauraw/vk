package com.gark.vk.db;

import android.net.Uri;

public interface VKDBSchema {

	String CONTENT_AUTHORITY = "com.gark.vk";
	Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


	interface Tables {
		String MUSIC = "MusicTable";
        String SUGGESTION = "SuggestionTable";
        String VIDEO = "VideoTable";
        String BLOCKED_TOKENS = "BlockedTokensTable";

	}

	enum DBType {
		INT(" INTEGER,"), FLOAT(" FLOAT,"), TEXT(" TEXT,"), NUMERIC(" NUMERIC,");

		private String name;

		DBType(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

}
