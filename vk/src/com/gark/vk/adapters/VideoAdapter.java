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

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.gark.vk.R;
import com.gark.vk.db.VideoColumns;
import com.gark.vk.utils.BitmapLruCache;

import java.util.Locale;


public class VideoAdapter extends CursorAdapter {

    private LayoutInflater mInflater;
    public static final String VIDEO_TIME_FORMATTER_FULL = "%01d:%02d:%02d";
    public static final String VIDEO_TIME_FORMATTER_MIDDLE = "%02d:%02d";
    public static final String VIDEO_TIME_FORMATTER_SHORT = "%1d:%02d";

    private ImageLoader mImageLoader;
    private RequestQueue mRequestQ;


    public VideoAdapter(Context context, Cursor c) {
        super(context, c, false);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRequestQ = Volley.newRequestQueue(context);
        mImageLoader = new ImageLoader(mRequestQ, new BitmapLruCache(context));


    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        final ViewHolder listItem = (ViewHolder) view.getTag();

        final String description = getCursor().getString(cursor.getColumnIndex(VideoColumns.DESCRIPTION.getName()));
        final String title = getCursor().getString(cursor.getColumnIndex(VideoColumns.TITLE.getName()));
        final long duration = getCursor().getLong(cursor.getColumnIndex(VideoColumns.DURATION.getName()));
        final String image_url = getCursor().getString(cursor.getColumnIndex(VideoColumns.IMAGE_MEDIUM.getName()));

        mImageLoader.get(image_url, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                listItem.imgVideo.setImageBitmap(response.getBitmap());
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                listItem.imgVideo.setImageResource(R.drawable.ic_movie_default);
            }
        });

        listItem.txtTitle.setText(Html.fromHtml(title));
        listItem.txtArtist.setText(Html.fromHtml(description));
        listItem.txtDuration.setText(getDuration(duration));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.video_item, null);
        final ViewHolder listItem = new ViewHolder();
        listItem.txtArtist = (TextView) view.findViewById(R.id.video_artist);
        listItem.txtDuration = (TextView) view.findViewById(R.id.video_duration);
        listItem.txtTitle = (TextView) view.findViewById(R.id.video_title);
        listItem.imgVideo = (ImageView) view.findViewById(R.id.video_img);

        view.setTag(listItem);
        return view;

    }

    public class ViewHolder {
        ImageView imgVideo;
        TextView txtTitle;
        TextView txtArtist;
        TextView txtDuration;

    }

    private String getDuration(long duration) {
        int seconds = (int) (duration % 60);
        duration /= 60;
        int minutes = (int) (duration % 60);
        duration /= 60;
        int hours = (int) (duration % 24);

        String result = null;

        if (hours > 0) {
            result = String.format(Locale.getDefault(), VIDEO_TIME_FORMATTER_FULL, hours, minutes, seconds);
        } else if (hours <= 0 && minutes >= 10) {
            result = String.format(Locale.getDefault(), VIDEO_TIME_FORMATTER_MIDDLE, minutes, seconds);
        } else if (hours <= 0 && minutes < 10) {
            result = String.format(Locale.getDefault(), VIDEO_TIME_FORMATTER_SHORT, minutes, seconds);
        }
        return result;
    }

}
