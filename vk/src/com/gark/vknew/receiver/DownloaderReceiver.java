package com.gark.vknew.receiver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import com.gark.vknew.services.DownloadService;

/**
 * Created by Artem on 18.07.13.
 */
public class DownloaderReceiver extends BroadcastReceiver {

    private DownloadManager dm;

    @Override
    public void onReceive(Context context, Intent intent) {

        try {


            dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                Bundle extras = intent.getExtras();

                long id = extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID);

                DownloadManager.Query q = new DownloadManager.Query();
                q.setFilterById(id);

                Cursor c = dm.query(q);
                if (c != null && c.moveToFirst()) {
                    if (c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_FAILED) {
                        String uri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI));
                        dm.remove(id);

                        String title = c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE));
                        title = title.replaceAll("[^a-zA-Z0-9.]+", "");


                        try {
                            if (uri.contains("mp3")) {
                                title += ".mp3";
                            } else if (uri.contains("mp4")) {
                                title += ".mp4";
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        Intent downloadIntent = new Intent(context, DownloadService.class);
                        downloadIntent.putExtra(DownloadService.URL, uri);
                        downloadIntent.putExtra(DownloadService.ID, id);
                        downloadIntent.putExtra(DownloadService.TITLE, title);
                        context.startService(downloadIntent);

                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
