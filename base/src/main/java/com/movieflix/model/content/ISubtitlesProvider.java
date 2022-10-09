package com.movieflix.model.content;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import com.movieflix.base.model.WatchInfo;
import com.movieflix.base.model.video.info.Episode;
import com.movieflix.base.model.video.info.Season;
import com.movieflix.base.model.video.info.Torrent;
import com.movieflix.base.model.video.info.VideoInfo;
import com.movieflix.model.subtitles.Subtitles;

public interface ISubtitlesProvider {

    @NonNull
    Observable<Map.Entry<String, List<Subtitles>>[]> getSubtitles(@NonNull VideoInfo videoInfo,
                                                                @Nullable Season season,
                                                                @Nullable Episode episode,
                                                                @Nullable Torrent torrent);

    @NonNull
    Observable<Map.Entry<String, List<Subtitles>>[]> getSubtitles(@NonNull WatchInfo watchInfo);
}
