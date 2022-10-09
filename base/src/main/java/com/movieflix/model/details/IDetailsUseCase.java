package com.movieflix.model.details;

import android.support.annotation.NonNull;

import java.util.List;
import java.util.Map;

import com.movieflix.base.model.video.info.Episode;
import com.movieflix.base.model.video.info.Season;
import com.movieflix.base.model.video.info.Torrent;
import com.movieflix.base.model.video.info.VideoInfo;
import com.movieflix.model.ObservableChoiceProperty;
import com.movieflix.model.ObservableProperty;
import com.movieflix.model.subtitles.Subtitles;

public interface IDetailsUseCase {

    @NonNull
    ObservableProperty<VideoInfo> getVideoInfoProperty();

    @NonNull
    ObservableChoiceProperty<Season> getSeasonChoiceProperty();

    @NonNull
    ObservableChoiceProperty<Episode> getEpisodeChoiceProperty();

    @NonNull
    ObservableChoiceProperty<Map.Entry<String, List<Torrent>>> getDubbingChoiceProperty();

    @NonNull
    ObservableChoiceProperty<Torrent> getTorrentChoiceProperty();

    @NonNull
    ObservableChoiceProperty<Map.Entry<String, List<Subtitles>>> getLangSubtitlesChoiceProperty();

    @NonNull
    ObservableChoiceProperty<Subtitles> getSubtitlesChoiceProperty();

    @NonNull
    ObservableProperty<Subtitles> getCustomSubtitlesProperty();
}
