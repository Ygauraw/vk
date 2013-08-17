package com.gark.vk.receiver;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.gark.vk.R;

/**
 * Created by Artem on 18.07.13.
 */
public class DownloaderReceiver extends BroadcastReceiver {

    private DownloadManager dm;
    private NotificationManager m_notificationMgr;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (android.os.Build.VERSION.SDK_INT < 11) {
            dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            m_notificationMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            try {
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

                            NotificationCompat.Builder nb = new NotificationCompat.Builder(context)
                                    .setContentTitle(title + " " + description)
                                    .setContentText(context.getString(R.string.downloading_complete))
                                    .setTicker(context.getString(R.string.downloading_complete))
                                    .setSmallIcon(R.drawable.download_icon_2)
                                    .setOngoing(false);

                            m_notificationMgr.notify((int) id, nb.build());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Bundle extras = intent.getExtras();
        int status = extras.getInt(DownloadManager.COLUMN_STATUS);
        int reason = extras.getInt(DownloadManager.COLUMN_REASON);

        switch (status) {
            case DownloadManager.STATUS_FAILED:
                String failedReason = "";
                switch (reason) {
                    case DownloadManager.ERROR_CANNOT_RESUME:
                        failedReason = "ERROR_CANNOT_RESUME";
                        break;
                    case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                        failedReason = "ERROR_DEVICE_NOT_FOUND";
                        break;
                    case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                        failedReason = "ERROR_FILE_ALREADY_EXISTS";
                        break;
                    case DownloadManager.ERROR_FILE_ERROR:
                        failedReason = "ERROR_FILE_ERROR";
                        break;
                    case DownloadManager.ERROR_HTTP_DATA_ERROR:
                        failedReason = "ERROR_HTTP_DATA_ERROR";
                        break;
                    case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                        failedReason = "ERROR_INSUFFICIENT_SPACE";
                        break;
                    case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                        failedReason = "ERROR_TOO_MANY_REDIRECTS";
                        break;
                    case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                        failedReason = "ERROR_UNHANDLED_HTTP_CODE";
                        break;
                    case DownloadManager.ERROR_UNKNOWN:
                        failedReason = "ERROR_UNKNOWN";
                        break;
                }

                Toast.makeText(context, context.getString(R.string.download_error) + " " + failedReason, Toast.LENGTH_LONG).show();

        }

    }
}
