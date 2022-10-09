package com.movieflix.base.torrent.client;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.movieflix.base.database.tables.Downloads;
import com.movieflix.base.model.DownloadInfo;
import com.movieflix.base.prefs.PopcornPrefs;
import com.movieflix.base.prefs.Prefs;
import com.movieflix.base.storage.StorageUtil;
import com.movieflix.base.torrent.AddToDownloadsThread;
import com.movieflix.base.torrent.TorrentState;
import com.movieflix.base.torrent.TorrentUtil;
import com.movieflix.base.utils.Logger;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadsClient extends BaseClient {

    public DownloadsClient(Context context) {
        super(context);
    }

    public void downloadsAdd(DownloadInfo info) {
        if (!bound) {
            return;
        }
        String lastTorrent = Prefs.getPopcornPrefs().get(PopcornPrefs.LAST_TORRENT, "");
        if (!"".equals(lastTorrent)) {
            if (lastTorrent.equals(info.torrentUrl) || lastTorrent.equals(info.torrentMagnet)) {
                TorrentUtil.saveMetadata(torrentService, info, lastTorrent);
                torrentService.removeTorrent(lastTorrent);
                Prefs.getPopcornPrefs().put(PopcornPrefs.LAST_TORRENT, "");
                // move files from cache dir to download dir
                File[] files = StorageUtil.getCacheDir().listFiles();
                if (files != null) {
                    for (File file : files) {
                        try {
                            if (file.isDirectory()) {
                                FileUtils.moveDirectoryToDirectory(file, info.directory, true);
                            } else {
                                FileUtils.moveFileToDirectory(file, info.directory, true);
                            }
                        } catch (IOException e) {
                            Logger.error(e.getMessage());
                        }
                    }
                }
            }
        }
        Downloads.insert(context, info);

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(new AddToDownloadsThread(context, torrentService, info));
        executorService.shutdown();
    }

    public void downloadsPause(DownloadInfo info) {
        if (!bound) {
            return;
        }
        String torrentFile = getAvailableTorrentFile(info);
        if (!TextUtils.isEmpty(torrentFile)) {
            pauseTorrent(torrentFile);
        }
        if (TorrentState.DOWNLOADING == info.state) {
            info.state = TorrentState.PAUSED;
            Downloads.update(context, info);
        }
    }

    public void downloadsResume(DownloadInfo info) {
        if (!bound) {
            return;
        }
        String torrentFile = getAvailableTorrentFile(info);
        if (!TextUtils.isEmpty(torrentFile)) {
            resumeTorrent(torrentFile);
        }
        if (TorrentState.PAUSED == info.state) {
            info.state = TorrentState.DOWNLOADING;
            Downloads.update(context, info);
        }
    }

    public void downloadsRemove(DownloadInfo info) {
        if (!bound) {
            return;
        }
        String torrentFile = getAvailableTorrentFile(info);
        if (!TextUtils.isEmpty(torrentFile)) {
            removeTorrent(torrentFile);
        }
        Downloads.delete(context, info);
        if (info.directory.exists()) {
            StorageUtil.deleteDir(info.directory);
        }
    }

    public void downloadsRetry(DownloadInfo info) {
        if (!bound) {
            return;
        }
        info.state = TorrentState.DOWNLOADING;
        Downloads.update(context, info);
        new Thread(new AddToDownloadsThread(context, torrentService, info)).start();
    }

    public void downloadsPauseAll() {
        if (!bound) {
            return;
        }
        String selection = Downloads._STATE + "=" + TorrentState.DOWNLOADING;
        Cursor cursor = Downloads.query(context, null, selection, null, Downloads._ID + " DESC");
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    final DownloadInfo info = new DownloadInfo();
                    Downloads.populate(info, cursor);
                    downloadsPause(info);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

    public void downloadsResumeAll() {
        if (!bound) {
            return;
        }
        String selection = Downloads._STATE + "=" + TorrentState.PAUSED;
        Cursor cursor = Downloads.query(context, null, selection, null, Downloads._ID + " DESC");
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    final DownloadInfo info = new DownloadInfo();
                    Downloads.populate(info, cursor);
                    downloadsResume(info);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

    public void downloadsRemoveAll() {
        if (!bound) {
            return;
        }
        Cursor cursor = Downloads.query(context, null, null, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    final DownloadInfo info = new DownloadInfo();
                    Downloads.populate(info, cursor);
                    downloadsRemove(info);
                } while (cursor.moveToNext());
                context.getContentResolver().delete(Downloads.CONTENT_URI, null, null);
                StorageUtil.clearDownloadsDir();
            }
            cursor.close();
        }
    }

    public String getAvailableTorrentFile(DownloadInfo info) {
        return getAvailableTorrentFile(info.torrentFilePath, info.torrentUrl, info.torrentMagnet);
    }
}