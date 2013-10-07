package com.gark.vknew.db;

import android.net.Uri;

public interface VKDBSchema {

	String CONTENT_AUTHORITY = "com.gark.vknew";
	Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


	interface Tables {
		String MUSIC = "MusicTable";
        String SUGGESTION = "SuggestionTable";
        String VIDEO = "VideoTable";

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
