package com.gark.vk.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gark.vk.R;
import com.gark.vk.db.MusicColumns;


public class MusicAdapter extends CursorAdapter {

    private LayoutInflater mInflater;

    public MusicAdapter(Context context, Cursor c) {
        super(context, c, false);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        final ViewHolder listItem = (ViewHolder) view.getTag();

        final String artist = getCursor().getString(cursor.getColumnIndex(MusicColumns.ARTIST.getName()));
        final String title = getCursor().getString(cursor.getColumnIndex(MusicColumns.TITLE.getName()));
        final long duration = getCursor().getLong(cursor.getColumnIndex(MusicColumns.DURATION.getName()));
        final String url = getCursor().getString(cursor.getColumnIndex(MusicColumns.URL.getName()));


        listItem.txtTitle.setText(title);
        listItem.txtDuration.setText(String.valueOf(duration));
        listItem.txtArtist.setText(artist);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.audio_item, null);
        final ViewHolder listItem = new ViewHolder();
        listItem.txtArtist = (TextView) view.findViewById(R.id.music_artist);
        listItem.txtDuration = (TextView) view.findViewById(R.id.music_duraion);
        listItem.txtTitle = (TextView) view.findViewById(R.id.music_title);

        view.setTag(listItem);
        return view;

    }

    public class ViewHolder {
        TextView txtTitle;
        TextView txtArtist;
        TextView txtDuration;

    }

}
