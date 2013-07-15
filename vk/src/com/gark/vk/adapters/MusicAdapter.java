package com.gark.vk.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.gark.vk.R;
import com.gark.vk.db.MusicColumns;


public class MusicAdapter extends CursorAdapter {

    private LayoutInflater mInflater;
    AnimationDrawable mAnim;


    public MusicAdapter(Context context, Cursor c) {
        super(context, c, false);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        final ViewHolder listItem = (ViewHolder) view.getTag();

        final String artist = getCursor().getString(cursor.getColumnIndex(MusicColumns.ARTIST.getName()));
        final String title = getCursor().getString(cursor.getColumnIndex(MusicColumns.TITLE.getName()));
        final long duration = getCursor().getLong(cursor.getColumnIndex(MusicColumns.DURATION.getName()));
//        final String url = getCursor().getString(cursor.getColumnIndex(MusicColumns.URL.getName()));
        final int isActive = getCursor().getInt(cursor.getColumnIndex(MusicColumns.IS_ACTIVE.getName()));

        if (isActive == 1) {
            listItem.imgIsAcive_1.setBackgroundResource(R.anim.peak_meter_1);
            listItem.imgIsAcive_2.setBackgroundResource(R.anim.peak_meter_2);
            listItem.imgIsAcive_3.setBackgroundResource(R.anim.peak_meter_3);

            mAnim = (AnimationDrawable) listItem.imgIsAcive_1.getBackground();
            if (mAnim != null) {
                mAnim.start();
            }

            mAnim = (AnimationDrawable) listItem.imgIsAcive_2.getBackground();
            if (mAnim != null) {
                mAnim.start();
            }

            mAnim = (AnimationDrawable) listItem.imgIsAcive_3.getBackground();
            if (mAnim != null) {
                mAnim.start();
            }
        } else {
            mAnim = (AnimationDrawable) listItem.imgIsAcive_1.getBackground();
            if (mAnim != null && mAnim.isRunning()) {
                mAnim.stop();

                int sdk = android.os.Build.VERSION.SDK_INT;
                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    listItem.imgIsAcive_1.setBackgroundDrawable(null);
                } else {
                    listItem.imgIsAcive_1.setBackground(null);
                }
            }


            mAnim = (AnimationDrawable) listItem.imgIsAcive_2.getBackground();
            if (mAnim != null && mAnim.isRunning()) {
                mAnim.stop();

                int sdk = android.os.Build.VERSION.SDK_INT;
                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    listItem.imgIsAcive_2.setBackgroundDrawable(null);
                } else {
                    listItem.imgIsAcive_2.setBackground(null);
                }
            }


            mAnim = (AnimationDrawable) listItem.imgIsAcive_3.getBackground();
            if (mAnim != null && mAnim.isRunning()) {
                mAnim.stop();

                int sdk = android.os.Build.VERSION.SDK_INT;
                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    listItem.imgIsAcive_3.setBackgroundDrawable(null);
                } else {
                    listItem.imgIsAcive_3.setBackground(null);
                }
            }
        }


        listItem.txtTitle.setText(Html.fromHtml(title));
        listItem.txtDuration.setText(String.valueOf(duration));
        listItem.txtArtist.setText(Html.fromHtml(artist));

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.audio_item, null);
        final ViewHolder listItem = new ViewHolder();
        listItem.txtArtist = (TextView) view.findViewById(R.id.music_artist);
        listItem.txtDuration = (TextView) view.findViewById(R.id.music_duraion);
        listItem.txtTitle = (TextView) view.findViewById(R.id.music_title);
        listItem.imgIsAcive_1 = (ImageView) view.findViewById(R.id.isActiveImage_1);
        listItem.imgIsAcive_2 = (ImageView) view.findViewById(R.id.isActiveImage_2);
        listItem.imgIsAcive_3 = (ImageView) view.findViewById(R.id.isActiveImage_3);

        view.setTag(listItem);
        return view;

    }

    public class ViewHolder {
        ImageView imgIsAcive_1;
        ImageView imgIsAcive_2;
        ImageView imgIsAcive_3;
        TextView txtTitle;
        TextView txtArtist;
        TextView txtDuration;

    }

}
