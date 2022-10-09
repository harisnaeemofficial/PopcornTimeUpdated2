package com.movieflix.base.torrent.client;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.movieflix.base.model.WatchInfo;
import com.movieflix.base.torrent.TorrentService;
import com.movieflix.base.torrent.watch.WatchListener;

public class WatchClient extends BaseClient {

    public WatchClient(Context context) {
        super(context);
    }

    public void startWatch(WatchInfo watchInfo, WatchListener listener) {
        if (bound) {
            torrentService.startWatch(watchInfo, listener);
        }
    }

    public void stopWatch() {
        Intent intent = TorrentService.createIntent(context);
        intent.setAction(TorrentService.ACTION_STOP_WATCH);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public void removeWatchListener(WatchListener listener) {
        if (bound) {
            torrentService.removeWatchListener(listener);
        }
    }
}