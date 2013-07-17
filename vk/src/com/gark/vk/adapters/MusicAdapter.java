package com.gark.vk.adapters;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.gark.vk.R;
import com.gark.vk.db.MusicColumns;

import java.util.Locale;


public class MusicAdapter extends CursorAdapter {

    private LayoutInflater mInflater;
    private AnimationDrawable mAnim;
    public static final String TIME_FORMATTER = "%02d:%02d";
    private DownloadManager dm;
    private long enqueue;


    public MusicAdapter(Context context, Cursor c) {
        super(context, c, false);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        final ViewHolder listItem = (ViewHolder) view.getTag();

        final String artist = getCursor().getString(cursor.getColumnIndex(MusicColumns.ARTIST.getName()));
        final String title = getCursor().getString(cursor.getColumnIndex(MusicColumns.TITLE.getName()));
        final long duration = getCursor().getLong(cursor.getColumnIndex(MusicColumns.DURATION.getName()));
        final String url = getCursor().getString(cursor.getColumnIndex(MusicColumns.URL.getName()));
        final int isActive = getCursor().getInt(cursor.getColumnIndex(MusicColumns.IS_ACTIVE.getName()));

        if (isActive == 1) {
            listItem.imgIsActive_1.setBackgroundResource(R.anim.peak_meter_1);
            listItem.imgIsActive_2.setBackgroundResource(R.anim.peak_meter_2);
            listItem.imgIsActive_3.setBackgroundResource(R.anim.peak_meter_3);

            mAnim = (AnimationDrawable) listItem.imgIsActive_1.getBackground();
            if (mAnim != null) {
                mAnim.start();
            }

            mAnim = (AnimationDrawable) listItem.imgIsActive_2.getBackground();
            if (mAnim != null) {
                mAnim.start();
            }

            mAnim = (AnimationDrawable) listItem.imgIsActive_3.getBackground();
            if (mAnim != null) {
                mAnim.start();
            }
        } else {
            mAnim = (AnimationDrawable) listItem.imgIsActive_1.getBackground();
            if (mAnim != null && mAnim.isRunning()) {
                mAnim.stop();

                int sdk = android.os.Build.VERSION.SDK_INT;
                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    listItem.imgIsActive_1.setBackgroundDrawable(null);
                } else {
                    listItem.imgIsActive_1.setBackground(null);
                }
            }


            mAnim = (AnimationDrawable) listItem.imgIsActive_2.getBackground();
            if (mAnim != null && mAnim.isRunning()) {
                mAnim.stop();

                int sdk = android.os.Build.VERSION.SDK_INT;
                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    listItem.imgIsActive_2.setBackgroundDrawable(null);
                } else {
                    listItem.imgIsActive_2.setBackground(null);
                }
            }


            mAnim = (AnimationDrawable) listItem.imgIsActive_3.getBackground();
            if (mAnim != null && mAnim.isRunning()) {
                mAnim.stop();

                int sdk = android.os.Build.VERSION.SDK_INT;
                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    listItem.imgIsActive_3.setBackgroundDrawable(null);
                } else {
                    listItem.imgIsActive_3.setBackground(null);
                }
            }
        }


        listItem.txtTitle.setText(Html.fromHtml(title));

        String durationValue = String.format(Locale.getDefault(), TIME_FORMATTER, duration / 60, duration % 60);
//        String durationValue = duration/60 + ":" + duration%60;

        listItem.txtDuration.setText(durationValue);
        listItem.txtArtist.setText(Html.fromHtml(artist));
        listItem.downloader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                //Set whether this download may proceed over a roaming connection.
                request.setAllowedOverRoaming(false);
                request.setTitle(title);
                request.setDescription(artist);
                request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, title + " " + artist  + ".mp3");

                enqueue = dm.enqueue(request);


//                Toast.makeText(context, artist, Toast.LENGTH_SHORT).show();

            }
        });

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.audio_item, null);
        final ViewHolder listItem = new ViewHolder();
        listItem.txtArtist = (TextView) view.findViewById(R.id.music_artist);
        listItem.txtDuration = (TextView) view.findViewById(R.id.music_duraion);
        listItem.txtTitle = (TextView) view.findViewById(R.id.music_title);
        listItem.imgIsActive_1 = (ImageView) view.findViewById(R.id.isActiveImage_1);
        listItem.imgIsActive_2 = (ImageView) view.findViewById(R.id.isActiveImage_2);
        listItem.imgIsActive_3 = (ImageView) view.findViewById(R.id.isActiveImage_3);
        listItem.downloader = (ImageView) view.findViewById(R.id.download);

        view.setTag(listItem);
        return view;

    }

    public class ViewHolder {
        ImageView imgIsActive_1;
        ImageView imgIsActive_2;
        ImageView imgIsActive_3;
        ImageView downloader;
        TextView txtTitle;
        TextView txtArtist;
        TextView txtDuration;

    }

}
