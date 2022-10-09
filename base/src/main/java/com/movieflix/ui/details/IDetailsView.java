package com.movieflix.ui.details;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.movieflix.base.model.video.info.Torrent;
import com.movieflix.base.model.video.info.VideoInfo;

public interface IDetailsView<T extends VideoInfo> {

    void onVideoInfo(@NonNull T videoInfo);

    void onDubbing(@Nullable String[] languages, int position);

    void onTorrents(@Nullable Torrent[] torrents, int position);

    void onLangSubtitles(@Nullable String[] languages, int position);
}
