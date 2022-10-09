package com.movieflix.base.torrent.client;

import android.content.Context;
import android.database.Cursor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.movieflix.base.database.tables.Downloads;
import com.movieflix.base.model.DownloadInfo;
import com.movieflix.base.prefs.PopcornPrefs;
import com.movieflix.base.prefs.Prefs;
import com.movieflix.base.receiver.ConnectivityReceiver;
import com.movieflix.base.torrent.AddToDownloadsThread;
import com.movieflix.base.torrent.TorrentState;

public class MainClient extends BaseClient {

    final int DEFAULT_INIT_THREADS_COUNT = 3;

    private ExecutorService executorService;
    private boolean init;

    ClientConnectionListener connectionListener = new ClientConnectionListener() {
        @Override
        public void onClientConnected() {
            if (!init) {
//                setProxy(null);
                ConnectivityReceiver.checkWifiConnection(context);
                initDownloads();
                init = true;
            }
        }

        @Override
        public void onClientDisconnected() {

        }
    };

    public MainClient(Context context) {
        super(context);
        init = false;
        setConnectionListener(connectionListener);
    }

    public void exitFromApp() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }

    public void removeLastOnExit() {
        if (!bound) {
            return;
        }
        String last = Prefs.getPopcornPrefs().get(PopcornPrefs.LAST_TORRENT, "");
        if (!last.equals("")) {
            removeTorrent(last);
            Prefs.getPopcornPrefs().put(PopcornPrefs.LAST_TORRENT, "");
        }
    }

    private void initDownloads() {
        Cursor cursor = Downloads.query(context, null, null, null, Downloads._ID + " DESC");
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                int threadsCount = cursor.getCount() >= DEFAULT_INIT_THREADS_COUNT ? DEFAULT_INIT_THREADS_COUNT : cursor.getCount();
                executorService = Executors.newFixedThreadPool(threadsCount);
                do {
                    final DownloadInfo info = new DownloadInfo();
                    Downloads.populate(info, cursor);
                    if (TorrentState.ERROR == info.state) {
                        info.state = TorrentState.DOWNLOADING;
                        Downloads.update(context, info);
                    }
                    executorService.execute(new AddToDownloadsThread(context, torrentService, info));
                } while (cursor.moveToNext());
                executorService.shutdown();
            }
            cursor.close();
        }
    }
}