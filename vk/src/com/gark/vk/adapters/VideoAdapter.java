package com.gark.vk.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gark.vk.R;
import com.gark.vk.db.MusicColumns;
import com.gark.vk.db.VideoColumns;


public class VideoAdapter extends CursorAdapter {

    private LayoutInflater mInflater;

    public VideoAdapter(Context context, Cursor c) {
        super(context, c, false);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        final ViewHolder listItem = (ViewHolder) view.getTag();

        final String description = getCursor().getString(cursor.getColumnIndex(VideoColumns.DESCRIPTION.getName()));
        final String title = getCursor().getString(cursor.getColumnIndex(VideoColumns.TITLE.getName()));
        final long duration = getCursor().getLong(cursor.getColumnIndex(VideoColumns.DURATION.getName()));
        final String image_url = getCursor().getString(cursor.getColumnIndex(VideoColumns.IMAGE_MEDIUM.getName()));
        final String player = getCursor().getString(cursor.getColumnIndex(VideoColumns.PLAYER.getName()));


        listItem.txtTitle.setText(Html.fromHtml(title));
        listItem.txtDuration.setText(String.valueOf(duration));
        listItem.txtArtist.setText(Html.fromHtml(description) + "\n" + image_url + "\n" + player);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.video_item, null);
        final ViewHolder listItem = new ViewHolder();
        listItem.txtArtist = (TextView) view.findViewById(R.id.video_artist);
        listItem.txtDuration = (TextView) view.findViewById(R.id.video_duration);
        listItem.txtTitle = (TextView) view.findViewById(R.id.video_title);

        view.setTag(listItem);
        return view;

    }

    public class ViewHolder {
        TextView txtTitle;
        TextView txtArtist;
        TextView txtDuration;

    }

}
