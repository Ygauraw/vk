package com.gark.vk.db;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.gark.vk.model.BlockedTokensObject;
import com.gark.vk.model.MusicObject;
import com.gark.vk.model.SuggestionObject;
import com.gark.vk.model.VideoObject;

import java.util.ArrayList;

public class VKProvider extends ContentProvider {

    private DatabaseHelper dbHelper;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final int MUSIC = 100;
    private static final int SUGGESTION = 200;
    private static final int VIDEO = 300;
    private static final int BLOCKED_TOKENS = 400;


    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = VKDBSchema.CONTENT_AUTHORITY;

        matcher.addURI(authority, VKDBSchema.Tables.MUSIC, MUSIC);
        matcher.addURI(authority, VKDBSchema.Tables.SUGGESTION, SUGGESTION);
        matcher.addURI(authority, VKDBSchema.Tables.VIDEO, VIDEO);
        matcher.addURI(authority, VKDBSchema.Tables.BLOCKED_TOKENS, BLOCKED_TOKENS);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MUSIC:
                return MusicObject.CONTENT_TYPE;
            case SUGGESTION:
                return SuggestionObject.CONTENT_TYPE;
            case VIDEO:
                return VideoObject.CONTENT_TYPE;
            case BLOCKED_TOKENS:
                return BlockedTokensObject.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri);
        getContext().getContentResolver().notifyChange(uri, null);
        return builder.where(selection, selectionArgs).delete(db);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.insertOrThrow(getTable(uri), null, values);
        getContext().getContentResolver().notifyChange(uri, null);
        return uri;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri);
        final Cursor result = builder.where(selection, selectionArgs).query(db, projection, sortOrder);
        result.setNotificationUri(getContext().getContentResolver(), uri);
        return result;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri);
        int result = builder.where(selection, selectionArgs).update(db, values);
        getContext().getContentResolver().notifyChange(uri, null);
        return result;

    }

    private SelectionBuilder buildSimpleSelection(Uri uri) {
        final SelectionBuilder builder = new SelectionBuilder();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MUSIC:
                return builder.table(VKDBSchema.Tables.MUSIC);
            case SUGGESTION:
                return builder.table(VKDBSchema.Tables.SUGGESTION);
            case VIDEO:
                return builder.table(VKDBSchema.Tables.VIDEO);
            case BLOCKED_TOKENS:
                return builder.table(VKDBSchema.Tables.BLOCKED_TOKENS);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }
    }

    private String getTable(final Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case MUSIC:
                return VKDBSchema.Tables.MUSIC;
            case SUGGESTION:
                return VKDBSchema.Tables.SUGGESTION;
            case VIDEO:
                return VKDBSchema.Tables.VIDEO;
            case BLOCKED_TOKENS:
                return VKDBSchema.Tables.BLOCKED_TOKENS;
            default:
                throw new UnsupportedOperationException("Unknown query uri: " + uri);
        }
    }

    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {

        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }

}
