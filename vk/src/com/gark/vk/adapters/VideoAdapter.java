package com.gark.vk.adapters;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.gark.vk.R;
import com.gark.vk.db.MusicColumns;
import com.gark.vk.db.VideoColumns;
import com.gark.vk.utils.BitmapLruCache;


public class VideoAdapter extends CursorAdapter {

    private LayoutInflater mInflater;
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
        final String player = getCursor().getString(cursor.getColumnIndex(VideoColumns.PLAYER.getName()));


        mImageLoader.get(image_url, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                listItem.imgVideo.setImageBitmap(response.getBitmap());
            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        listItem.txtTitle.setText(Html.fromHtml(title));
        listItem.txtDuration.setText(String.valueOf(duration));
        listItem.txtArtist.setText(Html.fromHtml(description));

//        listItem.imgVideoDownloader.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
//
//                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
//                request.setAllowedOverRoaming(false);
//                request.setTitle(title);
//                request.setDescription(description);
//                request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, title + ".mp4");
//
//                dm.enqueue(request);
//
//                String downloadToastMessage = context.getString(R.string.downloading_started, artist, title);
//                Toast.makeText(context, downloadToastMessage, Toast.LENGTH_SHORT).show();
//
//            }
//        });

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

}
