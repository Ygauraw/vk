package com.gark.vk.adapters;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gark.vk.db.SuggestionColumns;
import com.gark.vk.db.SuggestionQuery;
import com.gark.vk.model.SuggestionObject;

/**
 * Created by Gark on 14.07.13.
 */
public class MySuggestionsAdapter extends CursorAdapter {

    private Context context;

    public MySuggestionsAdapter(Context context, Cursor c) {
        super(context, c, 0);
        this.context = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tv = (TextView) view;
        final int textIndex = cursor.getColumnIndex(SuggestionColumns.TEXT.getName());
        tv.setText(cursor.getString(textIndex));
    }

    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        Cursor cursor = context.getContentResolver().query(
                SuggestionObject.CONTENT_URI,
                SuggestionQuery.PROJECTION,
                SuggestionColumns.TEXT.getName() + " LIKE ?",
                new String[]{"%" + constraint.toString() + "%"},
                null);

        return cursor;
    }
}
