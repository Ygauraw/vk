package com.gark.vk.receiver;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.gark.vk.R;

/**
 * Created by Artem on 18.07.13.
 */
public class DownloaderReceiver extends BroadcastReceiver {

    private DownloadManager dm;
    private NotificationManager m_notificationMgr;

    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    @Override
    public void onReceive(Context context, Intent intent) {

        dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        m_notificationMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        String action = intent.getAction();
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            Bundle extras = intent.getExtras();

            final long id = extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID);

            DownloadManager.Query query = new DownloadManager.Query();

            query.setFilterById(id);
            Cursor c = dm.query(query);
            if (c != null && c.moveToFirst()) {

                int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);

                if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                    String path = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                    String title = c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE));
                    String description = c.getString(c.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION));

                    Bitmap bitmap = null;
                    try {
                        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                        mediaMetadataRetriever.setDataSource(path);
                        byte[] img = mediaMetadataRetriever.getEmbeddedPicture();
                        bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    NotificationCompat.Builder nb = new NotificationCompat.Builder(context)
                            .setContentTitle(title + " " + description)
                            .setContentText(context.getString(R.string.downloading_complete))
                            .setTicker(context.getString(R.string.downloading_complete))
                            .setSmallIcon(R.drawable.download_icon_2)
                            .setLargeIcon(bitmap)
                            .setOngoing(false);

                    m_notificationMgr.notify((int) id, nb.build());
                }


            }
        }
    }
}
